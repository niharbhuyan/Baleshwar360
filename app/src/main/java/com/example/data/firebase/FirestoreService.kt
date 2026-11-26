package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.example.data.local.CivicReportEntity
import com.example.data.local.HotspotEntity
import com.example.data.local.NewsArticleEntity
import com.example.data.local.ReviewEntity
import com.example.data.local.UserEntity
import com.example.data.model.FeedbackReport
import com.example.data.model.FirestoreUserReport
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

enum class OperationType(val value: String) {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    LIST("list"),
    GET("get"),
    WRITE("write"),
}

fun handleFirestoreError(exception: Throwable, operationType: OperationType, path: String?): String {
    val causes = generateSequence(exception) { it.cause }
    val rootError = causes.firstOrNull { it is FirebaseFirestoreException }
        ?: causes.firstOrNull { it !is CancellationException }
        ?: exception

    val auth = try {
        FirebaseAuth.getInstance()
    } catch (_: Exception) {
        null
    }
    val currentUser = auth?.currentUser

    val providerInfoList = currentUser?.providerData?.map { provider ->
        JSONObject().apply {
            put("providerId", provider.providerId)
            put("email", provider.email)
        }
    } ?: emptyList()

    val authInfoJson = JSONObject().apply {
        put("userId", currentUser?.uid)
        put("email", currentUser?.email)
        put("emailVerified", currentUser?.isEmailVerified)
        put("tenantId", currentUser?.tenantId)
        put("providerInfo", JSONArray(providerInfoList))
    }

    val errorInfoJson = JSONObject().apply {
        put("error", rootError.message ?: rootError.toString())
        put("operationType", operationType.value)
        put("path", path)
        put("authInfo", authInfoJson)
    }

    val jsonString = errorInfoJson.toString()
    Log.e("FirestoreError", "Firestore Error: $jsonString")
    return jsonString
}

/**
 * Cloud Firestore persistence service for Balasore 360.
 * Persists user bookmarks, saved articles, preferences, and community reviews.
 */
