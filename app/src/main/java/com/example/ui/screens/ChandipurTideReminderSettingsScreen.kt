package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.AppLanguage
import com.example.data.notification.ChandipurTideReminderPreferences
import com.example.data.notification.TideAlertSoundOption
import com.example.ui.components.LocalTideCalculationEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.sin

/**
 * Dedicated Settings Screen for configuring the Chandipur Vanishing Sea
 * 30-minute safe walking window tide reminder notification and customizing the alert sound.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChandipurTideReminderSettingsScreen(
    language: AppLanguage = AppLanguage.ENGLISH,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isOdia = language == AppLanguage.ODIA

    BackHandler(onBack = onClose)

    // Load persisted preferences into StateFlow on entry
    LaunchedEffect(Unit) {
        ChandipurTideReminderPreferences.loadConfig(context)
    }

    val config by ChandipurTideReminderPreferences.configFlow.collectAsState()
    var liveWindow by remember { mutableStateOf(LocalTideCalculationEngine.evaluateLiveTideWindow()) }
    var currentlyPreviewingSoundId by remember { mutableStateOf<String?>(null) }

    // Refresh live tidal telemetry every 30s while viewing settings
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000L)
            liveWindow = LocalTideCalculationEngine.evaluateLiveTideWindow()
        }
    }

    // Ensure preview ringtone stops cleanly when navigating away
    DisposableEffect(Unit) {
        onDispose {
            ChandipurTideReminderPreferences.stopPreview()
        }
    }

    // Android 13+ POST_NOTIFICATIONS runtime permission state & launcher
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotificationPermission = granted
        Toast.makeText(
            context,
            if (granted) {
                if (isOdia) "ନୋଟିଫିକେସନ୍ ଅନୁମତି ମଞ୍ଜୁର ହେଲା!" else "Notification permission granted!"
            } else {
                if (isOdia) "ନୋଟିଫିକେସନ୍ ଅନୁମତି ଆବଶ୍ୟକ" else "Enable notifications in Android Settings to hear tide alerts"
            },
            Toast.LENGTH_SHORT
        ).show()
    }

    // Native Android Ringtone Picker for selecting any custom device alert sound
    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val pickedUri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            }
            val updated = ChandipurTideReminderPreferences.setCustomDeviceSound(context, pickedUri)
            val label = ChandipurTideReminderPreferences.getSoundDisplayLabel(updated, isOdia)
            Toast.makeText(
                context,
                if (isOdia) "ଆଲର୍ଟ ସ୍ୱର ଚୟନ ହେଲା: $label" else "Alert sound set to: $label",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun launchSystemRingtonePicker() {
        val currentUri = ChandipurTideReminderPreferences.resolveAlertSoundUri(
            context = context,
            option = config.selectedSound,
            customUriString = config.customSoundUri
        )
        val pickerIntent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL)
            putExtra(
                RingtoneManager.EXTRA_RINGTONE_TITLE,
                if (isOdia) "ଚାନ୍ଦିପୁର ଜୁଆର ଆଲର୍ଟ ସ୍ୱର ବାଛନ୍ତୁ" else "Select Chandipur Tide Alert Sound"
            )
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
            putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentUri)
        }
        try {
            ringtonePickerLauncher.launch(pickerIntent)
        } catch (_: Exception) {
            Toast.makeText(
                context,
                if (isOdia) "ଏହି ଡିଭାଇସ୍ ରେ ସିଷ୍ଟମ୍ ରିଙ୍ଗଟୋନ୍ ପିକର୍ ଉପଲବ୍ଧ ନାହିଁ"
                else "System ringtone picker unavailable on this device — select a preset above",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun togglePreview(option: TideAlertSoundOption) {
        if (currentlyPreviewingSoundId == option.id) {
            ChandipurTideReminderPreferences.stopPreview()
            currentlyPreviewingSoundId = null
        } else {
            val started = ChandipurTideReminderPreferences.previewSound(
                context = context,
                option = option,
                customUriString = config.customSoundUri
            )
            if (started) {
                currentlyPreviewingSoundId = option.id
                scope.launch {
                    delay(2600L)
                    if (currentlyPreviewingSoundId == option.id) {
                        ChandipurTideReminderPreferences.stopPreview()
                        currentlyPreviewingSoundId = null
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("chandipur_tide_reminder_settings_screen"),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isOdia) "ଚାନ୍ଦିପୁର ଜୁଆର ରିମାଇଣ୍ଡର ସେଟିଂସ୍" else "Chandipur Tide Reminder",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isOdia) "୩୦-ମିନିଟ୍ ସୁରକ୍ଷିତ ଚାଲିବା ଆଲର୍ଟ ଓ ସ୍ୱର"
                            else "30-Min Safe Walking Window & Alert Sound",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            ChandipurTideReminderPreferences.stopPreview()
                            onClose()
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("tide_reminder_settings_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (isOdia) "ପଛକୁ ଯାଆନ୍ତୁ" else "Navigate back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 640.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Coastal Vanishing Sea Tidal Rhythm Hero Banner
                item {
                    VanishingSeaHeroCard(
                        isOdia = isOdia,
                        isEnabled = config.is30MinReminderEnabled,
                        recessionKm = liveWindow.currentRecessionKm,
                        waterLevelMeters = liveWindow.currentWaterLevelMeters,
                        safeWindowRange = liveWindow.safeWalkingWindowRange,
                        nextHighTideLabel = liveWindow.nextHighTideClockLabel,
                        activeSoundLabel = ChandipurTideReminderPreferences.getSoundDisplayLabel(config, isOdia)
                    )
                }

                // 2. Android 13+ Notification Permission Notice (if missing)
                if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.45f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("tide_notif_permission_banner")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Notification permission required",
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isOdia) "ନୋଟିଫିକେସନ୍ ଅନୁମତି ଆବଶ୍ୟକ" else "Notification Permission Needed",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Text(
                                        text = if (isOdia) "୩୦ ମିନିଟ୍ ପୂର୍ବରୁ ଜୁଆର ଆଲର୍ଟ ପାଇବା ପାଇଁ ଅନୁମତି ଦିଅନ୍ତୁ।"
                                        else "Allow notifications so 30-minute Chandipur safe walk alerts can chime on time.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                                Button(
                                    onClick = {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    },
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("tide_request_notif_permission_btn"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = if (isOdia) "ଅନୁମତି" else "Allow",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Master 30-Minute Tide Reminder Notification Toggle Card
                item {
                    val cardContainerColor by animateColorAsState(
                        targetValue = if (config.is30MinReminderEnabled) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        },
                        label = "reminder_card_bg"
                    )
                    val cardBorderColor by animateColorAsState(
                        targetValue = if (config.is30MinReminderEnabled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        },
                        label = "reminder_card_border"
                    )

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = cardContainerColor),
                        border = BorderStroke(1.2.dp, cardBorderColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tide_reminder_master_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (config.is30MinReminderEnabled) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.outline
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (config.is30MinReminderEnabled) {
                                                Icons.Default.NotificationsActive
                                            } else {
                                                Icons.Default.NotificationsOff
                                            },
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = if (config.is30MinReminderEnabled) {
                                                if (isOdia) "ସକ୍ରିୟ • ୩୦ ମିନିଟ୍ ପୂର୍ବରୁ" else "ACTIVE • 30M ADVANCE"
                                            } else {
                                                if (isOdia) "ବନ୍ଦ ଅଛି" else "REMINDER OFF"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = if (isOdia) "ଅଫଲାଇନ୍ ହାର୍ମୋନିକ୍ ସିଙ୍କ୍" else "Offline Harmonic Sync",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = if (isOdia) {
                                            "୩୦-ମିନିଟ୍ ସୁରକ୍ଷିତ ଚାଲିବା ରିମାଇଣ୍ଡର"
                                        } else {
                                            "30-Minute Tide Reminder Notification"
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (isOdia) {
                                            "ଚାନ୍ଦିପୁର ଅଦୃଶ୍ୟ ସମୁଦ୍ର ବେଳାଭୂମି ସୁରକ୍ଷିତ ଚାଲିବା ସମୟ (${liveWindow.safeWalkingWindowRange}) ଖୋଲିବାର ୩୦ ମିନିଟ୍ ପୂର୍ବରୁ ସ୍ୱୟଂଚାଳିତ ନୋଟିଫିକେସନ୍ ପଠାଏ।"
                                        } else {
                                            "Automatically alerts you 30 minutes before the Chandipur vanishing sea safe walking window (${liveWindow.safeWalkingWindowRange}) opens."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Switch(
                                    checked = config.is30MinReminderEnabled,
                                    onCheckedChange = { enabled ->
                                        ChandipurTideReminderPreferences.set30MinReminderEnabled(context, enabled)
                                        Toast.makeText(
                                            context,
                                            if (enabled) {
                                                if (isOdia) "୩୦-ମିନିଟ୍ ଜୁଆର ରିମାଇଣ୍ଡର ଅନ୍ ହେଲା!"
                                                else "30-minute Chandipur tide reminder enabled"
                                            } else {
                                                if (isOdia) "୩୦-ମିନିଟ୍ ଜୁଆର ରିମାଇଣ୍ଡର ବନ୍ଦ ହେଲା"
                                                else "30-minute Chandipur tide reminder turned off"
                                            },
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .semantics {
                                            contentDescription = "Toggle 30-minute Chandipur tide reminder notification"
                                        }
                                        .testTag("tide_reminder_30m_master_switch")
                                )
                            }
                        }
                    }
                }

                // 4. Alert Sound Customization Card
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tide_alert_sound_selector_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
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
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Column {
                                        Text(
                                            text = if (isOdia) "ଆଲର୍ଟ ସ୍ୱର କଷ୍ଟମାଇଜ୍ କରନ୍ତୁ" else "Customize Alert Sound",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (isOdia) "ଚାନ୍ଦିପୁର ବେଳାଭୂମି ପବନ ଓ ଲହରୀ ପାଇଁ ସ୍ପଷ୍ଟ ସ୍ୱର ବାଛନ୍ତୁ"
                                            else "Select a preset tone or pick any sound on your device",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))

                            TideAlertSoundOption.entries.forEach { option ->
                                val isSelected = config.selectedSound == option
                                val isPreviewPlaying = currentlyPreviewingSoundId == option.id
                                val iconVector: ImageVector = when (option) {
                                    TideAlertSoundOption.DEFAULT_NOTIFICATION -> Icons.Default.NotificationsActive
                                    TideAlertSoundOption.COASTAL_ALARM_SIREN -> Icons.Default.Alarm
                                    TideAlertSoundOption.HARBOR_BELL_CHIME -> Icons.Default.GraphicEq
                                    TideAlertSoundOption.CUSTOM_DEVICE_TONE -> Icons.Default.LibraryMusic
                                    TideAlertSoundOption.SILENT_VIBRATE_ONLY -> Icons.AutoMirrored.Filled.VolumeOff
                                }

                                val titleText = if (option == TideAlertSoundOption.CUSTOM_DEVICE_TONE && !config.customSoundTitle.isNullOrBlank()) {
                                    if (isOdia) "କଷ୍ଟମ୍ ସ୍ୱର: ${config.customSoundTitle}" else "Custom: ${config.customSoundTitle}"
                                } else {
                                    if (isOdia) option.titleOr else option.titleEn
                                }

                                val subtitleText = if (isOdia) option.subtitleOr else option.subtitleEn

                                SoundOptionRow(
                                    option = option,
                                    icon = iconVector,
                                    title = titleText,
                                    subtitle = subtitleText,
                                    isSelected = isSelected,
                                    isPreviewPlaying = isPreviewPlaying,
                                    enabled = config.is30MinReminderEnabled,
                                    onSelect = {
                                        if (option == TideAlertSoundOption.CUSTOM_DEVICE_TONE && config.customSoundUri.isNullOrBlank()) {
                                            launchSystemRingtonePicker()
                                        } else {
                                            ChandipurTideReminderPreferences.setSelectedSoundOption(context, option)
                                            Toast.makeText(
                                                context,
                                                if (isOdia) "ସ୍ୱର ଅପଡେଟ୍ ହେଲା: $titleText" else "Alert sound updated: $titleText",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                    onPreviewClick = { togglePreview(option) }
                                )
                            }

                            // Native Android Device Ringtone Picker CTA
                            OutlinedButton(
                                onClick = { launchSystemRingtonePicker() },
                                enabled = config.is30MinReminderEnabled,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                                    .testTag("tide_pick_custom_ringtone_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LibraryMusic,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isOdia) "ଫୋନ୍ ରିଙ୍ଗଟୋନ୍ ରୁ କଷ୍ଟମ୍ ସ୍ୱର ବାଛନ୍ତୁ"
                                    else "Choose Custom Tone from Device Ringtones",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // 5. Coastal Delivery & Return Safety Preferences Card
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tide_delivery_options_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = if (isOdia) "ସୁରକ୍ଷା ଓ କମ୍ପନ ବିକଳ୍ପ" else "Coastal Delivery & Return Safety",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Vibration,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = if (isOdia) "କମ୍ପନ ପ୍ୟାଟର୍ନ୍ (Vibration)" else "Rhythmic Coastal Vibration",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = if (isOdia) "ଉଚ୍ଚ ପବନ ସମୟରେ ପକେଟ୍ ରେ ସତର୍କତା ଅନୁଭବ କରନ୍ତୁ"
                                            else "Pulse vibration alongside sound so you feel alerts in loud surf",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Switch(
                                    checked = config.vibrateEnabled,
                                    enabled = config.is30MinReminderEnabled,
                                    onCheckedChange = { enabled ->
                                        ChandipurTideReminderPreferences.setVibrateEnabled(context, enabled)
                                    },
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("tide_reminder_vibrate_switch")
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Waves,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = if (isOdia) "ଜୁଆର ଫେରିବା ୩୦-ମିନିଟ୍ ସତର୍କତା" else "30-Min Return-to-Shore Warning",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = if (isOdia) "ଉଚ୍ଚ ଜୁଆର (${liveWindow.nextHighTideClockLabel}) ପୂର୍ବରୁ କୂଳକୁ ଫେରିବା ପାଇଁ ସୂଚନା"
                                            else "Also alert 30 minutes before high tide (${liveWindow.nextHighTideClockLabel}) reclaims the seabed",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Switch(
                                    checked = config.closingReturnReminderEnabled,
                                    enabled = config.is30MinReminderEnabled,
                                    onCheckedChange = { enabled ->
                                        ChandipurTideReminderPreferences.setClosingReturnReminderEnabled(context, enabled)
                                    },
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("tide_reminder_closing_switch")
                                )
                            }
                        }
                    }
                }

                // 6. Test Reminder & System Channel Controls
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    LocalTideCalculationEngine.triggerSafeTide30MinNotification(
                                        context = context,
                                        isOdia = isOdia
                                    )
                                }
                            },
                            enabled = config.is30MinReminderEnabled,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("tide_send_test_reminder_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isOdia) "ପରୀକ୍ଷଣ ୩୦-ମିନିଟ୍ ଜୁଆର ଆଲର୍ଟ ପଠାନ୍ତୁ"
                                else "Send Test 30-Min Tide Reminder",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            OutlinedButton(
                                onClick = {
                                    val channelId = ChandipurTideReminderPreferences.ensureConfiguredNotificationChannel(context)
                                    val channelIntent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                        putExtra(Settings.EXTRA_CHANNEL_ID, channelId)
                                    }
                                    try {
                                        context.startActivity(channelIntent)
                                    } catch (_: Exception) {
                                        Toast.makeText(
                                            context,
                                            "Open Android Settings → Notifications to fine-tune channels",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                                    .testTag("tide_open_system_channel_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isOdia) "ଆଣ୍ଡ୍ରଏଡ୍ ନୋଟିଫିକେସନ୍ ଚ୍ୟାନେଲ୍ ସେଟିଂସ୍"
                                    else "Open Android Notification Channel Settings",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun VanishingSeaHeroCard(
    isOdia: Boolean,
    isEnabled: Boolean,
    recessionKm: Double,
    waterLevelMeters: Double,
    safeWindowRange: String,
    nextHighTideLabel: String,
    activeSoundLabel: String
) {
    val infiniteTransition = rememberInfiniteTransition(label = "chandipur_wave_motion")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    val deepOceanStart = Color(0xFF06283D)
    val deepOceanMid = Color(0xFF0A4D68)
    val seafoamTeal = Color(0xFF088395)

    Card(
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("chandipur_tide_hero_banner")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(deepOceanStart, deepOceanMid, seafoamTeal)
                    )
                )
        ) {
            // Animated receding sea wave layers painted on Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(168.dp)
            ) {
                val width = size.width
                val height = size.height

                val backWave = Path().apply {
                    moveTo(0f, height)
                    val baseY = height * 0.68f
                    var x = 0f
                    while (x <= width) {
                        val y = baseY + sin((x / width) * 3.5f * Math.PI.toFloat() + wavePhase) * 14f
                        lineTo(x, y)
                        x += 12f
                    }
                    lineTo(width, height)
                    close()
                }
                drawPath(path = backWave, color = Color(0x2838BDF8))

                val frontWave = Path().apply {
                    moveTo(0f, height)
                    val baseY = height * 0.80f
                    var x = 0f
                    while (x <= width) {
                        val y = baseY + sin((x / width) * 2.8f * Math.PI.toFloat() - wavePhase * 1.2f) * 10f
                        lineTo(x, y)
                        x += 12f
                    }
                    lineTo(width, height)
                    close()
                }
                drawPath(path = frontWave, color = Color(0x33A7F3D0))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Waves,
                                contentDescription = null,
                                tint = Color(0xFF7DD3FC),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (isOdia) "ଚାନ୍ଦିପୁର ଅଦୃଶ୍ୟ ସମୁଦ୍ର" else "CHANDIPUR VANISHING SEA",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.7.sp
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isEnabled) Color(0xFF10B981).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.12f),
                        border = BorderStroke(
                            1.dp,
                            if (isEnabled) Color(0xFF34D399) else Color.White.copy(alpha = 0.3f)
                        )
                    ) {
                        Text(
                            text = if (isEnabled) {
                                if (isOdia) "● ୩୦ମି ଆଲର୍ଟ ଅନ୍" else "● 30m Alert ON"
                            } else {
                                if (isOdia) "○ ବନ୍ଦ ଅଛି" else "○ Paused"
                            },
                            color = if (isEnabled) Color(0xFFA7F3D0) else Color.White.copy(alpha = 0.8f),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = if (isOdia) "ସୁରକ୍ଷିତ ବେଳାଭୂମି ଚାଲିବା ସମୟ" else "Safe Walking Window",
                        color = Color(0xFFBAE6FD),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = safeWindowRange,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HeroMetricPill(
                        label = if (isOdia) "ସମୁଦ୍ର ପଶ୍ଚାତଗମନ" else "Seabed Exposed",
                        value = "${String.format(Locale.US, "%.1f", recessionKm)} km",
                        modifier = Modifier.weight(1f)
                    )
                    HeroMetricPill(
                        label = if (isOdia) "ଜଳସ୍ତର / ଉଚ୍ଚ ଜୁଆର" else "Level / High Tide",
                        value = "${String.format(Locale.US, "%.1f", waterLevelMeters)}m • $nextHighTideLabel",
                        modifier = Modifier.weight(1.3f)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = null,
                        tint = Color(0xFF67E8F9),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (isOdia) "ସକ୍ରିୟ ଆଲର୍ଟ ସ୍ୱର: $activeSoundLabel" else "Active Alert Sound: $activeSoundLabel",
                        color = Color(0xFFE0F2FE),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroMetricPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.Black.copy(alpha = 0.24f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = label,
                color = Color(0xFFBAE6FD),
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                color = Color.White,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SoundOptionRow(
    option: TideAlertSoundOption,
    icon: ImageVector,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    isPreviewPlaying: Boolean,
    enabled: Boolean,
    onSelect: () -> Unit,
    onPreviewClick: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected && enabled) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        },
        label = "sound_row_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected && enabled) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
        },
        label = "sound_row_border"
    )

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        border = BorderStroke(if (isSelected && enabled) 1.5.dp else 1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = enabled, onClick = onSelect)
            .testTag("tide_sound_option_${option.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = if (enabled) onSelect else null,
                enabled = enabled
            )

            Surface(
                shape = CircleShape,
                color = if (isSelected && enabled) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                } else {
                    MaterialTheme.colorScheme.surface
                },
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected && enabled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    AnimatedVisibility(visible = isSelected && enabled) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selected alert sound",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (option != TideAlertSoundOption.SILENT_VIBRATE_ONLY) {
                IconButton(
                    onClick = onPreviewClick,
                    enabled = enabled,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("tide_sound_preview_btn_${option.id}")
                ) {
                    Icon(
                        imageVector = if (isPreviewPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = if (isPreviewPlaying) "Stop preview of $title" else "Preview $title",
                        tint = if (isPreviewPlaying) {
                            MaterialTheme.colorScheme.error
                        } else if (enabled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        }
                    )
                }
            }
        }
    }
}
