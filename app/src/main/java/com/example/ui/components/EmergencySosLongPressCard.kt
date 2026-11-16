package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.emergency.EmergencySosContact
import com.example.data.emergency.EmergencySosManager
import com.example.data.emergency.SosTriggerResult
import com.example.data.model.AppLanguage
import com.example.ui.theme.BentoRedBg
import com.example.ui.theme.BentoRedText
import com.example.ui.theme.BentoSlate100
import com.example.ui.theme.BentoSlate500
import com.example.ui.theme.BentoSlate600
import com.example.ui.theme.BentoSlate700
import com.example.ui.theme.BentoSlate800
import com.example.ui.theme.BentoSlate900
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Long-Press Emergency SOS Action Component.
 *
 * Implements:
 * 1. Long-press gesture (1.5 seconds) with animated circular progress and haptic vibration feedback.
 * 2. Instant GPS coordinate fetching.
 * 3. SMS dispatch with Google Maps location link to pre-set emergency contacts.
 * 4. Automatic telephony call to nearest police control room (e.g. 112 / Balasore Town Police).
 * 5. Configuration modal to manage pre-set contacts and preview the emergency dispatch.
 */
@Composable
fun EmergencySosLongPressCard(
    language: AppLanguage,
    onSosTriggered: (SosTriggerResult) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isPressing by remember { mutableStateOf(false) }
    var pressProgress by remember { mutableFloatStateOf(0f) }
    var isTriggering by remember { mutableStateOf(false) }
    var lastResult by remember { mutableStateOf<SosTriggerResult?>(null) }
    var showSosResultDialog by remember { mutableStateOf(false) }
    var showConfigureContactsDialog by remember { mutableStateOf(false) }

    // Pre-set contacts list
    var contactsList by remember {
        mutableStateOf(EmergencySosManager.DEFAULT_SOS_CONTACTS)
    }

    // Permission launcher for Location and SMS
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Permissions handled gracefully inside EmergencySosManager
    }

    // Vibrator for tactile haptic feedback during long press
    val vibrator = remember {
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    // Pulsing transition for visual emergency readiness
    val infiniteTransition = rememberInfiniteTransition(label = "sos_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Long press timer loop
    LaunchedEffect(isPressing) {
        if (isPressing && !isTriggering) {
            val totalDurationMs = 1500L
            val stepMs = 50L
            var elapsed = 0L

            while (isPressing && elapsed < totalDurationMs) {
                delay(stepMs)
                elapsed += stepMs
                pressProgress = (elapsed.toFloat() / totalDurationMs).coerceIn(0f, 1f)

                // Haptic tick
                if (elapsed % 300 == 0L) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator?.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator?.vibrate(30)
                        }
                    } catch (_: Exception) {}
                }
            }

            if (pressProgress >= 1f && !isTriggering) {
                // Completed long press! Trigger SOS immediately
                isTriggering = true
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 150, 100, 250), -1))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator?.vibrate(300)
                    }
                } catch (_: Exception) {}

                EmergencySosManager.triggerSos(context, contactsList) { result ->
                    isTriggering = false
                    isPressing = false
                    pressProgress = 0f
                    lastResult = result
                    showSosResultDialog = true
                    onSosTriggered(result)
                }
            }
        } else {
            pressProgress = 0f
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("emergency_sos_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
        border = BorderStroke(1.5.dp, Color(0xFFFCA5A5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Title & Contact Settings Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BentoRedText
                    ) {
                        Text(
                            text = "EMERGENCY SOS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp,
                                color = Color.White
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (language == AppLanguage.ODIA) "ଦୀର୍ଘ ସମୟ ଦବାନ୍ତୁ" else "Hold 1.5s to Alert",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF991B1B)
                        )
                    )
                }

                // Settings icon to configure pre-set emergency SMS recipients
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFFECACA)),
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .clickable { showConfigureContactsDialog = true }
                        .testTag("sos_settings_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configure SOS Contacts",
                            tint = Color(0xFFB91C1C),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Interactive Long-Press SOS Trigger Pad
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Interactive Big Red Button
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .scale(if (isPressing) 1f else pulseScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color(0xFFEF4444),
                                    Color(0xFFDC2626),
                                    Color(0xFF991B1B)
                                )
                            )
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    permissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION,
                                            Manifest.permission.SEND_SMS
                                        )
                                    )
                                    isPressing = true
                                    tryAwaitRelease()
                                    isPressing = false
                                }
                            )
                        }
                        .testTag("sos_long_press_button"),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer progress ring indicating hold duration
                    if (isPressing || pressProgress > 0f) {
                        CircularProgressIndicator(
                            progress = { pressProgress },
                            modifier = Modifier.size(92.dp),
                            color = Color.Yellow,
                            strokeWidth = 5.dp,
                            trackColor = Color.White.copy(alpha = 0.3f)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isTriggering) Icons.Default.Sync else Icons.Default.CrisisAlert,
                            contentDescription = "SOS Button",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = if (isTriggering) "SENDING..." else if (isPressing) "${(pressProgress * 100).toInt()}%" else "SOS",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    }
                }

                // Description & Action Summary Column
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (language == AppLanguage.ODIA)
                            "ତୁରନ୍ତ GPS ସ୍ଥିତି SMS ମାଧ୍ୟମରେ ପ୍ରେରଣ ଓ ୧୧୨ ପୋଲିସ କଣ୍ଟ୍ରୋଲ୍ ରୁମ୍ କଲ୍"
                        else
                            "Shares live GPS coordinates via SMS to contacts & automatically calls nearest Police",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF7F1D1D),
                            lineHeight = 17.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Contact Badges Preview
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                        ) {
                            Text(
                                text = "📱 ${contactsList.size} SMS Contacts",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFEE2E2),
                            border = BorderStroke(1.dp, Color(0xFFF87171))
                        ) {
                            Text(
                                text = "📞 Dial 112 / Police",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7F1D1D),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog 1: SOS Triggered Result Confirmation
    if (showSosResultDialog && lastResult != null) {
        val result = lastResult!!
        AlertDialog(
            onDismissRequest = { showSosResultDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF16A34A),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = if (language == AppLanguage.ODIA) "ଜରୁରୀକାଳୀନ SOS ପ୍ରେରିତ!" else "Emergency SOS Dispatched!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.ODIA)
                            "ଆପଣଙ୍କ GPS କୋର୍ଡିନେଟ୍ ସହ SMS ପ୍ରେରଣ କରାଗଲା ଏବଂ ନିକଟସ୍ଥ ପୋଲିସ ଥାନାକୁ କଲ୍ ଆରମ୍ଭ ହେଲା।"
                        else
                            "GPS coordinates were dispatched via SMS to all pre-set contacts and a call was placed to the nearest police control room.",
                        fontSize = 12.5.sp,
                        color = BentoSlate700
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = BentoSlate100,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "📍 GPS Location:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = BentoSlate900
                            )
                            Text(
                                text = "Lat: %.5f, Lng: %.5f".format(Locale.US, result.latitude, result.longitude),
                                fontSize = 11.5.sp,
                                color = Color(0xFF0369A1)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "🚔 Police Station: ${result.nearestPoliceStationName} (${result.nearestPoliceNumber})",
                                fontSize = 11.sp,
                                color = Color(0xFF991B1B),
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "✉️ SMS Recipients: ${result.smsRecipients.joinToString(", ")}",
                                fontSize = 10.5.sp,
                                color = BentoSlate700
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSosResultDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("OK")
                }
            }
        )
    }

    // Dialog 2: Configure Pre-set Emergency Contacts
    if (showConfigureContactsDialog) {
        var contact1Name by remember { mutableStateOf(contactsList.getOrNull(0)?.nameEn ?: "") }
        var contact1Phone by remember { mutableStateOf(contactsList.getOrNull(0)?.phoneNumber ?: "") }
        var contact2Name by remember { mutableStateOf(contactsList.getOrNull(1)?.nameEn ?: "") }
        var contact2Phone by remember { mutableStateOf(contactsList.getOrNull(1)?.phoneNumber ?: "") }

        AlertDialog(
            onDismissRequest = { showConfigureContactsDialog = false },
            title = {
                Text(
                    text = if (language == AppLanguage.ODIA) "ଜରୁରୀକାଳୀନ ସମ୍ପର୍କ ନମ୍ବର (Pre-set)" else "Pre-set Emergency Contacts",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Enter phone numbers to receive emergency SMS messages containing your live GPS location link when SOS is triggered.",
                        fontSize = 12.sp,
                        color = BentoSlate600
                    )

                    OutlinedTextField(
                        value = contact1Name,
                        onValueChange = { contact1Name = it },
                        label = { Text("Contact 1 Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = contact1Phone,
                        onValueChange = { contact1Phone = it },
                        label = { Text("Contact 1 Phone Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Divider(color = Color(0xFFE2E8F0))

                    OutlinedTextField(
                        value = contact2Name,
                        onValueChange = { contact2Name = it },
                        label = { Text("Contact 2 Name (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = contact2Phone,
                        onValueChange = { contact2Phone = it },
                        label = { Text("Contact 2 Phone Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newContacts = mutableListOf<EmergencySosContact>()
                        if (contact1Phone.isNotBlank()) {
                            newContacts.add(
                                EmergencySosContact(
                                    id = "sos_contact_1",
                                    nameEn = contact1Name.ifBlank { "Emergency Contact 1" },
                                    nameOr = contact1Name.ifBlank { "ଜରୁରୀକାଳୀନ ସମ୍ପର୍କ ୧" },
                                    relationEn = "Guardian",
                                    relationOr = "ଅଭିଭାବକ",
                                    phoneNumber = contact1Phone,
                                    isPrimary = true
                                )
                            )
                        }
                        if (contact2Phone.isNotBlank()) {
                            newContacts.add(
                                EmergencySosContact(
                                    id = "sos_contact_2",
                                    nameEn = contact2Name.ifBlank { "Emergency Contact 2" },
                                    nameOr = contact2Name.ifBlank { "ଜରୁରୀକାଳୀନ ସମ୍ପର୍କ ୨" },
                                    relationEn = "Secondary",
                                    relationOr = "ଦ୍ୱିତୀୟ ସମ୍ପର୍କ",
                                    phoneNumber = contact2Phone,
                                    isPrimary = false
                                )
                            )
                        }
                        if (newContacts.isNotEmpty()) {
                            contactsList = newContacts
                        }
                        showConfigureContactsDialog = false
                        Toast.makeText(context, "Emergency SOS contacts updated", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Save Contacts")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfigureContactsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
