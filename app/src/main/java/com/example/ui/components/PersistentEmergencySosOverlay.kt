package com.example.ui.components

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.emergency.EmergencySosContact
import com.example.data.emergency.EmergencySosManager
import com.example.data.emergency.ForegroundVoiceHelpTrigger
import com.example.data.model.AppLanguage
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * Persistent Emergency SOS Quick-Action Overlay.
 *
 * Provides:
 * 1. 1-Tap instant dialing to Emergency 112 and automated GPS location dispatch to pre-configured contacts.
 * 2. Voice-activated trigger recognizing spoken distress commands (e.g. 'Call Balasore Emergency', 'Call 112').
 * 3. Quick-config sheet for customizing pre-configured emergency contacts.
 */
@Composable
fun PersistentEmergencySosOverlay(
    language: AppLanguage,
    onTriggerSosCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isExpanded by remember { mutableStateOf(false) }
    var showConfigureContactsDialog by remember { mutableStateOf(false) }
    var emergencyContacts by remember {
        mutableStateOf(EmergencySosManager.getSavedSosContacts(context))
    }
    var lastKnownLocationText by remember { mutableStateOf("Resolving GPS...") }
    var isVoiceListening by remember { mutableStateOf(false) }

    // Foreground "Help me" -> 112 auto-call confirmation dialog state
    var pendingVoiceHelpPhrase by remember { mutableStateOf<String?>(null) }
    var voiceCallCountdownSeconds by remember { mutableIntStateOf(3) }

    // Lifecycle-scoped foreground SpeechRecognizer controller for "Help me"
    val foregroundVoiceTrigger = remember(context) {
        ForegroundVoiceHelpTrigger(context) { matchedPhrase ->
            vibrateUrgent(context)
            voiceCallCountdownSeconds = 3
            pendingVoiceHelpPhrase = matchedPhrase
        }
    }
    val voiceGuardState by foregroundVoiceTrigger.state.collectAsState()

    // Keep recognizer locale synchronized with app language (English / Odia)
    LaunchedEffect(language) {
        foregroundVoiceTrigger.setLocale(if (language == AppLanguage.ODIA) "or-IN" else "en-IN")
    }

    // Bind ForegroundVoiceHelpTrigger strictly to foreground lifecycle (ON_RESUME / ON_PAUSE)
    DisposableEffect(lifecycleOwner, foregroundVoiceTrigger) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> foregroundVoiceTrigger.onAppResumed()
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> foregroundVoiceTrigger.onAppPaused()
                Lifecycle.Event.ON_DESTROY -> foregroundVoiceTrigger.destroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            foregroundVoiceTrigger.onAppResumed()
        }
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            foregroundVoiceTrigger.destroy()
        }
    }

    // 3-second automatic countdown that initiates the emergency call to 112 when "Help me" is heard
    LaunchedEffect(pendingVoiceHelpPhrase) {
        if (pendingVoiceHelpPhrase != null) {
            voiceCallCountdownSeconds = 3
            while (voiceCallCountdownSeconds > 0 && pendingVoiceHelpPhrase != null) {
                delay(1000L)
                voiceCallCountdownSeconds--
            }
            if (pendingVoiceHelpPhrase != null) {
                val heard = pendingVoiceHelpPhrase ?: "Help me"
                pendingVoiceHelpPhrase = null
                foregroundVoiceTrigger.pauseForAlertConfirmation(false)
                Toast.makeText(
                    context,
                    if (language == AppLanguage.ODIA)
                        "\"$heard\" ଚିହ୍ନଟ — ୧୧୨ ଜରୁରୀକାଳୀନ କଲ୍ ଆରମ୍ଭ ହେଉଛି!"
                    else
                        "\"$heard\" detected — Initiating Emergency Call to 112!",
                    Toast.LENGTH_LONG
                ).show()
                onTriggerSosCall()
            }
        }
    }

    // Runtime permission launcher for RECORD_AUDIO (+ CALL_PHONE for direct 112 call)
    val voiceGuardPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val micGranted = permissions[Manifest.permission.RECORD_AUDIO] == true ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (micGranted) {
            foregroundVoiceTrigger.setArmed(true)
            foregroundVoiceTrigger.onAppResumed()
            Toast.makeText(
                context,
                if (language == AppLanguage.ODIA)
                    "🎤 ଫୋରଗ୍ରାଉଣ୍ଡ 'Help me' ଭଏସ୍ ଗାର୍ଡ ସକ୍ରିୟ! ଆପ୍ ଖୋଲା ଥିବା ବେଳେ 'Help me' କୁହନ୍ତୁ।"
                else
                    "🎤 Foreground 'Help me' Voice Guard active! Say 'Help me' anytime while app is open to call 112.",
                Toast.LENGTH_LONG
            ).show()
        } else {
            foregroundVoiceTrigger.setArmed(false)
            Toast.makeText(
                context,
                if (language == AppLanguage.ODIA)
                    "'Help me' ଭଏସ୍ ଟ୍ରିଗର୍ ପାଇଁ ମାଇକ୍ରୋଫୋନ୍ ଅନୁମତି ଆବଶ୍ୟକ।"
                else
                    "Microphone permission is required for the foreground 'Help me' voice trigger.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val toggleForegroundVoiceGuard: (Boolean) -> Unit = { enable ->
        if (enable) {
            val hasMic = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
            val hasCall = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED
            if (hasMic) {
                foregroundVoiceTrigger.setArmed(true)
                foregroundVoiceTrigger.onAppResumed()
                if (!hasCall) {
                    voiceGuardPermissionLauncher.launch(arrayOf(Manifest.permission.CALL_PHONE))
                } else {
                    Toast.makeText(
                        context,
                        if (language == AppLanguage.ODIA)
                            "🎤 'Help me' ଭଏସ୍ ଗାର୍ଡ ସକ୍ରିୟ ହେଲା (୧୧୨ ଜରୁରୀ କଲ୍)"
                        else
                            "🎤 Listening in foreground: Say 'Help me' to call 112",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                val needed = mutableListOf(Manifest.permission.RECORD_AUDIO)
                if (!hasCall) needed.add(Manifest.permission.CALL_PHONE)
                voiceGuardPermissionLauncher.launch(needed.toTypedArray())
            }
        } else {
            foregroundVoiceTrigger.setArmed(false)
            Toast.makeText(
                context,
                if (language == AppLanguage.ODIA)
                    "🔇 'Help me' ଭଏସ୍ ଗାର୍ଡ ବନ୍ଦ କରାଗଲା"
                else
                    "🔇 Foreground 'Help me' Voice Guard turned off",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Pulsing animation for the SOS emergency radar beacon
    val infiniteTransition = rememberInfiniteTransition(label = "sos_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sos_scale"
    )

    // Resolve quick GPS coordinates preview
    LaunchedEffect(Unit) {
        EmergencySosManager.getCurrentCoordinates(context) { lat, lng, desc ->
            lastKnownLocationText = "GPS: %.4f, %.4f (%s)".format(Locale.US, lat, lng, desc)
        }
    }

    // Manual one-shot voice recognition launcher fallback
    val voiceSpeechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isVoiceListening = false
        foregroundVoiceTrigger.pauseForAlertConfirmation(false)
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenMatches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = spokenMatches?.firstOrNull() ?: ""
            if (spokenText.isNotBlank()) {
                val isEmergency = EmergencySosManager.isEmergencyVoiceCommand(spokenText)
                if (isEmergency) {
                    vibrateUrgent(context)
                    Toast.makeText(
                        context,
                        if (language == AppLanguage.ODIA)
                            "ସ୍ୱର ଚିହ୍ନଟ: \"$spokenText\" • ୧୧୨ କଲ୍ ଆରମ୍ଭ ହେଉଛି!"
                        else
                            "Emergency Voice Command Recognized: \"$spokenText\" • Calling 112!",
                        Toast.LENGTH_LONG
                    ).show()

                    // Execute immediate 112 dispatch
                    onTriggerSosCall()
                } else {
                    Toast.makeText(
                        context,
                        if (language == AppLanguage.ODIA)
                            "କମାଣ୍ଡ ଚିହ୍ନଟ ହୋଇପାରିଲା ନାହିଁ (\"$spokenText\")। ଦୟାକରି କୁହନ୍ତୁ 'Help me'"
                        else
                            "Unrecognized command (\"$spokenText\"). Please say 'Help me'",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    val startVoiceEmergencyTrigger = {
        try {
            foregroundVoiceTrigger.pauseForAlertConfirmation(true)
            isVoiceListening = true
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (language == AppLanguage.ODIA) "or-IN" else "en-IN")
                putExtra(RecognizerIntent.EXTRA_PROMPT, if (language == AppLanguage.ODIA)
                    "ଜରୁରୀକାଳୀନ କମାଣ୍ଡ କୁହନ୍ତୁ: 'Help me' ବା 'ମୋତେ ସାହାଯ୍ୟ କର'"
                else
                    "Say 'Help me' to call 112 Emergency"
                )
            }
            voiceSpeechLauncher.launch(intent)
        } catch (e: Exception) {
            isVoiceListening = false
            foregroundVoiceTrigger.pauseForAlertConfirmation(false)
            Toast.makeText(
                context,
                "Voice recognizer unavailable: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    var isSirenPlaying by remember { mutableStateOf(false) }

    val executeInstantSosAction = {
        vibrateUrgent(context)
        playHighVolumeEmergencySirenBurst(context)
        isSirenPlaying = true
        Toast.makeText(
            context,
            if (language == AppLanguage.ODIA)
                "🚨 SOS ସାଇରନ୍ + ହାପ୍ଟିକ୍ ସକ୍ରିୟ: ୧୧୨ କଲ୍ ସଂଯୋଗ ହେଉଛି ଏବଂ GPS ଲୋକେସନ୍ ପଠାଗଲା!"
            else
                "🚨 SOS Siren & Haptic Active: Calling 112 & dispatching GPS location to contacts!",
            Toast.LENGTH_LONG
        ).show()

        onTriggerSosCall()
        EmergencySosManager.triggerInstantSosOverlayAction(context) { statusMsg ->
            Toast.makeText(context, statusMsg, Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = modifier
            .testTag("persistent_emergency_sos_overlay_container"),
        contentAlignment = Alignment.BottomEnd
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Bottom
        ) {
            // Expanded SOS Quick-Control Card
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Card(
                    modifier = Modifier
                        .width(320.dp)
                        .padding(bottom = 12.dp)
                        .shadow(16.dp, RoundedCornerShape(20.dp))
                        .testTag("sos_overlay_expanded_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    border = BorderStroke(1.5.dp, Color(0xFFEF4444).copy(alpha = 0.8f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
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
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFDC2626)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Emergency,
                                        contentDescription = "SOS",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (language == AppLanguage.ODIA) "୧୧୨ ଜରୁରୀକାଳୀନ SOS ଡାଏଲର୍" else "Baleshwar 112 SOS Overlay",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            fontSize = 13.5.sp
                                        )
                                    )
                                    Text(
                                        text = if (language == AppLanguage.ODIA) "୧-ଟ୍ୟାପ୍ କଲ୍ + GPS ଲୋକେସନ୍ SMS" else "1-Tap Call + GPS Location SMS",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFFFCA5A5),
                                            fontSize = 10.5.sp
                                        )
                                    )
                                }
                            }
                            IconButton(
                                onClick = { isExpanded = false },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close SOS Menu",
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Primary 1-Tap SOS Button with subtle urgent pulse
                        val instantCallScale by infiniteTransition.animateFloat(
                            initialValue = 1.0f,
                            targetValue = 1.045f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "instant_call_btn_scale"
                        )
                        Button(
                            onClick = {
                                isExpanded = false
                                executeInstantSosAction()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .scale(instantCallScale)
                                .testTag("sos_overlay_instant_call_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (language == AppLanguage.ODIA) "ତୁରନ୍ତ ୧୧୨ କୁ କଲ୍ କରନ୍ତୁ" else "INSTANT CALL 112 & SEND GPS",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // High-Volume Emergency Siren + SOS Haptic Burst Button
                        Button(
                            onClick = {
                                isSirenPlaying = !isSirenPlaying
                                if (isSirenPlaying) {
                                    vibrateUrgent(context)
                                    playHighVolumeEmergencySirenBurst(context)
                                    EmergencySosManager.triggerInstantSosOverlayAction(context) { _ -> }
                                    Toast.makeText(
                                        context,
                                        "🔊 High-Volume Emergency Siren & SOS Haptic Sequence Active!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(context, "🔇 Emergency Siren Muted", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .testTag("sos_overlay_siren_haptic_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSirenPlaying) Color(0xFFB45309) else Color(0xFF991B1B)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (isSirenPlaying) {
                                    if (language == AppLanguage.ODIA) "🔊 ସାଇରନ୍ ବାଜୁଛି (ବନ୍ଦ କରିବାକୁ ଟ୍ୟାପ୍ କରନ୍ତୁ)" else "🔊 SIREN & SOS HAPTIC ACTIVE (TAP TO STOP)"
                                } else {
                                    if (language == AppLanguage.ODIA) "🔊 ଉଚ୍ଚ-ଧ୍ୱନି SOS ସାଇରନ୍ + ହାପ୍ଟିକ୍ ଆଲର୍ଟ" else "🔊 SOUND HIGH-VOLUME SIREN + SOS HAPTIC"
                                },
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Foreground Hands-Free Voice Guard Card: Say "Help me" while app is open -> Calls 112
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (voiceGuardState.isArmed) Color(0xFF0C4A6E) else Color(0xFF0F172A),
                            border = BorderStroke(
                                1.2.dp,
                                if (voiceGuardState.isArmed) Color(0xFF38BDF8) else Color(0xFF475569)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("foreground_voice_help_card")
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = if (voiceGuardState.isArmed) Icons.Default.Mic else Icons.Default.MicOff,
                                            contentDescription = "Foreground Voice Guard",
                                            tint = if (voiceGuardState.isArmed) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = if (language == AppLanguage.ODIA)
                                                    "ଫୋରଗ୍ରାଉଣ୍ଡ 'Help me' → ୧୧୨ ଗାର୍ଡ"
                                                else
                                                    "Foreground 'Help me' → 112 Guard",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = if (voiceGuardState.isArmed) {
                                                    if (language == AppLanguage.ODIA)
                                                        "ସକ୍ରିୟ: ଆପ୍ ଖୋଲା ଥିବା ବେଳେ 'Help me' କୁହନ୍ତୁ"
                                                    else
                                                        "Listening: Say \"Help me\" to call 112"
                                                } else {
                                                    if (language == AppLanguage.ODIA)
                                                        "ଆପ୍ ଖୋଲା ଥିବା ବେଳେ ହ୍ୟାଣ୍ଡସ୍-ଫ୍ରି ୧୧୨ କଲ୍"
                                                    else
                                                        "Hands-free 112 call while app is open"
                                                },
                                                fontSize = 9.5.sp,
                                                color = if (voiceGuardState.isArmed) Color(0xFFBAE6FD) else Color(0xFF94A3B8),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    Switch(
                                        checked = voiceGuardState.isArmed,
                                        onCheckedChange = { toggleForegroundVoiceGuard(it) },
                                        modifier = Modifier.testTag("foreground_voice_help_toggle"),
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = Color(0xFF0284C7),
                                            uncheckedThumbColor = Color(0xFFCBD5E1),
                                            uncheckedTrackColor = Color(0xFF334155)
                                        )
                                    )
                                }

                                if (voiceGuardState.isArmed && voiceGuardState.lastHeardSnippet.isNotBlank()) {
                                    Text(
                                        text = "Last heard: \"${voiceGuardState.lastHeardSnippet}\"",
                                        fontSize = 9.sp,
                                        color = Color(0xFF7DD3FC),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Manual One-Tap Voice Prompt: Say "Help me"
                        OutlinedButton(
                            onClick = {
                                isExpanded = false
                                startVoiceEmergencyTrigger()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("sos_overlay_voice_trigger_btn"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.2.dp, Color(0xFF38BDF8)),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF0F172A))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice Trigger",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (language == AppLanguage.ODIA)
                                    "ଏବେ କୁହନ୍ତୁ: 'Help me' (୧୧୨ କଲ୍)"
                                else
                                    "Tap & Say \"Help me\" (Call 112)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = Color(0xFF38BDF8)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Safety Status Toggle ('Safe' vs 'In Need of Assistance') with GPS Dispatch
                        var isSafetyStatusSafe by remember { mutableStateOf(true) }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSafetyStatusSafe) Color(0xFF064E3B) else Color(0xFF7F1D1D),
                            border = BorderStroke(
                                1.dp,
                                if (isSafetyStatusSafe) Color(0xFF34D399) else Color(0xFFF87171)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    isSafetyStatusSafe = !isSafetyStatusSafe
                                    val statusText = if (isSafetyStatusSafe) {
                                        "SAFE: I am currently safe in Baleshwar ($lastKnownLocationText)"
                                    } else {
                                        "IN NEED OF ASSISTANCE: Please check my Baleshwar GPS location ($lastKnownLocationText)"
                                    }
                                    EmergencySosManager.triggerInstantSosOverlayAction(context) { _ -> }
                                    Toast.makeText(
                                        context,
                                        "Safety Status Broadcast Sent: $statusText",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .testTag("sos_safety_status_toggle_row")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isSafetyStatusSafe) {
                                            if (language == AppLanguage.ODIA) "🟢 ସୁରକ୍ଷା ସ୍ଥିତି: ମୁଁ ସୁରକ୍ଷିତ ଅଛି (Safe)" else "🟢 Safety Status: I Am Safe"
                                        } else {
                                            if (language == AppLanguage.ODIA) "🔴 ସହାୟତା ଆବଶ୍ୟକ (In Need of Assistance)" else "🔴 Status: In Need of Assistance"
                                        },
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Tap to toggle & broadcast GPS coordinates to contacts",
                                        fontSize = 9.5.sp,
                                        color = Color(0xFFE2E8F0)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.White.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = if (isSafetyStatusSafe) "SAFE" else "ASSIST",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Location Preview Bar
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF334155).copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color(0xFF4ADE80),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = lastKnownLocationText,
                                    fontSize = 10.5.sp,
                                    color = Color(0xFFE2E8F0),
                                    maxLines = 1
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Emergency Contacts Bar with edit trigger
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.People,
                                    contentDescription = null,
                                    tint = Color(0xFFCBD5E1),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (language == AppLanguage.ODIA)
                                        "${emergencyContacts.size} ସମ୍ପର୍କୀୟ ସଂଯୁକ୍ତ"
                                    else
                                        "${emergencyContacts.size} Contacts Alerted via SMS",
                                    fontSize = 11.sp,
                                    color = Color(0xFFCBD5E1)
                                )
                            }
                            Text(
                                text = if (language == AppLanguage.ODIA) "ସମ୍ପାଦନା ✎" else "Configure ✎",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8),
                                modifier = Modifier
                                    .clickable {
                                        showConfigureContactsDialog = true
                                    }
                                    .padding(4.dp)
                                    .testTag("sos_configure_contacts_link")
                            )
                        }
                    }
                }
            }

            // Compact Live Foreground Listening Chip when 'Help me' Voice Guard is Armed
            AnimatedVisibility(visible = voiceGuardState.isArmed && !isExpanded) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF0C4A6E),
                    border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                        .shadow(6.dp, RoundedCornerShape(16.dp))
                        .clickable { toggleForegroundVoiceGuard(false) }
                        .testTag("foreground_voice_listening_status_chip")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (voiceGuardState.isListening) Color(0xFF4ADE80) else Color(0xFF38BDF8))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == AppLanguage.ODIA)
                                "ଶୁଣୁଛି: 'Help me' → ୧୧୨"
                            else
                                "Listening: \"Help me\" → 112",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }

            // Persistent Floating Pill Overlay
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                // Quick Foreground 'Help me' Voice Guard Toggle Button (48.dp touch target)
                Surface(
                    onClick = { toggleForegroundVoiceGuard(!voiceGuardState.isArmed) },
                    shape = CircleShape,
                    color = if (voiceGuardState.isArmed) Color(0xFF0284C7) else Color(0xFF0F172A),
                    border = BorderStroke(
                        1.5.dp,
                        if (voiceGuardState.isArmed) Color(0xFF4ADE80) else Color(0xFF38BDF8)
                    ),
                    modifier = Modifier
                        .size(48.dp)
                        .scale(if (voiceGuardState.isArmed && voiceGuardState.isListening) pulseScale else 1f)
                        .shadow(8.dp, CircleShape)
                        .testTag("floating_quick_voice_sos_btn")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (voiceGuardState.isArmed) Icons.Default.Mic else Icons.Default.MicOff,
                            contentDescription = if (voiceGuardState.isArmed) {
                                "Foreground Help me Voice Guard Active — tap to turn off"
                            } else {
                                "Enable Foreground Help me Voice Guard for 112"
                            },
                            tint = if (voiceGuardState.isArmed) Color.White else Color(0xFF38BDF8),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Primary Persistent SOS Floating Pill (Glowing & Animated)
                Surface(
                    onClick = {
                        // Tapping toggles menu; double tap or direct call button inside initiates
                        isExpanded = !isExpanded
                    },
                    shape = RoundedCornerShape(24.dp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .scale(if (isExpanded) 1f else pulseScale)
                        .shadow(12.dp, RoundedCornerShape(24.dp))
                        .testTag("persistent_sos_floating_pill")
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFDC2626),
                                        Color(0xFFB91C1C)
                                    )
                                )
                            )
                            .border(1.5.dp, Color(0xFFFCA5A5), RoundedCornerShape(24.dp))
                            .padding(horizontal = 14.dp, vertical = 9.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "SOS Call",
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SOS 112",
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                letterSpacing = 0.5.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "•",
                                color = Color(0xFFFCA5A5),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (language == AppLanguage.ODIA) "ଜରୁରୀ" else "TAP",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFFECEB)
                            )
                        }
                    }
                }
            }
        }
    }

    // Configure Emergency Contacts Dialog
    if (showConfigureContactsDialog) {
        var contact1Name by remember { mutableStateOf(emergencyContacts.getOrNull(0)?.nameEn ?: "") }
        var contact1Phone by remember { mutableStateOf(emergencyContacts.getOrNull(0)?.phoneNumber ?: "") }
        var contact2Name by remember { mutableStateOf(emergencyContacts.getOrNull(1)?.nameEn ?: "") }
        var contact2Phone by remember { mutableStateOf(emergencyContacts.getOrNull(1)?.phoneNumber ?: "") }

        AlertDialog(
            onDismissRequest = { showConfigureContactsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Color(0xFFDC2626)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (language == AppLanguage.ODIA) "ଜରୁରୀକାଳୀନ ସମ୍ପର୍କୀୟ ସେଟିଂସ" else "Configure SOS Contacts",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = if (language == AppLanguage.ODIA)
                            "ଏହି ନମ୍ବରଗୁଡ଼ିକୁ ଆପଣଙ୍କ ଲାଇଭ୍ GPS ଲୋକେସନ୍ ଲିଙ୍କ୍ ସହ ସ୍ୱୟଂକ୍ରିୟ SOS SMS ପଠାଯିବ:"
                        else
                            "Automated SOS SMS with live Google Maps GPS coordinates will be sent to these contacts:",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, color = Color.Gray)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = contact1Name,
                        onValueChange = { contact1Name = it },
                        label = { Text("Primary Contact Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFDC2626)
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = contact1Phone,
                        onValueChange = { contact1Phone = it },
                        label = { Text("Primary Phone Number (e.g. 9437012345)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFDC2626)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = contact2Name,
                        onValueChange = { contact2Name = it },
                        label = { Text("Secondary Contact Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFDC2626)
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = contact2Phone,
                        onValueChange = { contact2Phone = it },
                        label = { Text("Secondary Phone Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFDC2626)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updatedList = mutableListOf<EmergencySosContact>()
                        if (contact1Phone.isNotBlank()) {
                            updatedList.add(
                                EmergencySosContact(
                                    id = "contact_1",
                                    nameEn = contact1Name.ifBlank { "Primary Contact" },
                                    nameOr = contact1Name.ifBlank { "Primary Contact" },
                                    relationEn = "Emergency Contact",
                                    relationOr = "ଜରୁରୀ ସମ୍ପର୍କୀୟ",
                                    phoneNumber = contact1Phone.trim(),
                                    isPrimary = true
                                )
                            )
                        }
                        if (contact2Phone.isNotBlank()) {
                            updatedList.add(
                                EmergencySosContact(
                                    id = "contact_2",
                                    nameEn = contact2Name.ifBlank { "Secondary Contact" },
                                    nameOr = contact2Name.ifBlank { "Secondary Contact" },
                                    relationEn = "Emergency Contact",
                                    relationOr = "ଜରୁରୀ ସମ୍ପର୍କୀୟ",
                                    phoneNumber = contact2Phone.trim(),
                                    isPrimary = false
                                )
                            )
                        }
                        emergencyContacts = updatedList
                        EmergencySosManager.saveSosContacts(context, updatedList)
                        showConfigureContactsDialog = false
                        Toast.makeText(context, "SOS Emergency Contacts Saved", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Save Contacts", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfigureContactsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 3-Second Auto-Call 112 Confirmation Dialog triggered when user says "Help me" in foreground
    pendingVoiceHelpPhrase?.let { heardPhrase ->
        AlertDialog(
            onDismissRequest = {
                pendingVoiceHelpPhrase = null
                foregroundVoiceTrigger.pauseForAlertConfirmation(false)
            },
            modifier = Modifier.testTag("voice_help_112_countdown_dialog"),
            containerColor = Color(0xFF1E293B),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDC2626)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Emergency Call 112",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (language == AppLanguage.ODIA)
                            "\"$heardPhrase\" ଶୁଣାଗଲା — ୧୧୨ କଲ୍!"
                        else
                            "Heard \"$heardPhrase\" — Calling 112!",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (language == AppLanguage.ODIA)
                            "ଆପ୍ ଫୋରଗ୍ରାଉଣ୍ଡ ଭଏସ୍ ଟ୍ରିଗର୍ ସକ୍ରିୟ ହୋଇଛି। $voiceCallCountdownSeconds ସେକେଣ୍ଡରେ ୧୧୨ ଜରୁରୀକାଳୀନ କଲ୍ ସଂଯୋଗ ହେବ..."
                        else
                            "Foreground voice trigger activated. Automatically initiating emergency call to 112 in $voiceCallCountdownSeconds seconds...",
                        color = Color(0xFFE2E8F0),
                        fontSize = 13.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF7F1D1D),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (language == AppLanguage.ODIA)
                                "🚨 ୧୧୨ ଜରୁରୀକାଳୀନ କଲ୍: ${voiceCallCountdownSeconds}s"
                            else
                                "🚨 Dialing Emergency 112 in ${voiceCallCountdownSeconds}s",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingVoiceHelpPhrase = null
                        foregroundVoiceTrigger.pauseForAlertConfirmation(false)
                        onTriggerSosCall()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    modifier = Modifier.testTag("voice_help_call_now_btn")
                ) {
                    Text(
                        text = if (language == AppLanguage.ODIA) "ଏବେ ୧୧୨ କଲ୍ କରନ୍ତୁ" else "Call 112 Now",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        pendingVoiceHelpPhrase = null
                        foregroundVoiceTrigger.pauseForAlertConfirmation(false)
                        Toast.makeText(
                            context,
                            if (language == AppLanguage.ODIA) "୧୧୨ କଲ୍ ବାତିଲ୍ କରାଗଲା" else "112 Emergency Call cancelled",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier.testTag("voice_help_cancel_btn")
                ) {
                    Text(
                        text = if (language == AppLanguage.ODIA) "ବାତିଲ୍ କରନ୍ତୁ" else "Cancel",
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}

/**
 * Triggers a distinct long-vibration SOS haptic sequence (... --- ...) on SOS activation.
 */
private fun vibrateUrgent(context: Context) {
    com.example.util.BalasoreHapticHelper.vibrateEmergencySos(context)
}

/**
 * Plays a high-volume emergency siren alert burst over the alarm stream simultaneously with SOS activation.
 */
private fun playHighVolumeEmergencySirenBurst(context: Context) {
    try {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager
        audioManager?.let { am ->
            val maxVol = am.getStreamMaxVolume(android.media.AudioManager.STREAM_ALARM)
            if (maxVol > 0) {
                am.setStreamVolume(android.media.AudioManager.STREAM_ALARM, maxVol, 0)
            }
        }
        val toneGen = android.media.ToneGenerator(android.media.AudioManager.STREAM_ALARM, 100)
        toneGen.startTone(android.media.ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 1800)
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            try {
                toneGen.release()
            } catch (_: Exception) {}
        }, 2000L)
    } catch (_: Exception) {}
}
