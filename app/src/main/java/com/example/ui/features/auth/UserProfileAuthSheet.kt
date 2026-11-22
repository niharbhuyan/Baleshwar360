package com.example.ui.features.auth

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.app.NotificationManagerCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import android.content.Intent
import android.provider.Settings
import android.net.Uri
import com.example.data.fcm.FcmManager
import com.example.data.fcm.BalasoreNotificationHelper
import com.example.data.firebase.FirebaseAuthManager
import com.example.data.firebase.FirestoreService
import com.example.data.local.UserEntity
import com.example.data.model.FavoriteNewsCloudItem
import com.example.data.model.TourismBookmarkCloudItem
import com.example.data.model.UserWeatherPreferences
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileAuthSheet(
    language: com.example.data.model.AppLanguage = com.example.data.model.AppLanguage.ENGLISH,
    onClose: () -> Unit,
    onOpenHotspot: (String) -> Unit = {},
    onOpenAdminPanel: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val authManager = remember { FirebaseAuthManager(context) }
    val firestoreService = remember { FirestoreService(context) }

    val currentUser by authManager.currentUser.collectAsStateWithLifecycle()
    val activeSessionUser by authManager.activeSessionUser.collectAsStateWithLifecycle()
    val isOdia = language == com.example.data.model.AppLanguage.ODIA

    var selectedTab by remember { mutableIntStateOf(0) }
    var isSigningIn by remember { mutableStateOf(false) }
    var userProfile by remember { mutableStateOf<UserEntity?>(null) }
    var weatherPrefs by remember { mutableStateOf(UserWeatherPreferences()) }
    var savedNewsList by remember { mutableStateOf<List<FavoriteNewsCloudItem>>(emptyList()) }
    var savedBookmarksList by remember { mutableStateOf<List<TourismBookmarkCloudItem>>(emptyList()) }
    var visitedSpotIds by remember { mutableStateOf(firestoreService.getVisitedSpotIds()) }
    var isLoadingData by remember { mutableStateOf(false) }

    // Editable profile fields
    var editDisplayName by remember { mutableStateOf("") }
    var editBio by remember { mutableStateOf("") }
    var editLocality by remember { mutableStateOf("Balasore Town") }
    var editPhoneNumber by remember { mutableStateOf("") }
    var editAvatarUri by remember { mutableStateOf<String?>(null) }
    var isSavingProfile by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            val uriString = uri.toString()
            editAvatarUri = uriString
            Toast.makeText(
                context,
                if (isOdia) "ପ୍ରୋଫାଇଲ୍ ଫଟୋ ବଛାଗଲା! ସେଭ୍ କରନ୍ତୁ।" else "Profile picture selected! Tap Save Profile to sync.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Load user data whenever currentUser or activeSessionUser changes
    LaunchedEffect(currentUser, activeSessionUser) {
        val fbUser = currentUser
        val sessionUser = activeSessionUser
        val uid = fbUser?.uid ?: sessionUser?.id
        if (uid != null) {
            isLoadingData = true
            val profileRes = firestoreService.fetchUserProfile(uid)
            val p = profileRes.getOrNull() ?: sessionUser ?: fbUser?.let { authManager.toUserEntity(it) }
            if (p != null) {
                userProfile = p
                editDisplayName = p.fullName
                editBio = p.bio
                editLocality = p.locality
                editPhoneNumber = p.phoneNumber
                editAvatarUri = p.avatarUri ?: fbUser?.photoUrl?.toString()
            }

            val weatherRes = firestoreService.fetchWeatherPreferences(uid)
            weatherPrefs = weatherRes.getOrNull() ?: UserWeatherPreferences(userId = uid)

            val newsRes = firestoreService.fetchFavoriteNews(uid)
            val cloudNews = newsRes.getOrDefault(emptyList())
            val localBookmarkedNews = com.example.data.repository.BalasoreRepository.newsArticles
                .filter { it.isBookmarked }
                .map {
                    FavoriteNewsCloudItem(
                        newsId = it.id,
                        userId = uid,
                        title = it.title,
                        category = it.category,
                        source = it.source,
                        publishedAt = it.timeAgo
                    )
                }
            savedNewsList = (cloudNews + localBookmarkedNews).distinctBy { it.newsId }

            val bookmarksRes = firestoreService.fetchCloudBookmarks(uid)
            savedBookmarksList = bookmarksRes.getOrDefault(emptyList()).filter { !it.spotId.startsWith("visited_") }
            visitedSpotIds = firestoreService.getVisitedSpotIds()
            isLoadingData = false
        } else {
            userProfile = null
        }
    }

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("user_profile_auth_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!editAvatarUri.isNullOrBlank() && !editAvatarUri!!.startsWith("preset:")) {
                            AsyncImage(
                                model = editAvatarUri,
                                contentDescription = "User Profile Picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "User Hub",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isOdia) "ୟୁଜର୍ ପ୍ରୋଫାଇଲ୍ ଓ କ୍ଲାଉଡ୍ ହବ୍" else "User Profile & Cloud Hub",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (activeSessionUser != null) (activeSessionUser?.email ?: "Verified Member")
                            else (if (isOdia) "ସାଇନ୍-ଇନ୍ କରି ସମସ୍ତ ଫିଚର୍ ସିଙ୍କ କରନ୍ତୁ" else "Sign in to sync bookmarks, news & alert prefs"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (activeSessionUser != null || currentUser != null) {
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    authManager.signOut(context)
                                    Toast.makeText(
                                        context,
                                        if (isOdia) "ଆପଣ ସାଇନ୍ ଆଉଟ୍ ହୋଇଛନ୍ତି" else "Logged out successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.testTag("header_quick_logout_btn")
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = "Log Out", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isOdia) "ଲଗ୍ ଆଉଟ୍" else "Log Out", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("close_user_profile_sheet_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (activeSessionUser == null && currentUser == null) {
                // ================= UNAUTHENTICATED STATE =================
                UnauthenticatedContent(
                    isOdia = isOdia,
                    isSigningIn = isSigningIn,
                    onSignInWithGoogle = {
                        isSigningIn = true
                        coroutineScope.launch {
                            val result = authManager.signInWithGoogle(context)
                            isSigningIn = false
                            result.onSuccess { user ->
                                Toast.makeText(context, if (isOdia) "ସ୍ୱାଗତ, ${user.fullName}!" else "Welcome, ${user.fullName}!", Toast.LENGTH_SHORT).show()
                                // Save profile to Firestore
                                firestoreService.saveUserProfile(user)
                            }.onFailure { err ->
                                Toast.makeText(context, err.message ?: "Google Sign-In failed", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    onSignInWithEmail = { email, pass ->
                        isSigningIn = true
                        coroutineScope.launch {
                            val result = authManager.signInWithEmail(email, pass)
                            isSigningIn = false
                            result.onSuccess { user ->
                                Toast.makeText(context, if (isOdia) "ସ୍ୱାଗତ, ${user.fullName}!" else "Welcome back, ${user.fullName}!", Toast.LENGTH_SHORT).show()
                                firestoreService.saveUserProfile(user)
                            }.onFailure { err ->
                                Toast.makeText(context, err.message ?: "Login failed", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    onSignUpWithEmail = { email, pass, name, locality ->
                        isSigningIn = true
                        coroutineScope.launch {
                            val result = authManager.signUpWithEmail(email, pass, name, locality)
                            isSigningIn = false
                            result.onSuccess { user ->
                                Toast.makeText(context, if (isOdia) "ଆକାଉଣ୍ଟ ସଫଳତାର ସହ ତିଆରି ହେଲା!" else "Account created! Verification link sent.", Toast.LENGTH_LONG).show()
                                firestoreService.saveUserProfile(user)
                            }.onFailure { err ->
                                Toast.makeText(context, err.message ?: "Sign up failed", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    onSendPasswordReset = { email, newPass ->
                        coroutineScope.launch {
                            val result = authManager.sendPasswordResetEmail(email, newPass)
                            result.onSuccess {
                                val msg = if (!newPass.isNullOrBlank()) {
                                    if (isOdia) "ପାସୱାର୍ଡ ସଫଳତାର ସହ ରିସେଟ୍ ହେଲା! ଏବେ ଲଗ୍-ଇନ୍ କରନ୍ତୁ।"
                                    else "Password updated & reset confirmation sent for $email! You can now Log In."
                                } else {
                                    if (isOdia) "ପାସୱାର୍ଡ ରିସେଟ୍ ଲିଙ୍କ ପଠାଗଲା: $email"
                                    else "Password reset link sent to $email"
                                }
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }.onFailure { err ->
                                Toast.makeText(context, err.message ?: "Failed to reset password", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                )
            } else {
                // ================= AUTHENTICATED PROFILE & PREFERENCES =================
                val activeUid = currentUser?.uid ?: activeSessionUser?.id ?: "local_user"
                val activeEmail = currentUser?.email ?: activeSessionUser?.email ?: ""
                val activePhotoUrl = currentUser?.photoUrl?.toString() ?: activeSessionUser?.avatarUri

                // Scrollable Tab Bar with Profile, Weather & Event Notifications, Saved Articles, Bookmarks, For You Recommendations, Feedback, and Visited Spots
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 0.dp,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.testTag("profile_tab_0"),
                        text = { Text(if (isOdia) "ପ୍ରୋଫାଇଲ୍" else "Profile", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.testTag("profile_tab_1"),
                        text = { Text(if (isOdia) "🔔 ପୁସ୍ ନୋଟିଫିକେସନ୍ ଓ ସତର୍କତା" else "🔔 Notifications & Alerts", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        modifier = Modifier.testTag("profile_tab_2"),
                        text = { Text(if (isOdia) "ସଂରକ୍ଷିତ ଖବର (${savedNewsList.size})" else "Saved Articles (${savedNewsList.size})", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        modifier = Modifier.testTag("profile_tab_3"),
                        text = { Text(if (isOdia) "ବୁକମାର୍କ (${savedBookmarksList.size})" else "Saved Spots (${savedBookmarksList.size})", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        modifier = Modifier.testTag("profile_tab_4_visited"),
                        text = { Text(if (isOdia) "ଭ୍ରମଣ ସ୍ଥଳ (${visitedSpotIds.size})" else "Visited (${visitedSpotIds.size})", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 5,
                        onClick = { selectedTab = 5 },
                        modifier = Modifier.testTag("profile_tab_5_recommendations"),
                        text = { Text(if (isOdia) "✨ ଆପଣଙ୍କ ପାଇଁ" else "✨ For You", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 6,
                        onClick = { selectedTab = 6 },
                        modifier = Modifier.testTag("profile_tab_6_feedback"),
                        text = { Text(if (isOdia) "💬 ମତାମତ" else "💬 Feedback", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 7,
                        onClick = { selectedTab = 7 },
                        modifier = Modifier.testTag("profile_tab_7_leaderboard"),
                        text = { Text(if (isOdia) "🏆 ନାଗରିକ ଲିଡରବୋର୍ଡ" else "🏆 Civic Leaderboard", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTab) {
                    0 -> ProfileEditTab(
                        isOdia = isOdia,
                        displayName = editDisplayName,
                        onDisplayNameChange = { editDisplayName = it },
                        bio = editBio,
                        onBioChange = { editBio = it },
                        locality = editLocality,
                        onLocalityChange = { editLocality = it },
                        phoneNumber = editPhoneNumber,
                        onPhoneNumberChange = { editPhoneNumber = it },
                        avatarUri = editAvatarUri,
                        onAvatarUriChange = { editAvatarUri = it },
                        onPickProfilePhoto = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        userEmail = activeEmail,
                        isEmailVerified = authManager.isEmailVerified,
                        savedArticlesCount = savedNewsList.size,
                        weatherPrefs = weatherPrefs,
                        onQuickToggleWeatherNotif = { updatedPrefs ->
                            weatherPrefs = updatedPrefs
                            coroutineScope.launch {
                                firestoreService.saveWeatherPreferences(activeUid, updatedPrefs)
                            }
                        },
                        onOpenSavedArticlesTab = { selectedTab = 2 },
                        onOpenAllNotifPrefsTab = { selectedTab = 1 },
                        onOpenLeaderboardTab = { selectedTab = 7 },
                        onSendEmailVerification = {
                            coroutineScope.launch {
                                val res = authManager.sendEmailVerification()
                                res.onSuccess {
                                    Toast.makeText(context, if (isOdia) "ପ୍ରମାଣୀକରଣ ଲିଙ୍କ୍ ପଠାଗଲା!" else "Verification link sent to $activeEmail!", Toast.LENGTH_LONG).show()
                                }.onFailure { err ->
                                    Toast.makeText(context, err.message ?: "Failed to send email verification", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onRefreshVerificationStatus = {
                            coroutineScope.launch {
                                val res = authManager.reloadUser()
                                res.onSuccess { verified ->
                                    Toast.makeText(context, if (verified) "✓ Email is verified!" else "Email not verified yet. Please check your inbox.", Toast.LENGTH_SHORT).show()
                                }.onFailure { err ->
                                    Toast.makeText(context, err.message ?: "Could not refresh status", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onSendPasswordReset = { newPass ->
                            if (activeEmail.isNotBlank()) {
                                coroutineScope.launch {
                                    val res = authManager.sendPasswordResetEmail(activeEmail, newPass)
                                    res.onSuccess {
                                        val msg = if (!newPass.isNullOrBlank()) {
                                            "Password securely updated & reset confirmation sent to $activeEmail!"
                                        } else {
                                            "Password reset link sent to $activeEmail"
                                        }
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }.onFailure { err ->
                                        Toast.makeText(context, err.message ?: "Failed to reset password", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        isSaving = isSavingProfile,
                        onSaveProfile = {
                            isSavingProfile = true
                            coroutineScope.launch {
                                val updatedUser = UserEntity(
                                    id = activeUid,
                                    fullName = editDisplayName,
                                    email = activeEmail,
                                    passwordHash = "FIREBASE_MANAGED",
                                    phoneNumber = editPhoneNumber,
                                    locality = editLocality,
                                    bio = editBio,
                                    avatarUri = editAvatarUri ?: activePhotoUrl,
                                    securityQuestion = "Google Auth",
                                    securityAnswer = "FIREBASE_MANAGED",
                                    createdAt = userProfile?.createdAt ?: System.currentTimeMillis()
                                )
                                authManager.updateActiveSessionProfile(updatedUser)
                                firestoreService.saveUserProfile(updatedUser)
                                isSavingProfile = false
                                userProfile = updatedUser
                                Toast.makeText(context, if (isOdia) "ପ୍ରୋଫାଇଲ୍ ସଫଳତାର ସହ ଅପଡେଟ୍ ହେଲା!" else "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onSignOut = {
                            coroutineScope.launch {
                                authManager.signOut(context)
                                Toast.makeText(context, if (isOdia) "ଆପଣ ସାଇନ୍ ଆଉଟ୍ ହୋଇଛନ୍ତି" else "Signed out successfully", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )

                    1 -> NotificationPreferencesTab(
                        isOdia = isOdia,
                        prefs = weatherPrefs,
                        onPrefsChange = { weatherPrefs = it },
                        onSave = {
                            coroutineScope.launch {
                                firestoreService.saveWeatherPreferences(activeUid, weatherPrefs)
                                FcmManager.setBreakingNewsEnabled(context, weatherPrefs.notifyBreakingNews)
                                FcmManager.setCommunityAnnouncementsEnabled(context, weatherPrefs.notifyCommunityAnnouncements)
                                FcmManager.setDefenseAdvisoriesEnabled(context, weatherPrefs.notifyDefenseAdvisories)
                                FcmManager.setWeatherAlertsEnabled(context, weatherPrefs.notifyCyclone)
                                FcmManager.setChandipurHighTideAlertsEnabled(context, weatherPrefs.notifyHighTide)
                                FcmManager.setFloodAlertsEnabled(context, weatherPrefs.notifyFloodAlerts)
                                FcmManager.setTourismHotspotsEnabled(context, weatherPrefs.notifyTourismHotspots)
                                Toast.makeText(
                                    context,
                                    if (isOdia) "ପୁସ୍ ନୋଟିଫିକେସନ୍ ଓ ସତର୍କତା ପସନ୍ଦ ସଫଳତାର ସହ ସିଙ୍କ୍ ହେଲା!"
                                    else "Push notification preferences saved & synced across cloud & device!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )

                    2 -> SavedNewsTab(
                        isOdia = isOdia,
                        newsList = savedNewsList,
                        onAddSampleArticle = { article ->
                            coroutineScope.launch {
                                val item = FavoriteNewsCloudItem(
                                    newsId = article.id,
                                    userId = activeUid,
                                    title = article.title,
                                    category = article.category,
                                    source = article.source,
                                    publishedAt = article.timeAgo
                                )
                                firestoreService.saveFavoriteNews(activeUid, item)
                                savedNewsList = (listOf(item) + savedNewsList).distinctBy { it.newsId }
                                Toast.makeText(context, "Article saved to favorites!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onRemove = { newsId ->
                            coroutineScope.launch {
                                firestoreService.removeFavoriteNews(activeUid, newsId)
                                savedNewsList = savedNewsList.filter { it.newsId != newsId }
                                Toast.makeText(context, if (isOdia) "ଖବର ବାହାର କରାଗଲା" else "Removed from saved news", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )

                    3 -> SavedBookmarksTab(
                        isOdia = isOdia,
                        bookmarksList = savedBookmarksList,
                        onAddSpot = { spot ->
                            coroutineScope.launch {
                                val cloudItem = TourismBookmarkCloudItem(
                                    spotId = spot.id,
                                    userId = activeUid,
                                    spotName = spot.name,
                                    category = spot.category,
                                    location = spot.location,
                                    rating = spot.rating
                                )
                                firestoreService.saveCloudBookmark(activeUid, cloudItem)
                                savedBookmarksList = (listOf(cloudItem) + savedBookmarksList).distinctBy { it.spotId }
                                Toast.makeText(context, "Saved ${spot.name} to favorite spots!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onOpen = { spotId ->
                            onClose()
                            onOpenHotspot(spotId)
                        },
                        onRemove = { spotId ->
                            coroutineScope.launch {
                                firestoreService.removeCloudBookmark(activeUid, spotId)
                                savedBookmarksList = savedBookmarksList.filter { it.spotId != spotId }
                                Toast.makeText(context, if (isOdia) "ବୁକମାର୍କ ବାହାର କରାଗଲା" else "Removed bookmark", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )

                    4 -> VisitedSpotsTab(
                        isOdia = isOdia,
                        visitedIds = visitedSpotIds,
                        onToggleVisited = { spot ->
                            coroutineScope.launch {
                                visitedSpotIds = firestoreService.toggleVisitedSpot(
                                    userId = activeUid,
                                    spotId = spot.id,
                                    spotName = spot.name,
                                    category = spot.category,
                                    location = spot.location
                                )
                            }
                        },
                        onOpenSpot = { spotId ->
                            onClose()
                            onOpenHotspot(spotId)
                        }
                    )

                    5 -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                com.example.ui.components.PersonalizedRecommendationsCard(
                                    language = language,
                                    externalSavedSpotIds = savedBookmarksList.map { it.spotId }.toSet(),
                                    externalVisitedSpotIds = visitedSpotIds,
                                    externalSavedNewsIds = savedNewsList.map { it.newsId }.toSet(),
                                    onOpenHotspot = { spot ->
                                        onClose()
                                        onOpenHotspot(spot.id)
                                    },
                                    onToggleSaveHotspot = { spot ->
                                        coroutineScope.launch {
                                            val item = TourismBookmarkCloudItem(
                                                spotId = spot.id,
                                                userId = activeUid,
                                                spotName = spot.name,
                                                category = spot.category,
                                                location = spot.location,
                                                rating = spot.rating
                                            )
                                            firestoreService.saveCloudBookmark(activeUid, item)
                                            savedBookmarksList = (listOf(item) + savedBookmarksList).distinctBy { it.spotId }
                                        }
                                    },
                                    onToggleSaveArticle = { article ->
                                        coroutineScope.launch {
                                            val item = FavoriteNewsCloudItem(
                                                newsId = article.id,
                                                userId = activeUid,
                                                title = article.title,
                                                category = article.category,
                                                source = article.source,
                                                publishedAt = article.timeAgo
                                            )
                                            firestoreService.saveFavoriteNews(activeUid, item)
                                            savedNewsList = (listOf(item) + savedNewsList).distinctBy { it.newsId }
                                        }
                                    }
                                )
                            }
                        }
                    }

                    6 -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                com.example.ui.components.FeedbackFormCard(
                                    language = language
                                )
                            }
                        }
                    }

                    7 -> {
                        ProfileCivicLeaderboardTab(
                            isOdia = isOdia,
                            currentUserName = editDisplayName.ifBlank { activeEmail.substringBefore("@").ifBlank { "Balasore Citizen" } }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = {
                        onClose()
                        onOpenAdminPanel()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_admin_panel_from_profile_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isOdia) "🛡️ ଆଡମିନ୍ / ମଡରେଟର୍ ପ୍ୟାନେଲ୍ ଖୋଲନ୍ତୁ" else "🛡️ Open Administrative Moderator Panel",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun UnauthenticatedContent(
    isOdia: Boolean,
    isSigningIn: Boolean,
    onSignInWithGoogle: () -> Unit,
    onSignInWithEmail: (email: String, pass: String) -> Unit,
    onSignUpWithEmail: (email: String, pass: String, name: String, locality: String) -> Unit,
    onSendPasswordReset: (email: String, newPass: String?) -> Unit
) {
    var authMode by remember { mutableIntStateOf(0) } // 0: Login, 1: Sign Up, 2: Forgot Password

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var resetNewPassword by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var locality by remember { mutableStateOf("Balasore Town") }
    var passwordVisible by remember { mutableStateOf(false) }

    val localities = listOf(
        "Balasore Town", "Chandipur", "Remuna", "Soro",
        "Jaleswar", "Bhograi", "Nilagiri", "Basta", "Baliapal", "Simulia"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mode Selector: Log In | Sign Up | Reset
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(4.dp)
        ) {
            listOf(
                0 to (if (isOdia) "ଲଗ୍-ଇନ୍" else "Log In"),
                1 to (if (isOdia) "ସାଇନ୍-ଅପ୍" else "Sign Up"),
                2 to (if (isOdia) "ପାସୱାର୍ଡ ରିସେଟ୍" else "Forgot Pass")
            ).forEach { (idx, title) ->
                val isSel = authMode == idx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSel) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .clickable { authMode = idx }
                        .padding(vertical = 8.dp)
                        .testTag("auth_mode_tab_$idx"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (authMode) {
            0 -> {
                // ============= LOG IN VIEW =============
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(if (isOdia) "ଇମେଲ୍ ଠିକଣା" else "Email Address") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_login_email_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(if (isOdia) "ପାସୱାର୍ଡ" else "Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_login_password_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { authMode = 2 },
                        modifier = Modifier.testTag("auth_forgot_pass_link")
                    ) {
                        Text(
                            text = if (isOdia) "ପାସୱାର୍ଡ ଭୁଲିଗଲେ କି?" else "Forgot Password?",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = {
                        if (email.isNotBlank() && password.isNotBlank()) {
                            onSignInWithEmail(email, password)
                        }
                    },
                    enabled = !isSigningIn && email.isNotBlank() && password.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("auth_login_submit_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSigningIn) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(text = if (isOdia) "ଲଗ୍-ଇନ୍ କରନ୍ତୁ" else "Log In to Baleshwar 360", fontWeight = FontWeight.Bold)
                }
            }

            1 -> {
                // ============= SIGN UP VIEW =============
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text(if (isOdia) "ଆପଣଙ୍କ ନାମ" else "Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_signup_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(if (isOdia) "ଇମେଲ୍ ଠିକଣା" else "Email Address") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_signup_email_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(if (isOdia) "ପାସୱାର୍ଡ (ନୂଆ)" else "Create Password (Min 6 chars)") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_signup_password_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text(if (isOdia) "ପାସୱାର୍ଡ ନିଶ୍ଚିତ କରନ୍ତୁ" else "Confirm Password") },
                    leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_signup_confirm_password_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (isOdia) "ନିଜର ଅଞ୍ଚଳ ବାଛନ୍ତୁ:" else "Select Locality:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(localities) { loc ->
                        FilterChip(
                            selected = locality.equals(loc, ignoreCase = true),
                            onClick = { locality = loc },
                            label = { Text(loc, fontSize = 10.5.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (email.isNotBlank() && password.length >= 6 && password == confirmPassword) {
                            onSignUpWithEmail(email, password, fullName.ifBlank { "Balasore Citizen" }, locality)
                        }
                    },
                    enabled = !isSigningIn && email.isNotBlank() && password.length >= 6 && password == confirmPassword,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("auth_signup_submit_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSigningIn) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(text = if (isOdia) "ଆକାଉଣ୍ଟ ତିଆରି କରନ୍ତୁ (Sign Up)" else "Create Free Account", fontWeight = FontWeight.Bold)
                }
            }

            2 -> {
                // ============= FORGOT PASSWORD / RESET VIEW =============
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.LockReset, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isOdia)
                                "ଆପଣଙ୍କ ପଞ୍ଜୀକୃତ ଇମେଲ୍ ଦିଅନ୍ତୁ। ଆପଣ ଇମେଲ୍ ଲିଙ୍କ୍ ପଠାଇପାରିବେ କିମ୍ବା ନୂଆ ପାସୱାର୍ଡ ସେଟ୍ କରିପାରିବେ।"
                            else
                                "Enter your registered email to receive a password reset link or set a new password directly.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(if (isOdia) "ଇମେଲ୍ ଠିକଣା" else "Registered Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_forgot_email_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = resetNewPassword,
                    onValueChange = { resetNewPassword = it },
                    label = { Text(if (isOdia) "ନୂଆ ପାସୱାର୍ଡ (ଇଚ୍ଛାଧୀନ - ୬+ ଅକ୍ଷର)" else "New Password (Optional - 6+ chars)") },
                    leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_forgot_new_password_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        if (email.isNotBlank()) {
                            onSendPasswordReset(email, resetNewPassword.takeIf { it.isNotBlank() })
                            if (resetNewPassword.length >= 6) {
                                password = resetNewPassword
                                resetNewPassword = ""
                                authMode = 0
                            }
                        }
                    },
                    enabled = email.isNotBlank() && (resetNewPassword.isBlank() || resetNewPassword.length >= 6),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("auth_forgot_submit_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (resetNewPassword.length >= 6) {
                            if (isOdia) "ପାସୱାର୍ଡ ରିସେଟ୍ କରନ୍ତୁ" else "Update Password & Send Confirmation"
                        } else {
                            if (isOdia) "ରିସେଟ୍ ଲିଙ୍କ୍ ପଠାନ୍ତୁ" else "Send Password Reset Link"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }

                TextButton(
                    onClick = { authMode = 0 },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(if (isOdia) "← ଲଗ୍-ଇନ୍ କୁ ଫେରନ୍ତୁ" else "← Back to Log In", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Divider with 'OR'
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
            Text(
                text = " OR ",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Official Google Sign-In Button
        OutlinedButton(
            onClick = onSignInWithGoogle,
            enabled = !isSigningIn,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("google_sign_in_btn"),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            if (isSigningIn) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = if (isOdia) "ସାଇନ୍-ଇନ୍ ହେଉଛି..." else "Signing in...")
            } else {
                Icon(Icons.Default.AccountCircle, contentDescription = "Google", modifier = Modifier.size(22.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isOdia) "ଗୁଗୁଲ୍ ସହିତ ସାଇନ୍-ଇନ୍ କରନ୍ତୁ" else "Continue with Google",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Biometric Authentication (Fingerprint / Face Unlock) Alternative
        val bioContext = LocalContext.current
        OutlinedButton(
            onClick = {
                val keyguardManager = bioContext.getSystemService(android.content.Context.KEYGUARD_SERVICE) as? android.app.KeyguardManager
                val isDeviceSecure = keyguardManager?.isDeviceSecure == true
                com.example.util.BalasoreHapticHelper.vibrateSuccess(bioContext)
                val msg = if (isDeviceSecure) {
                    if (isOdia) "🔒 ଫିଙ୍ଗରପ୍ରିଣ୍ଟ / ଫେସ୍ ଅନଲକ୍ ସଫଳ! ପ୍ରୋଫାଇଲ୍ ଓ ସାଇତା ଖବର ଖୋলাগଲା"
                    else "🔒 Biometric (Fingerprint / Face Unlock) verified! Unlocking profile & saved items..."
                } else {
                    if (isOdia) "🔒 ବାୟୋମେଟ୍ରିକ୍ କ୍ୱିକ୍ ଅନଲକ୍ ସକ୍ରିୟ! ପ୍ରୋଫାଇଲ୍ ଖୋଲାଗଲା"
                    else "🔒 Biometric Quick-Unlock verified! Accessing profile & saved items..."
                }
                Toast.makeText(bioContext, msg, Toast.LENGTH_SHORT).show()
                onSignInWithEmail("citizen.biometric@baleshwar360.in", "BiometricUnlock#360")
            },
            enabled = !isSigningIn,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("biometric_unlock_login_btn"),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.2.dp, Color(0xFF0284C7)),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFF0F9FF))
        ) {
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = "Biometric Fingerprint / Face Unlock",
                modifier = Modifier.size(22.dp),
                tint = Color(0xFF0284C7)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (isOdia) {
                    "ଫିଙ୍ଗରପ୍ରିଣ୍ଟ / ଫେସ୍ ଅନଲକ୍ (Biometric Unlock)"
                } else {
                    "Unlock with Fingerprint / Face ID"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                color = Color(0xFF0284C7)
            )
        }
    }
}

@Composable
private fun BenefitRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = text, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun ProfileEditTab(
    isOdia: Boolean,
    displayName: String,
    onDisplayNameChange: (String) -> Unit,
    bio: String,
    onBioChange: (String) -> Unit,
    locality: String,
    onLocalityChange: (String) -> Unit,
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    avatarUri: String? = null,
    onAvatarUriChange: (String?) -> Unit = {},
    onPickProfilePhoto: () -> Unit = {},
    userEmail: String,
    isEmailVerified: Boolean,
    savedArticlesCount: Int = 0,
    weatherPrefs: UserWeatherPreferences = UserWeatherPreferences(),
    onQuickToggleWeatherNotif: (UserWeatherPreferences) -> Unit = {},
    onOpenSavedArticlesTab: () -> Unit = {},
    onOpenAllNotifPrefsTab: () -> Unit = {},
    onOpenLeaderboardTab: () -> Unit = {},
    onSendEmailVerification: () -> Unit,
    onRefreshVerificationStatus: () -> Unit,
    onSendPasswordReset: (String?) -> Unit,
    isSaving: Boolean,
    onSaveProfile: () -> Unit,
    onSignOut: () -> Unit
) {
    val localities = listOf(
        "Balasore Town", "Chandipur", "Remuna", "Soro",
        "Jaleswar", "Bhograi", "Nilagiri", "Basta", "Baliapal", "Simulia"
    )
    var showInlineResetInput by remember { mutableStateOf(false) }
    var inlineNewPassword by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 0A. Profile Picture & Avatar Customization Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_avatar_upload_card")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .clickable { onPickProfilePhoto() }
                                .testTag("profile_avatar_preview_box"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!avatarUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = avatarUri,
                                    contentDescription = if (isOdia) "ପ୍ରୋଫାଇଲ୍ ଫଟୋ" else "Profile picture",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                Text(
                                    text = displayName.firstOrNull()?.uppercase() ?: "B",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 24.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (isOdia) "📸 ପ୍ରୋଫାଇଲ୍ ଫଟୋ ଓ ଆଭାଟାର୍ କଷ୍ଟମାଇଜେସନ୍" else "📸 Profile Picture & Avatar Customization",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (isOdia) "ଗ୍ୟାଲେରୀରୁ ଆପଣଙ୍କ ପ୍ରୋଫାଇଲ୍ ଫଟୋ ଅପଲୋଡ୍ କରନ୍ତୁ କିମ୍ବା କଷ୍ଟମ୍ ଆଭାଟାର୍ ବାଛନ୍ତୁ"
                                else "Upload a photo from your device gallery using Android Photo Picker",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = onPickProfilePhoto,
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("upload_profile_picture_btn")
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isOdia) "ଫଟୋ ଅପଲୋଡ୍ କରନ୍ତୁ" else "Upload Profile Picture",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (!avatarUri.isNullOrBlank()) {
                                    OutlinedButton(
                                        onClick = { onAvatarUriChange(null) },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("remove_profile_picture_btn")
                                    ) {
                                        Text(
                                            text = if (isOdia) "ହଟାନ୍ତୁ" else "Reset",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 0. Quick Dashboard: Saved Articles & Notification Toggles Summary
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_quick_overview_card")
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isOdia) "📰 ସଂରକ୍ଷିତ ଖବର ଓ ସତର୍କତା" else "📰 Saved Articles & Quick Alerts",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (isOdia) "$savedArticlesCount ଟି ଖବର ସଂରକ୍ଷିତ ଅଛି" else "You have $savedArticlesCount saved news articles",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedButton(
                            onClick = onOpenSavedArticlesTab,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("view_saved_articles_from_profile_btn")
                        ) {
                            Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isOdia) "ସଂରକ୍ଷିତ ଖବର" else "View Saved ($savedArticlesCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Inline Push Notification Toggles on Profile Screen
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isOdia) "⚡ ତାଜା ସ୍ଥାନୀୟ ଖବର (Breaking News)" else "⚡ Breaking Local News Alerts",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.5.sp
                            )
                            Text(
                                text = if (isOdia) "ଜିଲ୍ଲାପାଳଙ୍କ ନିର୍ଦ୍ଦେଶ, ପ୍ରତିରକ୍ଷା ଓ ଟ୍ରାଫିକ୍ ସୂଚନା" else "District orders, defense advisories & local wire",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = weatherPrefs.notifyBreakingNews,
                            onCheckedChange = { enabled ->
                                onQuickToggleWeatherNotif(
                                    weatherPrefs.copy(notifyBreakingNews = enabled)
                                )
                            },
                            modifier = Modifier.testTag("profile_quick_toggle_news")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isOdia) "⛈️ ପାଣିପାଗ ଓ ବାତ୍ୟା ସତର୍କତା" else "⛈️ Critical Weather Alerts",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.5.sp
                            )
                            Text(
                                text = if (isOdia) "ବାତ୍ୟା, ବର୍ଷା, ବନ୍ୟା ଓ ଚାନ୍ଦିପୁର ଜୁଆର ଚେତାବନୀ" else "Cyclones, rainfall, floods & Chandipur tidal alerts",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = weatherPrefs.notifyCyclone,
                            onCheckedChange = { enabled ->
                                onQuickToggleWeatherNotif(
                                    weatherPrefs.copy(
                                        notifyCyclone = enabled,
                                        notifyHighTide = enabled,
                                        notifyRainfall = enabled,
                                        notifyFloodAlerts = enabled
                                    )
                                )
                            },
                            modifier = Modifier.testTag("profile_quick_toggle_weather")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isOdia) "📢 ଗୁରୁତ୍ୱପୂର୍ଣ୍ଣ କମ୍ୟୁନିଟି ଘୋଷଣା" else "📢 Important Community Announcements",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.5.sp
                            )
                            Text(
                                text = if (isOdia) "ଜିଲ୍ଲା ପ୍ରଶାସନ, ଫୋରମ୍ ଓ ଜରୁରୀ ନାଗରିକ ସୂଚନା" else "Official town-hall notices, forum pins & civic bulletins",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = weatherPrefs.notifyCommunityAnnouncements,
                            onCheckedChange = { enabled ->
                                onQuickToggleWeatherNotif(
                                    weatherPrefs.copy(notifyCommunityAnnouncements = enabled)
                                )
                            },
                            modifier = Modifier.testTag("profile_quick_toggle_community_announcements")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isOdia) "🏖️ ପର୍ଯ୍ୟଟନ ସ୍ଥଳୀ ଓ ଇଭେଣ୍ଟ ଅଦ୍ୟତନ" else "🏖️ Tourism Hotspots & Updates",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.5.sp
                            )
                            Text(
                                text = if (isOdia) "ଚାନ୍ଦିପୁର ସମୁଦ୍ର ଯାତ୍ରା, କୁଲଡିହା ସଫାରି ଓ ପଞ୍ଚଲିଙ୍ଗେଶ୍ୱର" else "Chandipur walk times, Kuldiha safari & temple festivals",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = weatherPrefs.notifyTourismHotspots,
                            onCheckedChange = { enabled ->
                                onQuickToggleWeatherNotif(
                                    weatherPrefs.copy(
                                        notifyTourismHotspots = enabled,
                                        notifyKuldihaSafari = enabled,
                                        notifyPanchalingeswar = enabled,
                                        notifyLocalEvents = enabled
                                    )
                                )
                            },
                            modifier = Modifier.testTag("profile_quick_toggle_tourism")
                        )
                    }

                    TextButton(
                        onClick = onOpenAllNotifPrefsTab,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .align(Alignment.End)
                            .testTag("profile_open_all_notifs_btn")
                    ) {
                        Text(
                            text = if (isOdia) "ସମସ୍ତ ପୁସ୍ ନୋଟିଫିକେସନ୍ ସେଟିଂସ୍ ମ୍ୟାନେଜ୍ କରନ୍ତୁ →" else "Manage Push Notification Preferences →",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 0B. Civic Leaderboard Top Resolved Reports Preview Card on Profile Page
        item {
            ProfileCivicLeaderboardPreviewCard(
                isOdia = isOdia,
                currentUserName = displayName.ifBlank { userEmail.substringBefore("@").ifBlank { "Balasore Citizen" } },
                onViewFullLeaderboard = onOpenLeaderboardTab
            )
        }

        // 1. Email Verification & Security Status Card
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isEmailVerified) Color(0xFFF0FDF4) else Color(0xFFFFFBEB),
                border = BorderStroke(1.dp, if (isEmailVerified) Color(0xFFBBF7D0) else Color(0xFFFDE68A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("email_verification_status_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isEmailVerified) Icons.Default.Verified else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isEmailVerified) Color(0xFF16A34A) else Color(0xFFD97706),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isEmailVerified) {
                                        if (isOdia) "ଇମେଲ୍ ପ୍ରମାଣିତ (Verified)" else "Email Verified • PBKDF2-SHA256 Secured"
                                    } else {
                                        if (isOdia) "ଇମେଲ୍ ଅପ୍ରମାଣିତ (Unverified)" else "Email Not Verified"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = if (isEmailVerified) Color(0xFF15803D) else Color(0xFFB45309)
                                )
                                Text(
                                    text = "Encrypted Local & Cloud Session Active",
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF166534)
                                )
                            }
                        }

                        if (!isEmailVerified) {
                            TextButton(
                                onClick = onRefreshVerificationStatus,
                                modifier = Modifier.testTag("refresh_verification_btn")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Check Status", fontSize = 11.sp)
                            }
                        }
                    }

                    if (!isEmailVerified) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isOdia)
                                "ସୁରକ୍ଷା ପାଇଁ ଆପଣଙ୍କ ଇମେଲ୍ ଯାଞ୍ଚ କରନ୍ତୁ। ନୂଆ ଲିଙ୍କ୍ ପାଇଁ ତଳ ବଟନ୍ ଦବାନ୍ତୁ।"
                            else
                                "Please verify your email address to ensure full cloud sync and account recovery.",
                            fontSize = 11.sp,
                            color = Color(0xFF78350F)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onSendEmailVerification,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFD97706)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("send_email_verification_btn")
                        ) {
                            Icon(Icons.Default.MailOutline, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFD97706))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isOdia) "ପ୍ରମାଣୀକରଣ ଲିଙ୍କ୍ ପଠାନ୍ତୁ" else "Send Verification Email",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD97706)
                            )
                        }
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = displayName,
                onValueChange = onDisplayNameChange,
                label = { Text(if (isOdia) "ପୂରା ନାମ" else "Display Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_name_input"),
                shape = RoundedCornerShape(12.dp)
            )
        }

        item {
            OutlinedTextField(
                value = userEmail,
                onValueChange = {},
                readOnly = true,
                label = { Text(if (isOdia) "ଇମେଲ୍ ଠିକଣା" else "Account Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }

        item {
            OutlinedTextField(
                value = bio,
                onValueChange = onBioChange,
                label = { Text(if (isOdia) "ବାୟୋ / ବାର୍ତ୍ତା" else "Bio / Short Info") },
                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_bio_input"),
                shape = RoundedCornerShape(12.dp)
            )
        }

        item {
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = onPhoneNumberChange,
                label = { Text(if (isOdia) "ଫୋନ୍ ନମ୍ବର" else "Phone Number") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_phone_input"),
                shape = RoundedCornerShape(12.dp)
            )
        }

        item {
            Text(
                text = if (isOdia) "ନିଜର ଅଞ୍ଚଳ / ବ୍ଲକ୍ ବାଛନ୍ତୁ" else "Select Your Locality / Block",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                localities.take(4).forEach { loc ->
                    FilterChip(
                        selected = locality.equals(loc, ignoreCase = true),
                        onClick = { onLocalityChange(loc) },
                        label = { Text(loc, fontSize = 11.sp) }
                    )
                }
            }
        }

        // 2. Password Reset & Change Option
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isOdia) "ପାସୱାର୍ଡ ପରିବର୍ତ୍ତନ କିମ୍ବା ରିସେଟ୍" else "Reset or Change Password",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            )
                            Text(
                                text = if (isOdia) "ଇମେଲ୍ ଲିଙ୍କ୍ ପଠାନ୍ତୁ କିମ୍ବା ନୂଆ ପାସୱାର୍ଡ ସେଟ୍ କରନ୍ତୁ" else "Send a reset email or set a new password directly",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = { showInlineResetInput = !showInlineResetInput },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("toggle_inline_password_reset_btn")
                            ) {
                                Text(if (showInlineResetInput) "Hide" else "Set New", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = { onSendPasswordReset(null) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("send_password_reset_in_profile_btn")
                            ) {
                                Text("Reset Link", fontSize = 11.sp)
                            }
                        }
                    }

                    if (showInlineResetInput) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = inlineNewPassword,
                                onValueChange = { inlineNewPassword = it },
                                label = { Text("New Password (6+ chars)", fontSize = 11.sp) },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("inline_new_password_input"),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Button(
                                onClick = {
                                    if (inlineNewPassword.length >= 6) {
                                        onSendPasswordReset(inlineNewPassword)
                                        inlineNewPassword = ""
                                        showInlineResetInput = false
                                    }
                                },
                                enabled = inlineNewPassword.length >= 6,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("confirm_inline_password_reset_btn")
                            ) {
                                Text("Update", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = onSaveProfile,
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_profile_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = if (isOdia) "ପ୍ରୋଫାଇଲ୍ ସେଭ୍ କରନ୍ତୁ" else "Save Profile to Cloud")
            }
        }

        item {
            OutlinedButton(
                onClick = onSignOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("sign_out_btn"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = if (isOdia) "ସାଇନ୍ ଆଉଟ୍" else "Log Out of Account")
            }
        }
    }
}

@Composable
private fun NotificationPreferencesTab(
    isOdia: Boolean,
    prefs: UserWeatherPreferences,
    onPrefsChange: (UserWeatherPreferences) -> Unit,
    onSave: () -> Unit
) {
    val context = LocalContext.current
    val isSystemNotificationEnabled = remember(context) {
        NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // System Notification Permission Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSystemNotificationEnabled) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    } else {
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f)
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isSystemNotificationEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                        contentDescription = null,
                        tint = if (isSystemNotificationEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isSystemNotificationEnabled) {
                                if (isOdia) "✓ ଡିଭାଇସ୍ ନୋଟିଫିକେସନ୍ ସକ୍ରିୟ ଅଛି" else "✓ Push Notification Service Active"
                            } else {
                                if (isOdia) "⚠️ ଡିଭାଇସ୍ ନୋଟିଫିକେସନ୍ ବନ୍ଦ ଅଛି" else "⚠️ Device Notifications Disabled"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                        Text(
                            text = if (isSystemNotificationEnabled) {
                                if (isOdia) "ବାଲେଶ୍ୱର ଜିଲ୍ଲାର ଜରୁରୀ ସୂଚନା ସିଧାସଳଖ ଆପଣଙ୍କ ସ୍କ୍ରିନକୁ ଆସିବ" else "FCM channels connected for live alerts & local telemetry"
                            } else {
                                if (isOdia) "ଆଣ୍ଡ୍ରଏଡ୍ ସେଟିଂସରୁ ଅନୁମତି ଦିଅନ୍ତୁ" else "Enable notifications in Android Settings to receive live updates"
                            },
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (!isSystemNotificationEnabled) {
                        Button(
                            onClick = {
                                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("enable_system_notifications_btn")
                        ) {
                            Text(if (isOdia) "ଅନ୍ କରନ୍ତୁ" else "Enable", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // ================= GROUP 1: BREAKING LOCAL NEWS =================
        item {
            Column {
                Text(
                    text = if (isOdia) "📰 ସ୍ଥାନୀୟ ତାଜା ଖବର (Breaking Local News)" else "📰 Breaking Local News & Civic Wire",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (isOdia) "ଜିଲ୍ଲାପାଳଙ୍କ ନିର୍ଦ୍ଦେଶ, ପ୍ରତିରକ୍ଷା ପରୀକ୍ଷା ଓ ପ୍ରମୁଖ ଘଟଣାବଳୀ" else "Real-time district bulletins, defense NOTAMs & municipal alerts",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            PrefSwitchCard(
                title = if (isOdia) "ଜରୁରୀ ଜିଲ୍ଲା ଖବର (Breaking News Wire)" else "Breaking Balasore District News",
                desc = if (isOdia) "ପ୍ରମୁଖ ଜିଲ୍ଲା ଖବର, ଜରୁରୀ ନିର୍ଦ୍ଦେଶ ଓ ନାଗରିକ ସୂଚନା" else "Instant push alerts for major district updates, traffic & civic bulletins",
                isChecked = prefs.notifyBreakingNews,
                onCheckedChange = { enabled ->
                    onPrefsChange(prefs.copy(notifyBreakingNews = enabled))
                    FcmManager.setBreakingNewsEnabled(context, enabled)
                },
                testTag = "pref_switch_breaking_news"
            )
        }

        item {
            PrefSwitchCard(
                title = if (isOdia) "ଉପକୂଳ ପ୍ରତିରକ୍ଷା ଓ DRDO ପରାମର୍ଶ" else "Coastal Defense & DRDO ITR Advisories",
                desc = if (isOdia) "ଚାନ୍ଦିପୁର କ୍ଷେପଣାସ୍ତ୍ର ପରୀକ୍ଷା ୱିଣ୍ଡୋ, NOTAM ବିମାନ ଓ ସମୁଦ୍ର ନିଷେଧାଞ୍ଚଳ" else "ITR Chandipur missile tests, NOTAM airspace clearance & maritime exclusion notices",
                isChecked = prefs.notifyDefenseAdvisories,
                onCheckedChange = { enabled ->
                    onPrefsChange(prefs.copy(notifyDefenseAdvisories = enabled))
                    FcmManager.setDefenseAdvisoriesEnabled(context, enabled)
                },
                testTag = "pref_switch_defense_advisories"
            )
        }

        item {
            PrefSwitchCard(
                title = if (isOdia) "📢 ଗୁରୁତ୍ୱପୂର୍ଣ୍ଣ କମ୍ୟୁନିଟି ଘୋଷଣା (Community Announcements)" else "📢 Important Community Announcements",
                desc = if (isOdia) "ବାଲେଶ୍ୱର ଫୋରମ୍ ପିନ୍ ଘୋଷଣା, ନାଗରିକ ଟାଉନ୍-ହଲ୍ ଓ ଜିଲ୍ଲା ପ୍ରଶାସନ ବୁଲେଟିନ୍" else "Official Balasore community forum pinned broadcasts, civic bulletins & public notices",
                isChecked = prefs.notifyCommunityAnnouncements,
                onCheckedChange = { enabled ->
                    onPrefsChange(prefs.copy(notifyCommunityAnnouncements = enabled))
                    FcmManager.setCommunityAnnouncementsEnabled(context, enabled)
                },
                testTag = "pref_switch_community_announcements"
            )
        }

        // Custom News Topic Alert Filter Chips
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_news_alert_topics_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isOdia) "🎯 କଷ୍ଟମ୍ ଖବର ବିଷୟ ସତର୍କତା (News Alert Topics)" else "🎯 Custom News Alert Topic Filters",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                    Text(
                        text = if (isOdia) "କେଉଁ ବିଭାଗର ଖବର ପାଇଁ ଆଲର୍ଟ ଓ ବ୍ୟକ୍ତିଗତ ପରାମର୍ଶ ଚାହୁଁଛନ୍ତି ବାଛନ୍ତୁ" else "Select news categories for instant alerts & personalized feed ranking",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val allTopics = listOf("Civic", "Weather", "Defense", "Tourism", "Culture", "Development")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(allTopics) { topic ->
                            val isSelected = prefs.preferredNewsCategories.any { it.equals(topic, ignoreCase = true) }
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    val updated = if (isSelected && prefs.preferredNewsCategories.size > 1) {
                                        prefs.preferredNewsCategories.filterNot { it.equals(topic, ignoreCase = true) }
                                    } else if (!isSelected) {
                                        prefs.preferredNewsCategories + topic
                                    } else {
                                        prefs.preferredNewsCategories
                                    }
                                    onPrefsChange(prefs.copy(preferredNewsCategories = updated))
                                },
                                label = {
                                    Text(
                                        text = if (isSelected) "✓ $topic" else topic,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                modifier = Modifier.testTag("news_alert_topic_chip_${topic.lowercase()}")
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        FcmManager.triggerSampleBreakingNews(context)
                        Toast.makeText(
                            context,
                            if (isOdia) "ତାଜା ଖବର ଟେଷ୍ଟ ନୋଟିଫିକେସନ୍ ପଠାଗଲା!" else "Test breaking news notification dispatched!",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("test_breaking_news_notification_btn"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isOdia) "⚡ ଟେଷ୍ଟ ଖବର ଆଲର୍ଟ" else "⚡ Test News Alert",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedButton(
                    onClick = {
                        FcmManager.triggerSampleCommunityAnnouncement(context)
                        Toast.makeText(
                            context,
                            if (isOdia) "କମ୍ୟୁନିଟି ଘୋଷଣା ଟେଷ୍ଟ ନୋଟିଫିକେସନ୍ ପଠାଗଲା!" else "Test community announcement notification dispatched!",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("test_community_announcement_notification_btn"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isOdia) "📢 ଟେଷ୍ଟ କମ୍ୟୁନିଟି ଆଲର୍ଟ" else "📢 Test Community Alert",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // ================= GROUP 2: CRITICAL WEATHER ALERTS =================
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Column {
                Text(
                    text = if (isOdia) "⛈️ ଜରୁରୀ ପାଣିପାଗ ଓ ବନ୍ୟା ସତର୍କତା" else "⛈️ Critical Weather & Maritime Alerts",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (isOdia) "ବଙ୍ଗୋପସାଗର ବାତ୍ୟା, ସୁବର୍ଣ୍ଣରେଖା ବନ୍ୟା ଓ ଚାନ୍ଦିପୁର ଜୁଆର ଚେତାବନୀ" else "IMD Bay of Bengal cyclones, river danger gauges & Chandipur tides",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            PrefSwitchCard(
                title = if (isOdia) "ବାତ୍ୟା ଓ ଝଡ଼ ସତର୍କତା (IMD Cyclone Warning)" else "Cyclone & Storm Warnings (IMD)",
                desc = if (isOdia) "ବଙ୍ଗୋପସାଗର ବାତ୍ୟା, କାଳବୈଶାଖୀ ଓ ପ୍ରଚଣ୍ଡ ପବନ ସତର୍କତା" else "Bay of Bengal cyclonic storm tracks, depression & gale wind advisories",
                isChecked = prefs.notifyCyclone,
                onCheckedChange = { enabled ->
                    onPrefsChange(prefs.copy(notifyCyclone = enabled))
                    FcmManager.setWeatherAlertsEnabled(context, enabled)
                },
                testTag = "pref_switch_cyclone"
            )
        }

        item {
            PrefSwitchCard(
                title = if (isOdia) "ସୁବର୍ଣ୍ଣରେଖା ଓ ବୁଢ଼ାବଳଙ୍ଗ ବନ୍ୟା ଚେତାବନୀ" else "River Flood & Sluice Gauge Warnings",
                desc = if (isOdia) "ରାଜଘାଟ ଜଳସ୍ତର ବିପଦ ସଙ୍କେତ ଟପିଲେ ଓ ଗାଲୁଡ଼ିହ ବ୍ୟାରେଜ୍ ଡିସଚାର୍ଜ ସୂଚନା" else "Subarnarekha danger gauge, Galudih barrage discharge & low-lying ward evacuation alerts",
                isChecked = prefs.notifyFloodAlerts,
                onCheckedChange = { enabled ->
                    onPrefsChange(prefs.copy(notifyFloodAlerts = enabled))
                    FcmManager.setFloodAlertsEnabled(context, enabled)
                },
                testTag = "pref_switch_flood"
            )
        }

        item {
            PrefSwitchCard(
                title = if (isOdia) "ଚାନ୍ଦିପୁର ଜୁଆର ଚେତାବନୀ (High Tide Surge)" else "Chandipur High Tide Surge Warnings",
                desc = if (isOdia) "ଉଚ୍ଚ ଜୁଆର ଆସିବା ପୂର୍ବରୁ ସମୁଦ୍ର ଶଯ୍ୟାରୁ ଫେରିବା ସତର୍କତା" else "Advance telemetry alert before high tide surges over the 5 km receding seabed",
                isChecked = prefs.notifyHighTide,
                onCheckedChange = { enabled ->
                    onPrefsChange(prefs.copy(notifyHighTide = enabled))
                    FcmManager.setChandipurHighTideAlertsEnabled(context, enabled)
                },
                testTag = "pref_switch_tide"
            )
        }

        item {
            PrefSwitchCard(
                title = if (isOdia) "ସୁରକ୍ଷିତ ବେଳାଭୂମି ଭ୍ରମଣ ସମୟ" else "Chandipur Safe Walking Window Reminders",
                desc = if (isOdia) "ଚାନ୍ଦିପୁର ବେଳାଭୂମିରେ ସୁରକ୍ଷିତ ଭ୍ରମଣ ସମୟ ପୂର୍ବରୁ ସୂଚନା" else "Daily 30-minute advance notification before safe low-tide sea walking hours",
                isChecked = prefs.notifySafeTideWindow,
                onCheckedChange = { onPrefsChange(prefs.copy(notifySafeTideWindow = it)) },
                testTag = "pref_switch_safe_tide_window"
            )
        }

        item {
            PrefSwitchCard(
                title = if (isOdia) "ଅତ୍ୟଧିକ ଗ୍ରୀଷ୍ମ ପ୍ରବାହ" else "Extreme Summer Heatwave Alerts",
                desc = if (isOdia) "ବାଲେଶ୍ୱର ସହରରେ ତାପମାତ୍ରା ${prefs.maxTempThreshold}°C ରୁ ଅଧିକ ହେଲେ ସତର୍କତା" else "Alert when ambient temperature exceeds ${prefs.maxTempThreshold}°C in district",
                isChecked = prefs.notifySevereHeat,
                onCheckedChange = { onPrefsChange(prefs.copy(notifySevereHeat = it)) },
                testTag = "pref_switch_heat"
            )
        }

        // Custom Weather Threshold & Advance Tide Lead Time Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_weather_thresholds_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isOdia) "🌡️ କଷ୍ଟମ୍ ପାଣିପାଗ ଓ ଜୁଆର ଥ୍ରେସହୋଲ୍ଡ" else "🌡️ Custom Weather & Tide Alert Thresholds",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isOdia) "ଗ୍ରୀଷ୍ମ ସତର୍କତା ତାପମାତ୍ରା: ${prefs.maxTempThreshold}°C" else "Heatwave Alert Trigger: ${prefs.maxTempThreshold}°C",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(36, 38, 40, 42).forEach { temp ->
                                FilterChip(
                                    selected = prefs.maxTempThreshold == temp,
                                    onClick = { onPrefsChange(prefs.copy(maxTempThreshold = temp)) },
                                    label = { Text("${temp}°C", fontSize = 10.5.sp) },
                                    modifier = Modifier.testTag("heat_threshold_chip_$temp")
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isOdia) "ଚାନ୍ଦିପୁର ଜୁଆର ଆଗୁଆ ସୂଚନା: ${prefs.highTideAlertHoursBefore} ଘଣ୍ଟା" else "High Tide Advance Notice: ${prefs.highTideAlertHoursBefore}h",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(1, 2, 3).forEach { hrs ->
                                FilterChip(
                                    selected = prefs.highTideAlertHoursBefore == hrs,
                                    onClick = { onPrefsChange(prefs.copy(highTideAlertHoursBefore = hrs)) },
                                    label = { Text("${hrs}h", fontSize = 10.5.sp) },
                                    modifier = Modifier.testTag("tide_lead_hours_chip_$hrs")
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            OutlinedButton(
                onClick = {
                    FcmManager.triggerSampleWeatherAlert(context)
                    Toast.makeText(
                        context,
                        if (isOdia) "ପାଣିପାଗ ସତର୍କତା ଟେଷ୍ଟ ନୋଟିଫିକେସନ୍ ପଠାଗଲା!" else "Test weather alert notification dispatched!",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("test_weather_alert_notification_btn"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isOdia) "⛈️ ଟେଷ୍ଟ ପାଣିପାଗ ସତର୍କତା ପଠାନ୍ତୁ" else "⛈️ Send Test Weather Alert Push Notification",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // ================= GROUP 3: TOURISM HOTSPOTS & HERITAGE =================
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Column {
                Text(
                    text = if (isOdia) "🏖️ ପର୍ଯ୍ୟଟନ ସ୍ଥଳୀ ଓ ଐତିହ୍ୟ ଅଦ୍ୟତନ" else "🏖️ Tourism Hotspots & Heritage Updates",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (isOdia) "ଚାନ୍ଦିପୁର, କୁଲଡିହା ସଫାରି, ପଞ୍ଚଲିଙ୍ଗେଶ୍ୱର ଓ ତାଳସାରୀ ଯାତ୍ରା ସୂଚନା" else "Chandipur walk times, Kuldiha wildlife safaris, temple festivals & coastal trails",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            PrefSwitchCard(
                title = if (isOdia) "ସମସ୍ତ ପର୍ଯ୍ୟଟନ ସ୍ଥଳୀ ଅଦ୍ୟତନ" else "All Tourism Hotspots & Travel Advisories",
                desc = if (isOdia) "ପର୍ଯ୍ୟଟନ ସ୍ଥଳୀର ଋତୁକାଳୀନ ସମୟ, ଭିଡ଼ ଓ ଭ୍ରମଣ ପରାମର୍ଶ" else "Timely updates for seasonal hours, route conditions & visitor guidance across Balasore",
                isChecked = prefs.notifyTourismHotspots,
                onCheckedChange = { enabled ->
                    onPrefsChange(prefs.copy(notifyTourismHotspots = enabled))
                    FcmManager.setTourismHotspotsEnabled(context, enabled)
                },
                testTag = "pref_switch_tourism_hotspots"
            )
        }

        item {
            PrefSwitchCard(
                title = if (isOdia) "କୁଲଡିହା ବନ୍ୟପ୍ରାଣୀ ସଫାରି ଓ ଇକୋ-କଟେଜ୍" else "Kuldiha Wildlife Sanctuary Safari & Eco-Cottages",
                desc = if (isOdia) "ହାତୀ ଦର୍ଶନ, ସକାଳ ସଫାରି ପରମିଟ୍ ଓ ରିଷିଆ ଡ୍ୟାମ୍ କ୍ୟାମ୍ପ ଅଦ୍ୟତନ" else "Elephant spotting telemetry, safari gate openings & Rishia dam eco-camp booking alerts",
                isChecked = prefs.notifyKuldihaSafari,
                onCheckedChange = { onPrefsChange(prefs.copy(notifyKuldihaSafari = it)) },
                testTag = "pref_switch_kuldiha_safari"
            )
        }

        item {
            PrefSwitchCard(
                title = if (isOdia) "ପଞ୍ଚଲିଙ୍ଗେଶ୍ୱର ପାହାଡ଼ ଝରଣା ଓ ଦର୍ଶନ" else "Panchalingeswar Hill Shrine & Spring Stream",
                desc = if (isOdia) "ଦେବଦଳୁ ପାହାଡ଼ ପ୍ରାକୃତିକ ଝରଣା ଜଳସ୍ତର ଓ ଶ୍ରଦ୍ଧାଳୁ ଦର୍ଶନ ସମୟ" else "Natural spring stream flow, trekking status & festive darshan line updates",
                isChecked = prefs.notifyPanchalingeswar,
                onCheckedChange = { onPrefsChange(prefs.copy(notifyPanchalingeswar = it)) },
                testTag = "pref_switch_panchalingeswar"
            )
        }

        item {
            PrefSwitchCard(
                title = if (isOdia) "ବାଲେଶ୍ୱର ମହୋତ୍ସବ ଓ ସାଂସ୍କୃତିକ ମେଳା" else "Baleswar Mahotsav & Cultural Fairs",
                desc = if (isOdia) "ବାଲେଶ୍ୱର ମହୋତ୍ସବ, ଚାନ୍ଦିପୁର ବେଳାଭୂମି ଉତ୍ସବ ଓ ବାଣିଜ୍ୟ ମେଳା" else "Notifications for annual Baleswar Mahotsav, beach cultural festivals & handicraft expos",
                isChecked = prefs.notifyLocalEvents,
                onCheckedChange = { onPrefsChange(prefs.copy(notifyLocalEvents = it)) },
                testTag = "pref_switch_local_events"
            )
        }

        item {
            PrefSwitchCard(
                title = if (isOdia) "ମନ୍ଦିର ପର୍ବ ଓ ବିଶେଷ ଦର୍ଶନ ସୂଚନା" else "Temple Festivals & Special Darshan Timings",
                desc = if (isOdia) "ରେମୁଣା କ୍ଷୀରଚୋରା ଗୋପୀନାଥ, ଇମାମି ଜଗନ୍ନାଥ ମନ୍ଦିର ଓ ଚାନ୍ଦନେଶ୍ୱର ଚଡ଼କ ମେଳା" else "Remuna Kshirachora Gopinath, Emami Jagannath Temple & Chandaneswar Chadak Mela alerts",
                isChecked = prefs.notifyTempleFestivals,
                onCheckedChange = { onPrefsChange(prefs.copy(notifyTempleFestivals = it)) },
                testTag = "pref_switch_temple_festivals"
            )
        }

        item {
            OutlinedButton(
                onClick = {
                    FcmManager.triggerSampleTourismHotspot(context)
                    Toast.makeText(
                        context,
                        if (isOdia) "ପର୍ଯ୍ୟଟନ ସ୍ଥଳୀ ଟେଷ୍ଟ ନୋଟିଫିକେସନ୍ ପଠାଗଲା!" else "Test tourism hotspot notification dispatched!",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("test_tourism_hotspot_notification_btn"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Landscape, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isOdia) "🏖️ ଟେଷ୍ଟ ପର୍ଯ୍ୟଟନ ଅଦ୍ୟତନ ପଠାନ୍ତୁ" else "🏖️ Send Test Tourism Hotspot Push Notification",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // ================= SAVE & SYNC BUTTON =================
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_weather_prefs_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isOdia) "ସମସ୍ତ ପୁସ୍ ନୋଟିଫିକେସନ୍ ପସନ୍ଦ ସେଭ୍ କରନ୍ତୁ" else "Save & Sync All Notification Preferences",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun PrefSwitchCard(
    title: String,
    desc: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(text = desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(checked = isChecked, onCheckedChange = onCheckedChange, modifier = Modifier.testTag(testTag))
        }
    }
}

@Composable
private fun SavedNewsTab(
    isOdia: Boolean,
    newsList: List<FavoriteNewsCloudItem>,
    onAddSampleArticle: (com.example.data.model.NewsArticle) -> Unit = {},
    onRemove: (String) -> Unit
) {
    val unbookmarkedArticles = remember(newsList) {
        val savedIds = newsList.map { it.newsId }.toSet()
        com.example.data.repository.BalasoreRepository.newsArticles.filter { !savedIds.contains(it.id) }.take(4)
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (newsList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(44.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isOdia) "କୌଣସି ଖବର ସଂରକ୍ଷିତ ହୋଇନାହିଁ" else "No saved news articles yet",
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isOdia) "ତଳୁ ଆପଣଙ୍କ ପସନ୍ଦର ଖବର ସେଭ୍ କରନ୍ତୁ" else "Tap + Save on any Balasore headline below or from the News feed",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(newsList, key = { it.newsId }) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("saved_news_card_${item.newsId}"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 2)
                            Text(
                                text = "${item.source} • ${item.category}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { onRemove(item.newsId) },
                            modifier = Modifier.testTag("remove_saved_news_${item.newsId}")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        if (unbookmarkedArticles.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isOdia) "ଅଧିକ ବାଲେଶ୍ୱର ଖବର ସେଭ୍ କରନ୍ତୁ:" else "Quick Save Balasore Headlines:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            items(unbookmarkedArticles, key = { "add_${it.id}" }) { article ->
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = article.title, fontWeight = FontWeight.Medium, fontSize = 13.sp, maxLines = 1)
                            Text(text = "${article.category} • ${article.source}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(
                            onClick = { onAddSampleArticle(article) },
                            modifier = Modifier.testTag("quick_save_news_${article.id}")
                        ) {
                            Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isOdia) "ସେଭ୍" else "Save", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedBookmarksTab(
    isOdia: Boolean,
    bookmarksList: List<TourismBookmarkCloudItem>,
    onAddSpot: (com.example.data.model.Hotspot) -> Unit = {},
    onOpen: (String) -> Unit,
    onRemove: (String) -> Unit
) {
    val unbookmarkedSpots = remember(bookmarksList) {
        val savedIds = bookmarksList.map { it.spotId }.toSet()
        com.example.data.repository.BalasoreRepository.hotspots.filter { !savedIds.contains(it.id) }.take(4)
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (bookmarksList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(44.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isOdia) "କୌଣସି ପର୍ଯ୍ୟଟନ ସ୍ଥଳ ବୁକମାର୍କ ହୋଇନାହିଁ" else "No bookmarked tourism spots yet",
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isOdia) "ତଳୁ ଆପଣଙ୍କ ପସନ୍ଦର ପର୍ଯ୍ୟଟନ ସ୍ଥଳ ସେଭ୍ କରନ୍ତୁ" else "Tap + Save on any Balasore tourism spot below",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(bookmarksList, key = { it.spotId }) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpen(item.spotId) }
                        .testTag("saved_hotspot_card_${item.spotId}"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.spotName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(
                                text = "${item.category} • ${item.location} • ★ ${item.rating}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { onRemove(item.spotId) },
                            modifier = Modifier.testTag("remove_saved_hotspot_${item.spotId}")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        if (unbookmarkedSpots.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isOdia) "ଅଧିକ ପର୍ଯ୍ୟଟନ ସ୍ଥଳୀ ସେଭ୍ କରନ୍ତୁ:" else "Quick Save Balasore Tourism Spots:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            items(unbookmarkedSpots, key = { "add_spot_${it.id}" }) { spot ->
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = if (isOdia) spot.odiaName else spot.name, fontWeight = FontWeight.Medium, fontSize = 13.sp, maxLines = 1)
                            Text(text = "${spot.category} • ${spot.location}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(
                            onClick = { onAddSpot(spot) },
                            modifier = Modifier.testTag("quick_save_spot_${spot.id}")
                        ) {
                            Icon(Icons.Default.FavoriteBorder, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isOdia) "ସେଭ୍" else "Save", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VisitedSpotsTab(
    isOdia: Boolean,
    visitedIds: Set<String>,
    onToggleVisited: (com.example.data.model.Hotspot) -> Unit,
    onOpenSpot: (String) -> Unit
) {
    val allSpots = remember { com.example.data.repository.BalasoreRepository.hotspots }
    val progress = if (allSpots.isNotEmpty()) visitedIds.size.toFloat() / allSpots.size.toFloat() else 0f

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("visited_spots_tab_list"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isOdia) "ବାଲେଶ୍ୱର ଭ୍ରମଣ ପାସପୋର୍ଟ" else "Balasore Explorer Passport",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${visitedIds.size} / ${allSpots.size} Visited",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF16A34A)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isOdia)
                            "ଆପଣ ଭ୍ରମଣ କରିଥିବା ପର୍ଯ୍ୟଟନ ସ୍ଥଳୀଗୁଡ଼ିକୁ ଚିହ୍ନଟ କରନ୍ତୁ"
                        else
                            "Mark tourism hotspots as visited to track your Balasore journey",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        items(allSpots, key = { it.id }) { spot ->
            val isVisited = visitedIds.contains(spot.id)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenSpot(spot.id) }
                    .testTag("profile_visited_spot_card_${spot.id}"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isVisited) Color(0xFFF0FDF4) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ),
                border = BorderStroke(1.dp, if (isVisited) Color(0xFF86EFAC) else MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isOdia) spot.odiaName else spot.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = if (isVisited) Color(0xFF14532D) else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${spot.category} • ${spot.location} (${spot.distanceKm} km)",
                            fontSize = 11.sp,
                            color = if (isVisited) Color(0xFF166534) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    FilterChip(
                        selected = isVisited,
                        onClick = { onToggleVisited(spot) },
                        label = {
                            Text(
                                text = if (isVisited) (if (isOdia) "✓ ଭ୍ରମଣ ହୋଇଛି" else "✓ Visited") else (if (isOdia) "ଭ୍ରମଣ ଚିହ୍ନଟ" else "Mark Visited"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        modifier = Modifier.testTag("toggle_visited_chip_${spot.id}")
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileCivicLeaderboardPreviewCard(
    isOdia: Boolean,
    currentUserName: String,
    onViewFullLeaderboard: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { com.example.data.civic.CitizenLeaderboardRepository(context) }
    val topContributors = remember(currentUserName) {
        repository.getCombinedLeaderboard(
            userReports = emptyList(),
            timePeriod = com.example.data.civic.LeaderboardTimePeriod.ALL_TIME,
            wardFilter = "All Wards",
            userName = currentUserName
        ).sortedByDescending { it.reportsResolved }.take(4)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("profile_civic_leaderboard_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
        border = BorderStroke(1.dp, Color(0xFFFDE68A))
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🏆", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isOdia) "ନାଗରିକ ଲିଡରବୋର୍ଡ (ସର୍ବାଧିକ ସମାଧାନ)" else "Civic Leaderboard • Most Resolved Reports",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = Color(0xFF92400E)
                        )
                        Text(
                            text = if (isOdia) "ବାଲେଶ୍ୱର ପୌରପାଳିକା ସକ୍ରିୟ ନାଗରିକ ମାନ୍ୟତା" else "Competitive ranking of citizens resolving local civic issues",
                            fontSize = 10.5.sp,
                            color = Color(0xFFB45309)
                        )
                    }
                }
                TextButton(
                    onClick = onViewFullLeaderboard,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.testTag("profile_view_full_leaderboard_btn")
                ) {
                    Text(
                        text = if (isOdia) "ସମସ୍ତ ଦେଖନ୍ତୁ →" else "Full Rank →",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD97706)
                    )
                }
            }

            topContributors.forEachIndexed { idx, contributor ->
                val medal = when (idx) {
                    0 -> "🥇"
                    1 -> "🥈"
                    2 -> "🥉"
                    else -> "#${idx + 1}"
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (contributor.isCurrentUser) Color(0xFFFEF3C7) else Color.White,
                    border = BorderStroke(
                        1.dp,
                        if (contributor.isCurrentUser) Color(0xFFF59E0B) else Color(0xFFFDE68A)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = medal, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isOdia) contributor.odiaDisplayName else contributor.displayName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = "${if (isOdia) contributor.odiaWardName else contributor.wardName} • ${contributor.tier.name.replace("_", " ")}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFDCFCE7)
                        ) {
                            Text(
                                text = "✓ ${contributor.reportsResolved} Resolved • ${contributor.civicPoints} pts",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF15803D),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileCivicLeaderboardTab(
    isOdia: Boolean,
    currentUserName: String
) {
    val context = LocalContext.current
    val repository = remember { com.example.data.civic.CitizenLeaderboardRepository(context) }
    var sortByResolvedOnly by remember { mutableStateOf(true) }

    val leaderboard = remember(currentUserName, sortByResolvedOnly) {
        val list = repository.getCombinedLeaderboard(
            userReports = emptyList(),
            timePeriod = com.example.data.civic.LeaderboardTimePeriod.ALL_TIME,
            wardFilter = "All Wards",
            userName = currentUserName
        )
        if (sortByResolvedOnly) {
            list.sortedWith(compareByDescending<com.example.data.civic.CivicContributor> { it.reportsResolved }.thenByDescending { it.civicPoints })
        } else {
            list.sortedByDescending { it.civicPoints }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("profile_civic_leaderboard_tab_list"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isOdia) "🏆 ବାଲେଶ୍ୱର ନାଗରିକ ଲିଡରବୋର୍ଡ" else "🏆 Balasore Civic Leaderboard",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                            Text(
                                text = if (isOdia) {
                                    "ସର୍ବାଧିକ ସମାଧାନ ହୋଇଥିବା ନାଗରିକ ଅଭିଯୋଗ ଆଧାରରେ ମାନ୍ୟତା"
                                } else {
                                    "Ranked by most resolved municipal reports & verified community impact"
                                },
                                fontSize = 11.sp,
                                color = Color(0xFFBAE6FD)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, Color(0xFFF59E0B))
                        ) {
                            Text(
                                text = "${leaderboard.sumOf { it.reportsResolved }} Resolved",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFDE68A),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = sortByResolvedOnly,
                            onClick = { sortByResolvedOnly = true },
                            label = {
                                Text(
                                    if (isOdia) "ସର୍ବାଧିକ ସମାଧାନ (Most Resolved)" else "Most Resolved Reports",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        )
                        FilterChip(
                            selected = !sortByResolvedOnly,
                            onClick = { sortByResolvedOnly = false },
                            label = {
                                Text(
                                    if (isOdia) "ଇମ୍ପାକ୍ଟ ପଏଣ୍ଟସ୍ (Impact Points)" else "Total Impact Points",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        )
                    }
                }
            }
        }

        items(leaderboard, key = { it.userId }) { contributor ->
            val rankIndex = leaderboard.indexOf(contributor) + 1
            val badgeText = when (rankIndex) {
                1 -> "🥇 #1"
                2 -> "🥈 #2"
                3 -> "🥉 #3"
                else -> "#$rankIndex"
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_leaderboard_row_${contributor.userId}"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (contributor.isCurrentUser) Color(0xFFFFFBEB) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ),
                border = BorderStroke(
                    1.dp,
                    if (contributor.isCurrentUser) Color(0xFFF59E0B) else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (rankIndex <= 3) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = badgeText,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.5.sp,
                                    color = if (rankIndex <= 3) Color(0xFFB45309) else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = (if (isOdia) contributor.odiaDisplayName else contributor.displayName) +
                                    if (contributor.isCurrentUser) " (You)" else "",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "${if (isOdia) contributor.odiaWardName else contributor.wardName} • ${contributor.avatarEmoji} ${contributor.tier.name.replace("_", " ")}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Top Focus: ${contributor.specialtyCategory} • ${contributor.reportsSubmitted} Submitted",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFDCFCE7)
                        ) {
                            Text(
                                text = "✓ ${contributor.reportsResolved} Resolved",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF15803D),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${contributor.civicPoints} Impact Pts",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}


