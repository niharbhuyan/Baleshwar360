package com.example.ui.features.profile

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.fcm.FcmManager
import com.example.data.firebase.FirebaseAuthManager
import com.example.data.firebase.FirestoreService
import com.example.data.local.UserEntity
import com.example.data.model.AppLanguage
import com.example.data.model.FavoriteNewsCloudItem
import com.example.data.model.TourismBookmarkCloudItem
import com.example.data.model.UserWeatherPreferences
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    language: AppLanguage = AppLanguage.ENGLISH,
    onOpenHotspot: (String) -> Unit = {},
    onOpenAdminPanel: () -> Unit = {},
    onNavigateToTab: (Int) -> Unit = {},
    onOpenCivicEye: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val authManager = remember { FirebaseAuthManager(context) }
    val firestoreService = remember { FirestoreService(context) }

    val currentUser by authManager.currentUser.collectAsStateWithLifecycle()
    val activeSessionUser by authManager.activeSessionUser.collectAsStateWithLifecycle()
    val isOdia = language == AppLanguage.ODIA

    var selectedSection by remember { mutableIntStateOf(0) } // 0: Profile Info, 1: Notification Preferences, 2: Saved News, 3: Saved Spots
    var isSigningIn by remember { mutableStateOf(false) }
    var userProfile by remember { mutableStateOf<UserEntity?>(null) }
    var notifPrefs by remember { mutableStateOf(UserWeatherPreferences()) }
    var savedNewsList by remember { mutableStateOf<List<FavoriteNewsCloudItem>>(emptyList()) }
    var savedBookmarksList by remember { mutableStateOf<List<TourismBookmarkCloudItem>>(emptyList()) }
    var isSavingProfile by remember { mutableStateOf(false) }

    var editDisplayName by remember { mutableStateOf("") }
    var editBio by remember { mutableStateOf("") }
    var editLocality by remember { mutableStateOf("Balasore Town") }
    var editPhoneNumber by remember { mutableStateOf("") }

    val isSystemNotificationEnabled = remember(context) {
        NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    val localities = remember {
        listOf(
            "Balasore Town", "Chandipur", "Remuna", "Soro",
            "Jaleswar", "Bhograi", "Nilagiri", "Basta", "Baliapal", "Simulia"
        )
    }

    LaunchedEffect(currentUser, activeSessionUser) {
        val fbUser = currentUser
        val sessionUser = activeSessionUser
        val uid = fbUser?.uid ?: sessionUser?.id
        if (uid != null) {
            val profileRes = firestoreService.fetchUserProfile(uid)
            val p = profileRes.getOrNull() ?: sessionUser ?: fbUser?.let { authManager.toUserEntity(it) }
            if (p != null) {
                userProfile = p
                editDisplayName = p.fullName
                editBio = p.bio
                editLocality = p.locality
                editPhoneNumber = p.phoneNumber
            }

            val weatherRes = firestoreService.fetchWeatherPreferences(uid)
            notifPrefs = weatherRes.getOrNull() ?: UserWeatherPreferences(userId = uid)

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
        } else {
            userProfile = null
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("profile_screen_main_view")
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Hero Profile Header Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_hero_header_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Profile Avatar",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(52.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (editDisplayName.isNotBlank()) editDisplayName else (activeSessionUser?.fullName ?: if (isOdia) "ସମ୍ମାନିତ ବାଲେଶ୍ୱର ନାଗରିକ" else "Balasore Explorer"),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = currentUser?.email ?: activeSessionUser?.email ?: (if (isOdia) "ଅତିଥି ଆକାଉଣ୍ଟ (Local Guest)" else "Guest Account • Local Sync Active"),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(editLocality, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                )
                                SuggestionChip(
                                    onClick = {},
                                    label = {
                                        Text(
                                            if (currentUser != null || activeSessionUser != null) "✓ Verified" else "Guest",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (currentUser != null || activeSessionUser != null) Color(0xFF16A34A) else MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                )
                            }
                        }
                    }

                    if (currentUser == null && activeSessionUser == null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    isSigningIn = true
                                    coroutineScope.launch {
                                        val res = authManager.signInWithGoogle(context)
                                        isSigningIn = false
                                        res.onSuccess { user ->
                                            Toast.makeText(context, if (isOdia) "ସ୍ୱାଗତ, ${user.fullName}!" else "Welcome, ${user.fullName}!", Toast.LENGTH_SHORT).show()
                                            firestoreService.saveUserProfile(user)
                                        }.onFailure { err ->
                                            Toast.makeText(context, err.message ?: "Google Sign-In failed", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("profile_quick_google_signin_btn"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isOdia) "ଗୁଗୁଲ୍ ସାଇନ୍-ଇନ୍" else "Sign In with Google", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // 2. Section Selector Tabs (Details | Push Alerts | Saved Articles | Saved Spots)
        item {
            ScrollableTabRow(
                selectedTabIndex = selectedSection,
                edgePadding = 0.dp,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedSection == 0,
                    onClick = { selectedSection = 0 },
                    modifier = Modifier.testTag("profile_view_tab_0"),
                    text = { Text(if (isOdia) "ପ୍ରୋଫାଇଲ୍ ବିବରଣୀ" else "Profile Details", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedSection == 1,
                    onClick = { selectedSection = 1 },
                    modifier = Modifier.testTag("profile_view_tab_1"),
                    text = { Text(if (isOdia) "🔔 ପୁସ୍ ସତର୍କତା" else "🔔 Push Alerts & News", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedSection == 2,
                    onClick = { selectedSection = 2 },
                    modifier = Modifier.testTag("profile_view_tab_2"),
                    text = { Text(if (isOdia) "ଖବର (${savedNewsList.size})" else "Saved News (${savedNewsList.size})", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedSection == 3,
                    onClick = { selectedSection = 3 },
                    modifier = Modifier.testTag("profile_view_tab_3"),
                    text = { Text(if (isOdia) "ସ୍ଥଳୀ (${savedBookmarksList.size})" else "Hotspots (${savedBookmarksList.size})", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold) }
                )
            }
        }

        // 3. Tab Contents
        when (selectedSection) {
            0 -> {
                // Section 0: Civic Eye Card & Profile Info
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenCivicEye() }
                            .testTag("profile_civic_eye_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF0284C7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isOdia) "ନାଗରିକ ଦୃଷ୍ଟି (Civic Eye)" else "Civic Eye Infrastructure Reporting",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = if (isOdia) "ଖାଲ-ଖମା ଓ ଜଳବନ୍ଦୀ ସମସ୍ୟା ଦାଖଲ କରନ୍ତୁ • Firestore ସିଙ୍କ୍" else "Report potholes & waterlogging • Firestore synced",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF475569)
                                )
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(18.dp))
                        }
                    }
                }

                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = if (isOdia) "ବ୍ୟକ୍ତିଗତ ବିବରଣୀ ସମ୍ପାଦନା" else "Personal Identity & Balasore Locality",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )

                            OutlinedTextField(
                                value = editDisplayName,
                                onValueChange = { editDisplayName = it },
                                label = { Text(if (isOdia) "ପୂରା ନାମ" else "Full Name") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("profile_edit_name_input"),
                                shape = RoundedCornerShape(10.dp)
                            )

                            OutlinedTextField(
                                value = editPhoneNumber,
                                onValueChange = { editPhoneNumber = it },
                                label = { Text(if (isOdia) "ମୋବାଇଲ୍ ନମ୍ବର" else "Phone Number") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("profile_edit_phone_input"),
                                shape = RoundedCornerShape(10.dp)
                            )

                            OutlinedTextField(
                                value = editBio,
                                onValueChange = { editBio = it },
                                label = { Text(if (isOdia) "ଆପଣଙ୍କ ବିଷୟରେ (Bio)" else "Bio / Interests") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("profile_edit_bio_input"),
                                shape = RoundedCornerShape(10.dp),
                                maxLines = 3
                            )

                            Text(
                                text = if (isOdia) "ଆପଣଙ୍କ ବାଲେଶ୍ୱର ବ୍ଲକ୍ / ଅଞ୍ଚଳ ବାଛନ୍ତୁ:" else "Select Your Balasore Ward / Block:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                localities.take(4).forEach { loc ->
                                    val isSelected = editLocality == loc
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { editLocality = loc },
                                        label = { Text(loc, fontSize = 10.5.sp) }
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    isSavingProfile = true
                                    val activeUid = currentUser?.uid ?: activeSessionUser?.id ?: "local_guest"
                                    val updatedUser = UserEntity(
                                        id = activeUid,
                                        fullName = editDisplayName.ifBlank { "Balasore Citizen" },
                                        email = currentUser?.email ?: activeSessionUser?.email ?: "",
                                        passwordHash = "SESSION_MANAGED",
                                        phoneNumber = editPhoneNumber,
                                        locality = editLocality,
                                        bio = editBio,
                                        avatarUri = currentUser?.photoUrl?.toString() ?: activeSessionUser?.avatarUri,
                                        securityQuestion = "Google Auth",
                                        securityAnswer = "SESSION_MANAGED",
                                        createdAt = userProfile?.createdAt ?: System.currentTimeMillis()
                                    )
                                    coroutineScope.launch {
                                        authManager.updateActiveSessionProfile(updatedUser)
                                        firestoreService.saveUserProfile(updatedUser)
                                        isSavingProfile = false
                                        userProfile = updatedUser
                                        Toast.makeText(context, if (isOdia) "ପ୍ରୋଫାଇଲ୍ ସେଭ୍ ହେଲା!" else "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("profile_save_changes_btn"),
                                shape = RoundedCornerShape(10.dp),
                                enabled = !isSavingProfile
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isSavingProfile) "Saving..." else (if (isOdia) "ପରିବର୍ତ୍ତନ ସଂରକ୍ଷଣ କରନ୍ତୁ" else "Save Profile Changes"))
                            }
                        }
                    }
                }
            }

            1 -> {
                // Section 1: Push Notifications Manager (Breaking News, Weather, Tourism)
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = if (isOdia) "🔔 ପୁସ୍ ନୋଟିଫିକେସନ୍ ସେଟିଂସ୍" else "🔔 Push Notification & Alert Preferences",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp,
                                color = MaterialTheme.colorScheme.primary
                            )

                            // 1. Breaking Local News Toggle
                            ProfileSwitchRow(
                                title = if (isOdia) "⚡ ସ୍ଥାନୀୟ ତାଜା ଖବର" else "⚡ Breaking Local News",
                                desc = if (isOdia) "ଜିଲ୍ଲାପାଳଙ୍କ ନିର୍ଦ୍ଦେଶ, ପ୍ରତିରକ୍ଷା ଓ ଟ୍ରାଫିକ୍ ସୂଚନା" else "District orders, defense advisories & local wire",
                                isChecked = notifPrefs.notifyBreakingNews,
                                onCheckedChange = { checked ->
                                    notifPrefs = notifPrefs.copy(notifyBreakingNews = checked)
                                    FcmManager.setBreakingNewsEnabled(context, checked)
                                }
                            )

                            OutlinedButton(
                                onClick = {
                                    FcmManager.triggerSampleBreakingNews(context)
                                    Toast.makeText(context, "Test Breaking News push dispatched!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("profile_test_news_btn"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (isOdia) "ଟେଷ୍ଟ୍ ଖବର ନୋଟିଫିକେସନ୍ ପଠାନ୍ତୁ" else "Send Test News Push Notification", fontSize = 11.5.sp)
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                            // 2. Critical Weather Alerts Toggle
                            ProfileSwitchRow(
                                title = if (isOdia) "⛈️ ଜରୁରୀ ପାଣିପାଗ ଓ ବାତ୍ୟା" else "⛈️ Critical Weather Alerts",
                                desc = if (isOdia) "ବାତ୍ୟା, ପ୍ରବଳ ବର୍ଷା, ସୁବର୍ଣ୍ଣରେଖା ବନ୍ୟା ଓ ଚାନ୍ଦିପୁର ଜୁଆର" else "Cyclones, heavy rainfall, river floods & high tides",
                                isChecked = notifPrefs.notifyCyclone,
                                onCheckedChange = { checked ->
                                    notifPrefs = notifPrefs.copy(
                                        notifyCyclone = checked,
                                        notifyHighTide = checked,
                                        notifyFloodAlerts = checked
                                    )
                                    FcmManager.setWeatherAlertsEnabled(context, checked)
                                    FcmManager.setChandipurHighTideAlertsEnabled(context, checked)
                                    FcmManager.setFloodAlertsEnabled(context, checked)
                                }
                            )

                            OutlinedButton(
                                onClick = {
                                    FcmManager.triggerSampleWeatherAlert(context)
                                    Toast.makeText(context, "Test Weather Alert push dispatched!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("profile_test_weather_btn"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (isOdia) "ଟେଷ୍ଟ୍ ପାଣିପାଗ ସତର୍କତା ପଠାନ୍ତୁ" else "Send Test Weather Alert Push Notification", fontSize = 11.5.sp)
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                            // 3. Tourism Hotspots Toggle
                            ProfileSwitchRow(
                                title = if (isOdia) "🏖️ ପର୍ଯ୍ୟଟନ ସ୍ଥଳୀ ଓ ସଫାରି" else "🏖️ Tourism Hotspots & Updates",
                                desc = if (isOdia) "ଚାନ୍ଦିପୁର ସମୁଦ୍ର ଯାତ୍ରା, କୁଲଡିହା ସଫାରି ଓ ପଞ୍ଚଲିଙ୍ଗେଶ୍ୱର" else "Chandipur walk times, Kuldiha safari & temple festivals",
                                isChecked = notifPrefs.notifyTourismHotspots,
                                onCheckedChange = { checked ->
                                    notifPrefs = notifPrefs.copy(
                                        notifyTourismHotspots = checked,
                                        notifyKuldihaSafari = checked,
                                        notifyPanchalingeswar = checked
                                    )
                                    FcmManager.setTourismHotspotsEnabled(context, checked)
                                }
                            )

                            OutlinedButton(
                                onClick = {
                                    FcmManager.triggerSampleTourismHotspot(context)
                                    Toast.makeText(context, "Test Tourism Hotspot push dispatched!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("profile_test_tourism_btn"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (isOdia) "ଟେଷ୍ଟ୍ ପର୍ଯ୍ୟଟନ ସତର୍କତା ପଠାନ୍ତୁ" else "Send Test Tourism Hotspot Push Notification", fontSize = 11.5.sp)
                            }

                            Button(
                                onClick = {
                                    val activeUid = currentUser?.uid ?: activeSessionUser?.id ?: "local_guest"
                                    coroutineScope.launch {
                                        firestoreService.saveWeatherPreferences(activeUid, notifPrefs)
                                        Toast.makeText(context, if (isOdia) "ସମସ୍ତ ନୋଟିଫିକେସନ୍ ପସନ୍ଦ ସେଭ୍ ହେଲା!" else "Notification preferences saved & synced!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("profile_save_notifs_btn"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isOdia) "ସମସ୍ତ ନୋଟିଫିକେସନ୍ ପସନ୍ଦ ସେଭ୍ କରନ୍ତୁ" else "Save & Sync Notification Preferences")
                            }
                        }
                    }
                }
            }

            2 -> {
                // Section 2: Saved News
                if (savedNewsList.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isOdia) "କୌଣସି ସଂରକ୍ଷିତ ଖବର ନାହିଁ । ନ୍ୟୁଜ୍ ଟ୍ୟାବ୍ ରୁ ଖବର ବୁକମାର୍କ କରନ୍ତୁ।" else "No saved news articles yet. Bookmark stories from the News tab.",
                                    fontSize = 12.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(savedNewsList) { article ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(article.title, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text(article.category, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                    Text(article.publishedAt, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            3 -> {
                // Section 3: Saved Hotspots
                if (savedBookmarksList.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isOdia) "କୌଣସି ସଂରକ୍ଷିତ ପର୍ଯ୍ୟଟନ ସ୍ଥଳୀ ନାହିଁ ।" else "No saved hotspots yet. Bookmark destinations in Tourism tab.",
                                    fontSize = 12.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(savedBookmarksList) { spot ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenHotspot(spot.spotId) },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(spot.spotName, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                                    Text(spot.location, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // 4. Quick Actions (Admin Panel & Sign Out)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenAdminPanel,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isOdia) "ଆଡମିନ୍ ପ୍ୟାନେଲ୍" else "Admin Panel", fontSize = 11.5.sp)
                }

                if (currentUser != null || activeSessionUser != null) {
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                authManager.signOut(context)
                                Toast.makeText(context, if (isOdia) "ଲଗ୍ ଆଉଟ୍ ହୋଇଛନ୍ତି" else "Signed out", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isOdia) "ଲଗ୍ ଆଉଟ୍" else "Log Out", fontSize = 11.5.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileSwitchRow(
    title: String,
    desc: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(checked = isChecked, onCheckedChange = onCheckedChange)
    }
}