class FirestoreService(
    private val context: Context,
    private val customFirestore: FirebaseFirestore? = null,
    private val requireClientAuthMatch: Boolean = true
) {

    private val firestore: FirebaseFirestore? by lazy {
        if (customFirestore != null) {
            customFirestore
        } else {
            try {
                if (com.example.data.fcm.FcmManager.isFirebaseConfigured(context) || FirebaseApp.getApps(context).isNotEmpty()) {
                    val dbId = context.getString(com.example.R.string.firestore_database_id)
                    FirebaseFirestore.getInstance(dbId)
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.w("FirestoreService", "Firebase not initialized: ${e.message}")
                null
            }
        }
    }

    /**
     * Returns the Firestore instance for `/users/{userId}` paths only when the user is
     * actively authenticated with Firebase Auth and their UID matches `userId` (unless
     * `requireClientAuthMatch` is disabled for security rules negative testing).
     */
    private fun getAuthenticatedFirestoreForUser(userId: String): FirebaseFirestore? {
        if (userId.isBlank() || userId == "local_weather_user") return null
        if (!requireClientAuthMatch) return firestore
        val currentFbUid = try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseAuth.getInstance().currentUser?.uid
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
        if (currentFbUid == null || currentFbUid != userId) {
            return null
        }
        return firestore
    }

    private fun getSignedInFirestore(): FirebaseFirestore? {
        if (!requireClientAuthMatch) return firestore
        val currentFbUid = try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseAuth.getInstance().currentUser?.uid
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
        if (currentFbUid == null) return null
        return firestore
    }

    // Local in-memory cache for seamless offline-first experience
    private val localFeedbackCache = mutableListOf<FeedbackReport>()

    val isAvailable: Boolean
        get() = firestore != null

    /**
     * Save or sync a bookmarked hotspot to Firestore for the authenticated user.
     */
    suspend fun saveBookmark(userId: String, hotspot: HotspotEntity): Result<Unit> = withContext(Dispatchers.IO) {
        val path = "users/$userId/bookmarks/${hotspot.id}"
        try {
            val db = getAuthenticatedFirestoreForUser(userId) ?: return@withContext Result.success(Unit)
            val docRef = db.collection("users")
                .document(userId)
                .collection("bookmarks")
                .document(hotspot.id)

            val data = hashMapOf<String, Any>(
                "spotId" to hotspot.id,
                "userId" to userId,
                "spotName" to hotspot.name,
                "category" to hotspot.category,
                "location" to "Balasore",
                "rating" to 4.8,
                "bookmarkedAt" to System.currentTimeMillis()
            )

            docRef.set(data, SetOptions.merge()).await()
            Log.d("FirestoreService", "Bookmark saved to Firestore for user: $userId, spot: ${hotspot.id}")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    /**
     * Remove a bookmarked hotspot from Firestore.
     */
    suspend fun removeBookmark(userId: String, hotspotId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val path = "users/$userId/bookmarks/$hotspotId"
        try {
            val db = getAuthenticatedFirestoreForUser(userId) ?: return@withContext Result.success(Unit)
            db.collection("users")
                .document(userId)
                .collection("bookmarks")
                .document(hotspotId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    /**
     * Fetch user's bookmarked hotspot IDs from Firestore.
     */
    suspend fun fetchBookmarkedIds(userId: String): Result<List<String>> = withContext(Dispatchers.IO) {
        val path = "users/$userId/bookmarks"
        try {
            val db = getAuthenticatedFirestoreForUser(userId) ?: return@withContext Result.success(emptyList())
            val snapshot = db.collection("users")
                .document(userId)
                .collection("bookmarks")
                .get()
                .await()

            val ids = snapshot.documents.map { it.id }
            Result.success(ids)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            Result.failure(e)
        }
    }

    /**
     * Persist user preferences to Firestore.
     */
    suspend fun saveUserPreferences(
        userId: String,
        language: String,
        weatherAlerts: Boolean,
        breakingNews: Boolean,
        locality: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val path = "users/$userId/profile/preferences"
        try {
            val db = getAuthenticatedFirestoreForUser(userId) ?: return@withContext Result.success(Unit)
            val data = hashMapOf(
                "userId" to userId,
                "language" to language,
                "weatherAlertsEnabled" to weatherAlerts,
                "breakingNewsEnabled" to breakingNews,
                "locality" to locality,
                "lastSyncedAt" to System.currentTimeMillis()
            )
            db.collection("users")
                .document(userId)
                .collection("profile")
                .document("preferences")
                .set(data, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    /**
     * Save user profile details to Firestore matching firestore.rules isValidUserProfile.
     */
    suspend fun saveUserProfile(user: UserEntity): Result<Unit> = withContext(Dispatchers.IO) {
        val path = "users/${user.id}"
        try {
            val db = getAuthenticatedFirestoreForUser(user.id) ?: return@withContext Result.success(Unit)
            val now = System.currentTimeMillis()
            val data = hashMapOf<String, Any>(
                "userId" to user.id,
                "displayName" to user.fullName.ifBlank { "Balasore Resident" },
                "email" to user.email.ifBlank { "${user.id}@balasore.in" },
                "photoUrl" to (user.avatarUri ?: ""),
                "bio" to user.bio,
                "locality" to user.locality,
                "phoneNumber" to user.phoneNumber,
                "createdAt" to (if (user.createdAt > 0) user.createdAt else now),
                "updatedAt" to now
            )
            db.collection("users")
                .document(user.id)
                .set(data, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun fetchUserProfile(userId: String): Result<UserEntity?> = withContext(Dispatchers.IO) {
        val path = "users/$userId"
        try {
            val db = getAuthenticatedFirestoreForUser(userId) ?: return@withContext Result.success(null)
            val doc = db.collection("users").document(userId).get().await()
            if (!doc.exists()) {
                return@withContext Result.success(null)
            }
            val data = doc.data ?: emptyMap()
            val user = UserEntity(
                id = doc.id,
                fullName = data["displayName"] as? String ?: "Balasore Resident",
                email = data["email"] as? String ?: "",
                passwordHash = "FIREBASE_MANAGED",
                phoneNumber = data["phoneNumber"] as? String ?: "",
                locality = data["locality"] as? String ?: "Balasore",
                bio = data["bio"] as? String ?: "Verified Community Member",
                avatarUri = data["photoUrl"] as? String,
                securityQuestion = "Google Account Auth",
                securityAnswer = "FIREBASE_AUTH_MANAGED",
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
            Result.success(user)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            Result.failure(e)
        }
    }

    /**
     * Weather & Event Notification Preferences in local cache + Firestore subcollection users/{userId}/preferences/weather
     */
    suspend fun saveWeatherPreferences(
        userId: String,
        prefs: com.example.data.model.UserWeatherPreferences
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val path = "users/$userId/preferences/weather"
        try {
            val localPrefs = context.getSharedPreferences("balasore_user_notif_prefs", Context.MODE_PRIVATE)
            localPrefs.edit()
                .putBoolean("${userId}_notifyCyclone", prefs.notifyCyclone)
                .putBoolean("${userId}_notifyHighTide", prefs.notifyHighTide)
                .putBoolean("${userId}_notifySevereHeat", prefs.notifySevereHeat)
                .putBoolean("${userId}_notifyRainfall", prefs.notifyRainfall)
                .putBoolean("${userId}_notifyLocalEvents", prefs.notifyLocalEvents)
                .putBoolean("${userId}_notifyTempleFestivals", prefs.notifyTempleFestivals)
                .putBoolean("${userId}_notifyBreakingNews", prefs.notifyBreakingNews)
                .putBoolean("${userId}_notifyTourismHotspots", prefs.notifyTourismHotspots)
                .putBoolean("${userId}_notifyDefenseAdvisories", prefs.notifyDefenseAdvisories)
                .putBoolean("${userId}_notifyFloodAlerts", prefs.notifyFloodAlerts)
                .putBoolean("${userId}_notifyKuldihaSafari", prefs.notifyKuldihaSafari)
                .putBoolean("${userId}_notifyPanchalingeswar", prefs.notifyPanchalingeswar)
                .putBoolean("${userId}_notifySafeTideWindow", prefs.notifySafeTideWindow)
                .putInt("${userId}_highTideAlertHoursBefore", prefs.highTideAlertHoursBefore)
                .putInt("${userId}_minTempThreshold", prefs.minTempThreshold)
                .putInt("${userId}_maxTempThreshold", prefs.maxTempThreshold)
                .putStringSet("${userId}_preferredNewsCategories", prefs.preferredNewsCategories.toSet())
                .putString("${userId}_minWeatherAlertSeverity", prefs.minWeatherAlertSeverity)
                .putString("${userId}_notificationFrequency", prefs.notificationFrequency)
                .putString("${userId}_preferredNotificationWindow", prefs.preferredNotificationWindow)
                .putLong("${userId}_updatedAt", System.currentTimeMillis())
                .apply()

            val db = getAuthenticatedFirestoreForUser(userId)
            if (db != null) {
                db.collection("users")
                    .document(userId)
                    .collection("preferences")
                    .document("weather")
                    .set(prefs.copy(userId = userId).toFirestoreMap(), SetOptions.merge())
                    .await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            if (!requireClientAuthMatch) return@withContext Result.failure(e)
            Result.success(Unit)
        }
    }

    suspend fun fetchWeatherPreferences(userId: String): Result<com.example.data.model.UserWeatherPreferences?> = withContext(Dispatchers.IO) {
        val localPrefs = context.getSharedPreferences("balasore_user_notif_prefs", Context.MODE_PRIVATE)
        fun loadFromLocal(): com.example.data.model.UserWeatherPreferences {
            val savedCats = localPrefs.getStringSet("${userId}_preferredNewsCategories", null)
                ?.toList()
                ?.takeIf { it.isNotEmpty() }
                ?: listOf("Civic", "Events", "Traffic", "Weather", "Defense", "Tourism", "Culture")
            return com.example.data.model.UserWeatherPreferences(
                userId = userId,
                notifyCyclone = localPrefs.getBoolean("${userId}_notifyCyclone", true),
                notifyHighTide = localPrefs.getBoolean("${userId}_notifyHighTide", true),
                notifySevereHeat = localPrefs.getBoolean("${userId}_notifySevereHeat", true),
                notifyRainfall = localPrefs.getBoolean("${userId}_notifyRainfall", true),
                notifyLocalEvents = localPrefs.getBoolean("${userId}_notifyLocalEvents", true),
                notifyTempleFestivals = localPrefs.getBoolean("${userId}_notifyTempleFestivals", true),
                notifyBreakingNews = localPrefs.getBoolean("${userId}_notifyBreakingNews", true),
                notifyTourismHotspots = localPrefs.getBoolean("${userId}_notifyTourismHotspots", true),
                notifyDefenseAdvisories = localPrefs.getBoolean("${userId}_notifyDefenseAdvisories", true),
                notifyFloodAlerts = localPrefs.getBoolean("${userId}_notifyFloodAlerts", true),
                notifyKuldihaSafari = localPrefs.getBoolean("${userId}_notifyKuldihaSafari", true),
                notifyPanchalingeswar = localPrefs.getBoolean("${userId}_notifyPanchalingeswar", true),
                notifySafeTideWindow = localPrefs.getBoolean("${userId}_notifySafeTideWindow", true),
                highTideAlertHoursBefore = localPrefs.getInt("${userId}_highTideAlertHoursBefore", 2),
                minTempThreshold = localPrefs.getInt("${userId}_minTempThreshold", 12),
                maxTempThreshold = localPrefs.getInt("${userId}_maxTempThreshold", 38),
                preferredNewsCategories = savedCats,
                minWeatherAlertSeverity = localPrefs.getString("${userId}_minWeatherAlertSeverity", "MODERATE") ?: "MODERATE",
                notificationFrequency = localPrefs.getString("${userId}_notificationFrequency", "REAL_TIME") ?: "REAL_TIME",
                preferredNotificationWindow = localPrefs.getString("${userId}_preferredNotificationWindow", "ANYTIME") ?: "ANYTIME",
                updatedAt = localPrefs.getLong("${userId}_updatedAt", System.currentTimeMillis())
            )
        }
        val path = "users/$userId/preferences/weather"
        try {
            val db = getAuthenticatedFirestoreForUser(userId) ?: return@withContext Result.success(loadFromLocal())
            val doc = db.collection("users")
                .document(userId)
                .collection("preferences")
                .document("weather")
                .get()
                .await()
            if (!doc.exists()) {
                return@withContext Result.success(loadFromLocal())
            }
            Result.success(com.example.data.model.UserWeatherPreferences.fromFirestoreMap(doc.data ?: emptyMap()))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            if (!requireClientAuthMatch) return@withContext Result.failure(e)
            Result.success(loadFromLocal())
        }
    }

    /**
     * Save Favorite News in local cache + Firestore subcollection users/{userId}/favorite_news/{newsId}
     */
    suspend fun saveFavoriteNews(
        userId: String,
        item: com.example.data.model.FavoriteNewsCloudItem
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val path = "users/$userId/favorite_news/${item.newsId}"
        try {
            val prefs = context.getSharedPreferences("balasore_user_saved_news", Context.MODE_PRIVATE)
            val currentSet = prefs.getStringSet("news_ids_$userId", emptySet())?.toMutableSet() ?: mutableSetOf()
            currentSet.add(item.newsId)
            prefs.edit()
                .putStringSet("news_ids_$userId", currentSet)
                .putString("news_title_${userId}_${item.newsId}", item.title)
                .putString("news_cat_${userId}_${item.newsId}", item.category)
                .putString("news_src_${userId}_${item.newsId}", item.source)
                .putString("news_pub_${userId}_${item.newsId}", item.publishedAt)
                .apply()

            com.example.data.recommendation.BalasoreRecommendationEngine.recordArticleSaved(
                context = context,
                articleId = item.newsId,
                title = item.title,
                category = item.category,
                isSaved = true
            )

            val db = getAuthenticatedFirestoreForUser(userId)
            if (db != null) {
                db.collection("users")
                    .document(userId)
                    .collection("favorite_news")
                    .document(item.newsId)
                    .set(item.copy(userId = userId).toFirestoreMap(), SetOptions.merge())
                    .await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            if (!requireClientAuthMatch) return@withContext Result.failure(e)
            Result.success(Unit)
        }
    }

    suspend fun removeFavoriteNews(userId: String, newsId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val path = "users/$userId/favorite_news/$newsId"
        try {
            val prefs = context.getSharedPreferences("balasore_user_saved_news", Context.MODE_PRIVATE)
            val currentSet = prefs.getStringSet("news_ids_$userId", emptySet())?.toMutableSet() ?: mutableSetOf()
            currentSet.remove(newsId)
            prefs.edit().putStringSet("news_ids_$userId", currentSet).apply()

            com.example.data.recommendation.BalasoreRecommendationEngine.recordArticleSaved(
                context = context,
                articleId = newsId,
                title = "",
                category = "",
                isSaved = false
            )

            val db = getAuthenticatedFirestoreForUser(userId)
            if (db != null) {
                db.collection("users")
                    .document(userId)
                    .collection("favorite_news")
                    .document(newsId)
                    .delete()
                    .await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            if (!requireClientAuthMatch) return@withContext Result.failure(e)
            Result.success(Unit)
        }
    }

    suspend fun fetchFavoriteNews(userId: String): Result<List<com.example.data.model.FavoriteNewsCloudItem>> = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences("balasore_user_saved_news", Context.MODE_PRIVATE)
        val localIds = prefs.getStringSet("news_ids_$userId", emptySet()) ?: emptySet()
        val localItems = localIds.map { nid ->
            com.example.data.model.FavoriteNewsCloudItem(
                newsId = nid,
                userId = userId,
                title = prefs.getString("news_title_${userId}_$nid", null)
                    ?: com.example.data.repository.BalasoreRepository.newsArticles.find { it.id == nid }?.title
                    ?: "Saved Balasore Report #$nid",
                category = prefs.getString("news_cat_${userId}_$nid", "Local News") ?: "Local News",
                source = prefs.getString("news_src_${userId}_$nid", "Balasore 360") ?: "Balasore 360",
                publishedAt = prefs.getString("news_pub_${userId}_$nid", "Saved") ?: "Saved"
            )
        }
        val path = "users/$userId/favorite_news"
        try {
            val db = getAuthenticatedFirestoreForUser(userId) ?: return@withContext Result.success(localItems)
            val snapshot = db.collection("users")
                .document(userId)
                .collection("favorite_news")
                .get()
                .await()
            val list = snapshot.documents.map {
                com.example.data.model.FavoriteNewsCloudItem.fromFirestoreMap(it.data ?: emptyMap())
            }
            Result.success((list + localItems).distinctBy { it.newsId })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            if (!requireClientAuthMatch) return@withContext Result.failure(e)
            Result.success(localItems)
        }
    }

    /**
     * Cloud Tourism Bookmarks in local cache + users/{userId}/bookmarks/{spotId}
     */
    suspend fun saveCloudBookmark(
        userId: String,
        item: com.example.data.model.TourismBookmarkCloudItem
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val path = "users/$userId/bookmarks/${item.spotId}"
        try {
            val prefs = context.getSharedPreferences("balasore_user_saved_spots", Context.MODE_PRIVATE)
            val currentSet = prefs.getStringSet("spot_ids_$userId", emptySet())?.toMutableSet() ?: mutableSetOf()
            currentSet.add(item.spotId)
            prefs.edit()
                .putStringSet("spot_ids_$userId", currentSet)
                .putString("spot_name_${userId}_${item.spotId}", item.spotName)
                .putString("spot_cat_${userId}_${item.spotId}", item.category)
                .putString("spot_loc_${userId}_${item.spotId}", item.location)
                .putFloat("spot_rat_${userId}_${item.spotId}", item.rating.toFloat())
                .apply()

            com.example.data.recommendation.BalasoreRecommendationEngine.recordLocationSaved(
                context = context,
                spotId = item.spotId,
                spotName = item.spotName,
                category = item.category,
                location = item.location,
                isSaved = true
            )

            val db = getAuthenticatedFirestoreForUser(userId)
            if (db != null) {
                db.collection("users")
                    .document(userId)
                    .collection("bookmarks")
                    .document(item.spotId)
                    .set(item.copy(userId = userId).toFirestoreMap(), SetOptions.merge())
                    .await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            if (!requireClientAuthMatch) return@withContext Result.failure(e)
            Result.success(Unit)
        }
    }

    suspend fun removeCloudBookmark(userId: String, spotId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val path = "users/$userId/bookmarks/$spotId"
        try {
            val prefs = context.getSharedPreferences("balasore_user_saved_spots", Context.MODE_PRIVATE)
            val currentSet = prefs.getStringSet("spot_ids_$userId", emptySet())?.toMutableSet() ?: mutableSetOf()
            currentSet.remove(spotId)
            prefs.edit().putStringSet("spot_ids_$userId", currentSet).apply()

            com.example.data.recommendation.BalasoreRecommendationEngine.recordLocationSaved(
                context = context,
                spotId = spotId,
                spotName = "",
                category = "",
                location = "",
                isSaved = false
            )

            val db = getAuthenticatedFirestoreForUser(userId)
            if (db != null) {
                db.collection("users")
                    .document(userId)
                    .collection("bookmarks")
                    .document(spotId)
                    .delete()
                    .await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            if (!requireClientAuthMatch) return@withContext Result.failure(e)
            Result.success(Unit)
        }
    }

    suspend fun fetchCloudBookmarks(userId: String): Result<List<com.example.data.model.TourismBookmarkCloudItem>> = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences("balasore_user_saved_spots", Context.MODE_PRIVATE)
        val localIds = prefs.getStringSet("spot_ids_$userId", emptySet()) ?: emptySet()
        val localItems = localIds.map { sid ->
            com.example.data.model.TourismBookmarkCloudItem(
                spotId = sid,
                userId = userId,
                spotName = prefs.getString("spot_name_${userId}_$sid", null)
                    ?: com.example.data.repository.BalasoreRepository.hotspots.find { it.id == sid }?.name
                    ?: "Saved Spot #$sid",
                category = prefs.getString("spot_cat_${userId}_$sid", "Tourism") ?: "Tourism",
                location = prefs.getString("spot_loc_${userId}_$sid", "Balasore") ?: "Balasore",
                rating = prefs.getFloat("spot_rat_${userId}_$sid", 4.7f).toDouble()
            )
        }
        val path = "users/$userId/bookmarks"
        try {
            val db = getAuthenticatedFirestoreForUser(userId) ?: return@withContext Result.success(localItems)
            val snapshot = db.collection("users")
                .document(userId)
                .collection("bookmarks")
                .get()
                .await()
            val list = snapshot.documents.map {
                com.example.data.model.TourismBookmarkCloudItem.fromFirestoreMap(it.data ?: emptyMap())
            }
            Result.success((list + localItems).distinctBy { it.spotId })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            if (!requireClientAuthMatch) return@withContext Result.failure(e)
            Result.success(localItems)
        }
    }

    /**
     * Tourism Reviews in tourism_reviews/{reviewId}
     */
    suspend fun submitTourismReview(review: com.example.data.model.TourismReviewCloudEntity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val db = getSignedInFirestore() ?: return@withContext Result.success(Unit)
            val currentFbUid = try { FirebaseAuth.getInstance().currentUser?.uid } catch (_: Exception) { null }
            val docId = if (review.reviewId.isNotBlank()) review.reviewId else db.collection("tourism_reviews").document().id
            val effectiveUserId = currentFbUid ?: review.userId
            val finalReview = review.copy(reviewId = docId, userId = effectiveUserId)
            db.collection("tourism_reviews")
                .document(docId)
                .set(finalReview.toFirestoreMap(), SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "tourism_reviews/${review.reviewId}")
            Result.failure(e)
        }
    }

    suspend fun fetchTourismReviewsForSpot(spotId: String): Result<List<com.example.data.model.TourismReviewCloudEntity>> = withContext(Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext Result.failure(Exception("Firestore not initialized"))
            val snapshot = db.collection("tourism_reviews")
                .whereEqualTo("spotId", spotId)
                .get()
                .await()
            val list = snapshot.documents.map {
                com.example.data.model.TourismReviewCloudEntity.fromFirestoreMap(it.data ?: emptyMap())
            }.sortedByDescending { it.createdAt }
            Result.success(list)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, "tourism_reviews")
            Result.failure(e)
        }
    }

    suspend fun fetchAllTourismReviews(): Result<List<com.example.data.model.TourismReviewCloudEntity>> = withContext(Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext Result.failure(Exception("Firestore not initialized"))
            val snapshot = db.collection("tourism_reviews")
                .limit(50)
                .get()
                .await()
            val list = snapshot.documents.map {
                com.example.data.model.TourismReviewCloudEntity.fromFirestoreMap(it.data ?: emptyMap())
            }.sortedByDescending { it.createdAt }
            Result.success(list)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, "tourism_reviews")
            Result.failure(e)
        }
    }

    /**
     * Save search history or query grounding analytics.
     */
    suspend fun logGroundedQuery(userId: String, query: String, groundingType: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val db = getAuthenticatedFirestoreForUser(userId) ?: return@withContext Result.success(Unit)
            val data = hashMapOf(
                "userId" to userId,
                "query" to query,
                "groundingType" to groundingType,
                "timestamp" to System.currentTimeMillis()
            )
            db.collection("users")
                .document(userId)
                .collection("grounded_queries")
                .add(data)
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "users/$userId/grounded_queries")
            Result.failure(e)
        }
    }

    /**
     * Submit user report or suggestion directly to the Firestore collection 'feedback'.
     */
    suspend fun submitFeedback(report: FeedbackReport): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Always preserve in local cache first
            synchronized(localFeedbackCache) {
                localFeedbackCache.removeAll { it.id == report.id }
                localFeedbackCache.add(0, report)
            }

            val db = getSignedInFirestore()
            if (db != null) {
                val data = report.toFirestoreMap()
                db.collection("feedback")
                    .document(report.id)
                    .set(data, SetOptions.merge())
                    .await()
                Log.d("FirestoreService", "Feedback successfully submitted to collection 'feedback': ${report.id}")
            } else {
                Log.i("FirestoreService", "Firestore offline/unauthenticated. Feedback preserved locally: ${report.id}")
            }
            Result.success(report.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "feedback/${report.id}")
            // Still succeed since it is cached locally
            Result.success(report.id)
        }
    }

    /**
     * Fetch latest feedback reports and suggestions from Firestore collection 'feedback'.
     */
    suspend fun fetchFeedbackReports(limitCount: Long = 25): Result<List<FeedbackReport>> = withContext(Dispatchers.IO) {
        try {
            val db = firestore
            if (db != null) {
                val snapshot = db.collection("feedback")
                    .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(limitCount)
                    .get()
                    .await()

                val firestoreReports = snapshot.documents.map { doc ->
                    FeedbackReport.fromFirestoreMap(doc.data ?: emptyMap())
                }
                Result.success(firestoreReports)
            } else {
                synchronized(localFeedbackCache) {
                    Result.success(localFeedbackCache.toList())
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, "feedback")
            synchronized(localFeedbackCache) {
                Result.success(localFeedbackCache.toList())
            }
        }
    }

    /**
     * Uploads Citizen Civic Eye infrastructure reports to Firestore collection 'civic_reports'.
     * Requires an active Firebase Auth session and validates payload fields against firestore.rules.
     */
    suspend fun submitCivicReport(
        report: CivicReportEntity,
        photoBase64: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val path = "civic_reports/${report.id}"
        try {
            val currentFbUid = try {
                if (FirebaseApp.getApps(context).isNotEmpty()) {
                    FirebaseAuth.getInstance().currentUser?.uid
                } else {
                    null
                }
            } catch (_: Exception) {
                null
            }
            val db = getSignedInFirestore()
            if (db == null || (requireClientAuthMatch && currentFbUid == null)) {
                return@withContext Result.failure(
                    IllegalStateException("Sign in with Google to save Civic Eye incidents to Firestore.")
                )
            }
            val safeLat = report.latitude?.takeIf { it in -90.0..90.0 }
            val safeLng = report.longitude?.takeIf { it in -180.0..180.0 }
            val normalizedStatus = report.status.uppercase().let {
                if (it in listOf("PENDING", "SUBMITTED", "IN_PROGRESS", "RESOLVED", "REJECTED", "DRAFT")) it else "PENDING"
            }
            val normalizedUrgency = report.urgency.uppercase().let {
                if (it in listOf("LOW", "NORMAL", "MEDIUM", "HIGH", "EMERGENCY")) it else "MEDIUM"
            }
            val effectivePhotoBase64 = photoBase64?.takeIf { it.isNotBlank() }
                ?: report.photoUri?.takeIf { it.startsWith("data:image") }
            val rawData = mutableMapOf<String, Any?>(
                "id" to report.id.take(128),
                "userId" to (currentFbUid ?: "authenticated_citizen").take(128),
                "title" to report.title.ifBlank { "Civic Incident" }.take(200),
                "category" to report.category.ifBlank { "Civic Issue" }.take(80),
                "wardLocation" to report.wardLocation.take(150),
                "description" to report.description.ifBlank { "Reported via Civic Eye." }.take(2000),
                "status" to normalizedStatus,
                "urgency" to normalizedUrgency,
                "assignedDepartment" to report.assignedDepartment.take(150),
                "reportedTimestamp" to report.reportedTimestamp,
                "lastUpdatedTimestamp" to report.lastUpdatedTimestamp,
                "hasPhotoAttached" to (report.hasPhotoAttached || !effectivePhotoBase64.isNullOrBlank()),
                "photoUri" to report.photoUri?.takeIf { it.isNotBlank() && !it.startsWith("data:image") }?.take(1000),
                "photoBase64" to effectivePhotoBase64?.takeIf { it.length <= 120000 },
                "latitude" to safeLat,
                "longitude" to safeLng,
                "geoAddress" to report.geoAddress?.takeIf { it.isNotBlank() }?.take(250),
                "resolutionNotes" to report.resolutionNotes?.takeIf { it.isNotBlank() }?.take(1000)
            )
            @Suppress("UNCHECKED_CAST")
            val data = rawData.filterValues { it != null } as Map<String, Any>
            db.collection("civic_reports")
                .document(report.id)
                .set(data, SetOptions.merge())
                .await()
            Log.d("FirestoreService", "Civic report synced to Firestore 'civic_reports': ${report.id}")
            Result.success(report.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    /**
     * Submit a Citizen Civic Eye report directly to Firestore collection 'user_reports'.
     * Requires Google Sign-In via Firebase Auth and writes real coordinates & Base64 photo thumbnail.
     */
    suspend fun submitUserReport(report: FirestoreUserReport): Result<String> = withContext(Dispatchers.IO) {
        val path = "user_reports/${report.id}"
        try {
            val currentFbUid = try {
                if (FirebaseApp.getApps(context).isNotEmpty()) {
                    FirebaseAuth.getInstance().currentUser?.uid
                } else {
                    null
                }
            } catch (_: Exception) {
                null
            }
            val db = getSignedInFirestore()
            if (db == null || (requireClientAuthMatch && currentFbUid == null)) {
                return@withContext Result.failure(
                    IllegalStateException("Sign in with Google to save Civic Eye incidents to Firestore.")
                )
            }
            val effectiveReport = if (currentFbUid != null) report.copy(userId = currentFbUid) else report
            db.collection("user_reports")
                .document(effectiveReport.id)
                .set(effectiveReport.toFirestoreMap(), SetOptions.merge())
                .await()
            Log.d("FirestoreService", "Civic report synced to Firestore 'user_reports': ${effectiveReport.id}")
            Result.success(effectiveReport.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    /**
     * Fetch all citizen civic reports from Firestore collection 'user_reports'.
     */
    suspend fun fetchUserReports(): List<FirestoreUserReport> = withContext(Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext emptyList()
            val snapshot = db.collection("user_reports")
                .limit(50)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                doc.data?.let { FirestoreUserReport.fromFirestoreMap(it) }
            }.sortedByDescending { it.reportedTimestamp }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, "user_reports")
            emptyList()
        }
    }

    /**
     * Get the set of tourism spot IDs marked as visited by the user (offline-first + cloud synced).
     */
    fun getVisitedSpotIds(): Set<String> {
        val prefs = context.getSharedPreferences("balasore_visited_spots_prefs", Context.MODE_PRIVATE)
        return prefs.getStringSet("visited_spot_ids", setOf("1", "3"))?.toSet() ?: setOf("1", "3")
    }

    fun isSpotVisited(spotId: String): Boolean {
        return getVisitedSpotIds().contains(spotId)
    }

    /**
     * Toggle visited status of a tourism hotspot and sync to Firestore when authenticated.
     */
    suspend fun toggleVisitedSpot(
        userId: String?,
        spotId: String,
        spotName: String,
        category: String = "Tourism",
        location: String = "Balasore"
    ): Set<String> = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences("balasore_visited_spots_prefs", Context.MODE_PRIVATE)
        val current = getVisitedSpotIds().toMutableSet()
        val nowVisited = if (current.contains(spotId)) {
            current.remove(spotId)
            false
        } else {
            current.add(spotId)
            true
        }
        prefs.edit()
            .putStringSet("visited_spot_ids", current)
            .putLong("visited_ts_$spotId", System.currentTimeMillis())
            .apply()

        if (!userId.isNullOrBlank()) {
            val path = "users/$userId/bookmarks/visited_$spotId"
            try {
                val db = getAuthenticatedFirestoreForUser(userId)
                if (db != null) {
                    val docRef = db.collection("users")
                        .document(userId)
                        .collection("bookmarks")
                        .document("visited_$spotId")
                    if (nowVisited) {
                        val item = com.example.data.model.TourismBookmarkCloudItem(
                            spotId = "visited_$spotId",
                            userId = userId,
                            spotName = "✓ Visited: $spotName",
                            category = category,
                            location = location,
                            rating = 5.0,
                            bookmarkedAt = System.currentTimeMillis()
                        )
                        docRef.set(item.toFirestoreMap(), SetOptions.merge()).await()
                    } else {
                        docRef.delete().await()
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, if (nowVisited) OperationType.WRITE else OperationType.DELETE, path)
            }
        }
        current.toSet()
    }

    /**
     * Sync citizen voice vote to Firestore 'citizen_votes' collection.
     */
    suspend fun saveCitizenVote(vote: com.example.data.local.CitizenVoteEntity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext Result.failure(Exception("Firestore not initialized"))
            val docId = "${vote.userId ?: "anon"}_${vote.pollId}"
            val docRef = db.collection("citizen_votes").document(docId)
            val data = hashMapOf(
                "pollId" to vote.pollId,
                "optionId" to vote.optionId,
                "optionLabel" to vote.optionLabel,
                "userId" to (vote.userId ?: "anonymous_citizen"),
                "votedAtTimestamp" to vote.votedAtTimestamp,
                "initiativeCategory" to vote.initiativeCategory,
                "source" to "BALASORE_360_CITY_ESSENTIALS"
            )
            docRef.set(data, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w("FirestoreService", "Error syncing citizen vote to Firestore: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Tracks and increments news article view counts in local storage and Firestore ('news_article_views')
     * to power the 'Trending' news category tab.
     */
    suspend fun incrementArticleViewCount(
        articleId: String,
        title: String,
        category: String
    ): Int = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences("balasore_news_view_counts", Context.MODE_PRIVATE)
        val defaultBase = defaultBaselineViewCount(articleId)
        val current = _trendingViewCountsFlow.value[articleId]
            ?: prefs.getInt("views_$articleId", defaultBase)
        val updated = current + 1
        prefs.edit().putInt("views_$articleId", updated).apply()
        updateInMemoryViewCount(articleId, updated)

        try {
            val db = firestore
            if (db != null) {
                val data = hashMapOf(
                    "articleId" to articleId,
                    "title" to title,
                    "category" to category,
                    "viewCount" to updated,
                    "lastViewedAt" to System.currentTimeMillis()
                )
                db.collection("news_article_views")
                    .document(articleId)
                    .set(data, SetOptions.merge())
                    .await()
            }
        } catch (e: Exception) {
            Log.w("FirestoreService", "Article view count synced locally (Firestore notice: ${e.message})")
        }
        updated
    }

    fun getArticleViewCountSync(articleId: String): Int {
        val inMemory = _trendingViewCountsFlow.value[articleId]
        if (inMemory != null) return inMemory
        val prefs = context.getSharedPreferences("balasore_news_view_counts", Context.MODE_PRIVATE)
        val stored = prefs.getInt("views_$articleId", defaultBaselineViewCount(articleId))
        updateInMemoryViewCount(articleId, stored)
        return stored
    }

    suspend fun fetchTrendingViewCounts(): Map<String, Int> = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences("balasore_news_view_counts", Context.MODE_PRIVATE)
        val result = _trendingViewCountsFlow.value.toMutableMap()
        // Load any locally persisted counts first
        prefs.all.forEach { (key, value) ->
            if (key.startsWith("views_") && value is Int) {
                val id = key.removePrefix("views_")
                result[id] = maxOf(result[id] ?: 0, value)
            }
        }
        try {
            val db = firestore
            if (db != null) {
                val snapshot = db.collection("news_article_views")
                    .limit(100)
                    .get()
                    .await()
                snapshot.documents.forEach { doc ->
                    val id = doc.getString("articleId") ?: doc.id
                    val count = (doc.get("viewCount") as? Number)?.toInt() ?: defaultBaselineViewCount(id)
                    val mergedCount = maxOf(result[id] ?: 0, count)
                    result[id] = mergedCount
                    prefs.edit().putInt("views_$id", mergedCount).apply()
                }
            }
        } catch (_: Exception) {}
        _trendingViewCountsFlow.value = result.toMap()
        _lastTrendingSyncTimestampFlow.value = System.currentTimeMillis()
        result
    }

    /**
     * Manually fetches the latest trending articles and view counts from Firestore
     * during a user-initiated Pull-to-Refresh on the News feed.
     */
    suspend fun fetchLatestTrendingFromFirestore(): List<com.example.data.model.NewsArticle> = withContext(Dispatchers.IO) {
        val updatedCounts = fetchTrendingViewCounts().toMutableMap()
        val cloudTrendingArticles = mutableListOf<com.example.data.model.NewsArticle>()
        try {
            val db = firestore
            if (db != null) {
                val snapshot = db.collection("news_article_views")
                    .limit(50)
                    .get()
                    .await()
                snapshot.documents.forEachIndexed { idx, doc ->
                    val id = doc.getString("articleId") ?: doc.id
                    val title = doc.getString("title").orEmpty()
                    val category = doc.getString("category")?.ifBlank { "Trending" } ?: "Trending"
                    val snippet = doc.getString("snippet") ?: doc.getString("summary").orEmpty()
                    val content = doc.getString("content") ?: snippet
                    val source = doc.getString("source") ?: "Firestore Trending Desk"
                    val count = (doc.get("viewCount") as? Number)?.toInt() ?: defaultBaselineViewCount(id)
                    updatedCounts[id] = maxOf(updatedCounts[id] ?: 0, count)
                    if (title.isNotBlank() && snippet.isNotBlank()) {
                        cloudTrendingArticles.add(
                            com.example.data.model.NewsArticle(
                                id = id,
                                title = title,
                                odiaTitle = doc.getString("odiaTitle").orEmpty(),
                                category = category,
                                timeAgo = "Trending • Firestore Live",
                                source = source,
                                snippet = snippet,
                                odiaSnippet = doc.getString("odiaSnippet").orEmpty(),
                                content = content,
                                odiaContent = doc.getString("odiaContent").orEmpty(),
                                isBookmarked = false
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("FirestoreService", "Pull-to-refresh Firestore trending sync notice: ${e.message}")
        }
        _trendingViewCountsFlow.value = updatedCounts.toMap()
        _lastTrendingSyncTimestampFlow.value = System.currentTimeMillis()
        cloudTrendingArticles.sortedByDescending { getViewCountForId(it.id, updatedCounts) }
    }

    companion object {
        private val _lastTrendingSyncTimestampFlow = kotlinx.coroutines.flow.MutableStateFlow(System.currentTimeMillis())
        val lastTrendingSyncTimestampFlow: kotlinx.coroutines.flow.StateFlow<Long> = _lastTrendingSyncTimestampFlow

        private val _trendingViewCountsFlow = kotlinx.coroutines.flow.MutableStateFlow<Map<String, Int>>(
            mapOf(
                "1" to 2840,
                "2" to 2490,
                "3" to 2150,
                "4" to 1920,
                "5" to 1780,
                "6" to 1640,
                "7101" to 2840,
                "7201" to 2490,
                "7301" to 2150,
                "7102" to 1920,
                "7202" to 1780,
                "7302" to 1640,
                "7103" to 1510,
                "7203" to 1430,
                "7303" to 1360
            )
        )
        val trendingViewCountsFlow: kotlinx.coroutines.flow.StateFlow<Map<String, Int>> = _trendingViewCountsFlow

        fun updateInMemoryViewCount(articleId: String, count: Int) {
            val next = _trendingViewCountsFlow.value.toMutableMap()
            next[articleId] = count
            _trendingViewCountsFlow.value = next.toMap()
        }

        fun getViewCountForId(articleId: String, snapshot: Map<String, Int> = _trendingViewCountsFlow.value): Int {
            return snapshot[articleId] ?: defaultBaselineViewCount(articleId)
        }

        fun defaultBaselineViewCount(articleId: String): Int {
            return when (articleId) {
                "1", "7101" -> 2840
                "2", "7201" -> 2490
                "3", "7301" -> 2150
                "4", "7102" -> 1920
                "5", "7202" -> 1780
                "6", "7302" -> 1640
                "7103" -> 1510
                "7203" -> 1430
                "7303" -> 1360
                else -> 850 + (articleId.hashCode().let { if (it < 0) -it else it } % 900)
            }
        }
    }
}

