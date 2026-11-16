package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.OceanBlueDark

/**
 * Interactive In-App Privacy Policy Sheet for Baleshwar 360.
 * Complies with Google Play Developer Program Policies (User Data, Permissions,
 * Account Deletion, Families Policy, and Third-Party Disclosures).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicySheet(
    language: AppLanguage,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isOdia = language == AppLanguage.ODIA
    val privacyWebUrl = "https://ais-pre-honzkaxc7yscu2h42zeagl-613265325843.asia-east1.run.app/privacy"

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = modifier.testTag("privacy_policy_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // --- HEADER ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF10B981).copy(alpha = 0.12f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = if (isOdia) "ଗୋପନୀୟତା ନୀତି" else "Privacy Policy",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 19.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFDCFCE7)
                            ) {
                                Text(
                                    text = "Google Play Verified",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF166534),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = if (isOdia)
                                "Baleshwar 360 ତଥ୍ୟ ସୁରକ୍ଷା ଓ ଅଧିକାର"
                            else
                                "Baleshwar 360 Data Transparency & Security",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("close_privacy_policy_sheet")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Publisher Entity & Contact Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isOdia) "ଆପ୍ ପ୍ରକାଶକ ଓ ଯୋଗାଯୋଗ ବିବରଣୀ" else "PUBLISHER & PRIVACY CONTACT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Publisher: Nihar Sales (Balasore, Odisha, India)",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Privacy Point of Contact: niharbhuyan@gmail.com",
                        fontSize = 11.5.sp,
                        color = OceanBlue
                    )
                    Text(
                        text = "Effective Date: October 19, 2026 • Version: ${com.example.BuildConfig.VERSION_NAME}",
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 1: PERMISSIONS & SPECIFIC JUSTIFICATIONS
            Text(
                text = if (isOdia) "ଅନୁମତି ଓ ତଥ୍ୟ ବ୍ୟବହାର କାରଣ" else "PERMISSIONS & PURPOSE SPECIFICATION",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Location Permission
                PrivacyPermissionItem(
                    icon = Icons.Default.LocationOn,
                    iconTint = Color(0xFFEF4444),
                    title = if (isOdia) "ସ୍ଥାନ ଅନୁମତି (Location - Foreground Only)" else "Location (Foreground Only)",
                    permissionName = "ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION",
                    description = if (isOdia)
                        "ନିକଟତମ ବାତ୍ୟା ଆଶ୍ରୟସ୍ଥଳୀ (MCS), ଡାକ୍ତରଖାନା ଓ ଚାନ୍ଦିପୁର ଜୁଆର-ଭଟ୍ଟା ସୁରକ୍ଷିତ ଚଲାପଥ ଦୂରତା ଗଣନା ପାଇଁ କେବଳ ଆପ୍ ଚାଲୁଥିବା ସମୟରେ ବ୍ୟବହୃତ ହୁଏ। ବ୍ୟାକଗ୍ରାଉଣ୍ଡ ଟ୍ରାକିଂ କରାଯାଏ ନାହିଁ।"
                    else
                        "Used strictly while the app or widget is active to calculate distance to the nearest Multipurpose Cyclone Shelter (MCS), hospitals, and Chandipur tidal recession window. Never tracked in background or sold to advertisers."
                )

                // Camera Permission
                PrivacyPermissionItem(
                    icon = Icons.Default.CameraAlt,
                    iconTint = Color(0xFF8B5CF6),
                    title = if (isOdia) "କ୍ୟାମେରା ଅନୁମତି (ML Kit QR Scanner)" else "Camera (On-Device QR Scanner)",
                    permissionName = "CAMERA",
                    description = if (isOdia)
                        "ବାଲେଶ୍ୱର ପର୍ଯ୍ୟଟନ ପୋଷ୍ଟର୍ ଓ ସରକାରୀ ସୂଚନାପତ୍ର QR କୋଡ୍ ସ୍କାନ୍ କରିବା ପାଇଁ Google ML Kit ସାହାଯ୍ୟରେ ଅନ୍-ଡିଭାଇସ୍ ପ୍ରୋସେସ୍ ହୁଏ। କୌଣସି ଫଟୋ ବା ଭିଡିଓ ସେଭ୍ କିମ୍ବା ଅପଲୋଡ୍ ହୁଏ ନାହିଁ।"
                    else
                        "Powers ML Kit on-device scanning of official Balasore tourism posters and event QR codes. Processed transiently in memory; no images or video recordings are ever captured, saved, or uploaded."
                )

                // Microphone Permission
                PrivacyPermissionItem(
                    icon = Icons.Default.Mic,
                    iconTint = Color(0xFF0284C7),
                    title = if (isOdia) "ମାଇକ୍ରୋଫୋନ୍ (Voice Search)" else "Microphone (Voice Search Filter)",
                    permissionName = "RECORD_AUDIO",
                    description = if (isOdia)
                        "ଖବର ଓ ପର୍ଯ୍ୟଟନ ସ୍ଥାନ ଖୋଜିବା ପାଇଁ ଭଏସ୍-ଟୁ-ଟେକ୍ସଟ୍ ଇନପୁଟ୍ ପାଇଁ ବ୍ୟବହୃତ ହୁଏ। ଅଡିଓ ସାଙ୍ଗେ ସାଙ୍ଗେ ନଷ୍ଟ କରାଯାଏ।"
                    else
                        "Used solely for voice-to-text search input in English and Odia. Audio is processed ephemerally on-device or via system speech recognition and immediately discarded."
                )

                // Emergency Calling & SMS
                PrivacyPermissionItem(
                    icon = Icons.Default.Emergency,
                    iconTint = Color(0xFFDC2626),
                    title = if (isOdia) "ଜରୁରୀକାଳୀନ କଲ୍ ଓ SMS" else "Emergency SOS Calling & SMS",
                    permissionName = "CALL_PHONE, SEND_SMS",
                    description = if (isOdia)
                        "ବାତ୍ୟା କିମ୍ବା ବିପର୍ଯ୍ୟୟ ସମୟରେ ୧୧୨, ୧୦୭୭ ବା ଭରସାଯୋଗ୍ୟ ଯୋଗାଯୋଗକୁ ସିଧାସଳଖ ଫୋନ୍ କଲ୍ ବା SMS ପଠାଇବା ପାଇଁ କେବଳ ଉପଭୋକ୍ତାଙ୍କ ହାତରେ ସକ୍ରିୟ ହୁଏ।"
                    else
                        "User-initiated only for dialing district cyclone helplines (112, 1077, 108) or dispatching emergency SOS coordinates. Never triggered silently in the background."
                )

                // Battery Telemetry
                PrivacyPermissionItem(
                    icon = Icons.Default.BatteryChargingFull,
                    iconTint = Color(0xFF10B981),
                    title = if (isOdia) "ବ୍ୟାଟେରୀ ହାର୍ଡୱେର୍ ମନିଟରିଂ" else "Battery Hardware Telemetry",
                    permissionName = "Android BatteryManager API",
                    description = if (isOdia)
                        "ସ୍ମାର୍ଟ ସିଙ୍କ୍ ମୋଡ୍ ବ୍ୟାଟେରୀ ୨୦% ତଳକୁ ଖସିଲେ ବ୍ୟାକଗ୍ରାଉଣ୍ଡ ଟାସ୍କ ବନ୍ଦ କରି ଚାର୍ଜ ରକ୍ଷା କରେ। ସମସ୍ତ ତଥ୍ୟ ଫୋନରେ ହିଁ ରହେ।"
                    else
                        "Battery levels, voltage, and temperature are read locally on-device by SyncManager to throttle background tasks when battery is below 20% or screen is off. Never uploaded off-device."
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // SECTION 2: ZERO-PERMISSION MEDIA & LOCAL ROOM STORAGE
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.FolderShared, contentDescription = null, tint = OceanBlue, modifier = Modifier.size(20.dp))
                        Text(
                            text = if (isOdia) "ଜିରୋ-ଅନୁମତି ଫଟୋ ପିକର୍ ଓ ଅଫଲାଇନ୍ ସଞ୍ଚୟ" else "Zero-Permission Media & Offline Storage",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isOdia)
                            "• ଆମେ କୌଣସି ବ୍ୟାପକ ଷ୍ଟୋରେଜ୍ ଅନୁମତି (READ_EXTERNAL_STORAGE) ମାଗୁନାହୁଁ। କେବଳ ଆଣ୍ଡ୍ରଏଡ୍ ସିଷ୍ଟମ୍ ଫଟୋ ପିକର୍ ବ୍ୟବହୃତ ହୁଏ।\n• ସମସ୍ତ ବୁକମାର୍କ, ଅଫଲାଇନ୍ ମାନଚିତ୍ର ଓ ଖବର ତଥ୍ୟ ଫୋନର ଏନକ୍ରିପ୍ଟେଡ୍ SQLite Room ଡାଟାବେସରେ ସୁରକ୍ଷିତ ରହେ।"
                        else
                            "• Zero-Permission Media Selection: We adhere strictly to Google Play storage policies by utilizing the Android system Photo Picker (PickVisualMedia). We never request broad storage access.\n• Offline-First Sandbox: All bookmarked news, historical tidal trends, bus timetables, and vector map tiles are stored in a sandboxed SQLite Room database on your device.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // SECTION 3: THIRD-PARTY SERVICES & SDK DISCLOSURES
            Text(
                text = if (isOdia) "ତୃତୀୟ-ପକ୍ଷ SDK ଓ ସେବା ବିବରଣୀ" else "THIRD-PARTY SDK DISCLOSURES",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ThirdPartySdkCard(
                    sdkName = "Google AdMob",
                    category = "Community Advertising",
                    details = "Serves non-personalized banner and native community ads. Configured with 'google_analytics_adid_collection_enabled = false' and delayed initialization in compliance with Google Play Families & Privacy guidelines.",
                    privacyUrl = "https://policies.google.com/technologies/ads"
                )

                ThirdPartySdkCard(
                    sdkName = "Firebase Cloud Messaging (FCM)",
                    category = "Emergency Cyclone & Weather Push Alerts",
                    details = "Uses an anonymous device registration token to broadcast critical IMD coastal cyclone warnings and flood alerts. Can be toggled off at any time.",
                    privacyUrl = "https://policies.google.com/privacy"
                )

                ThirdPartySdkCard(
                    sdkName = "Firebase Analytics",
                    category = "Aggregated Usage Telemetry",
                    details = "Aggregates anonymous screen views and feature engagement counts to help prioritize regional community updates. Does not collect personal identifying information.",
                    privacyUrl = "https://policies.google.com/privacy"
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // SECTION 4: USER RIGHTS & 14-DAY DATA DELETION COMMITMENT
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                border = BorderStroke(1.2.dp, Color(0xFFFCA5A5)),
                modifier = Modifier.fillMaxWidth().testTag("user_data_deletion_rights_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(22.dp))
                        Text(
                            text = if (isOdia) "ଉପଭୋକ୍ତା ତଥ୍ୟ ଅଧିକାର ଓ ବିଲୋପ (Data Deletion)" else "Data Deletion Rights (14-Day Commitment)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = Color(0xFF991B1B)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isOdia)
                            "• ଆପଣ ଯେକୌଣସି ସମୟରେ Settings -> Purge Cache ଦ୍ୱାରା ଫୋନରୁ ସମସ୍ତ ତଥ୍ୟ ସଫା କରିପାରିବେ।\n• ଯଦି ଆପଣ ନିଜ ଖାତା ବା ସମୀକ୍ଷା ବିଲୋପ କରିବାକୁ ଚାହାନ୍ତି, ତେବେ niharbhuyan@gmail.com କୁ 'Data Deletion Request' ବିଷୟ ସହିତ ଇମେଲ୍ କରନ୍ତୁ। ଆମେ ୧୪ କାର୍ଯ୍ୟଦିବସ ମଧ୍ୟରେ ସମସ୍ତ ତଥ୍ୟ ସ୍ଥାୟୀ ଭାବେ ବିଲୋପ କରିବୁ।"
                        else
                            "• In-App Cache Purge: Purge all cached news and vector maps instantly from App Settings.\n• Permanent Account & Data Deletion: Under Google Play User Data policies, users can request permanent deletion of their account, feedback, or public reviews. Send an email to niharbhuyan@gmail.com with subject 'Data Deletion Request'. We verify and permanently purge all associated data within 14 calendar days.",
                        fontSize = 11.sp,
                        color = Color(0xFF7F1D1D),
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:niharbhuyan@gmail.com")
                                putExtra(Intent.EXTRA_SUBJECT, "Baleshwar 360 - Data Deletion Request")
                                putExtra(Intent.EXTRA_TEXT, "Hello Privacy Team,\n\nPlease permanently delete all personal data and reviews associated with my device/account in Baleshwar 360.\n\nUser identifier:\nDate:")
                            }
                            try {
                                context.startActivity(emailIntent)
                            } catch (_: Exception) {
                                Toast.makeText(context, "Contact: niharbhuyan@gmail.com", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("request_data_deletion_btn")
                    ) {
                        Icon(Icons.Default.Mail, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isOdia) "ତଥ୍ୟ ବିଲୋପ ଅନୁରୋଧ କରନ୍ତୁ" else "Request Data Deletion",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // SECTION 5: CHILDREN'S PRIVACY & SECURITY
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isOdia) "ଶିଶୁ ସୁରକ୍ଷା ଓ ଏନକ୍ରିପ୍ସନ୍ (COPPA & Security)" else "CHILDREN'S PRIVACY & ENCRYPTION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isOdia)
                            "• Baleshwar 360 ସାଧାରଣ ନାଗରିକଙ୍କ ପାଇଁ ଉଦ୍ଦିଷ୍ଟ ଏବଂ ୧୩ ବର୍ଷରୁ କମ୍ ଶିଶୁଙ୍କ ତଥ୍ୟ ସଂଗ୍ରହ କରେ ନାହିଁ।\n• ସମସ୍ତ ବାହ୍ୟ ନେଟୱାର୍କ ସଂଯୋଗ HTTPS / TLS 1.3 ଏନକ୍ରିପ୍ସନ୍ ଦ୍ୱାରା ସୁରକ୍ଷିତ।"
                        else
                            "• Children's Privacy: In compliance with COPPA and Google Play Families policies, the app does not knowingly collect personal information from children under the age of 13.\n• Security Standards: 100% of external network transmissions are encrypted in transit via HTTPS / TLS 1.3.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ACTION BUTTONS (OPEN OFFICIAL WEB POLICY & CONTACT)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyWebUrl))
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            Toast.makeText(context, "URL: $privacyWebUrl", Toast.LENGTH_LONG).show()
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
                    modifier = Modifier.weight(1f).testTag("open_web_privacy_policy_btn")
                ) {
                    Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isOdia) "ୱେବ୍ ନୀତି ଖୋଲନ୍ତୁ ↗" else "Web Policy ↗",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                OutlinedButton(
                    onClick = onClose,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).testTag("privacy_policy_done_btn")
                ) {
                    Text(
                        text = if (isOdia) "ବନ୍ଦ କରନ୍ତୁ" else "Close",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun PrivacyPermissionItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    permissionName: String,
    description: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = iconTint.copy(alpha = 0.12f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                    }
                }

                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = permissionName,
                        fontSize = 9.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun ThirdPartySdkCard(
    sdkName: String,
    category: String,
    details: String,
    privacyUrl: String
) {
    val context = LocalContext.current

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = sdkName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = category,
                        fontSize = 9.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "Policy ↗",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = OceanBlue,
                    modifier = Modifier.clickable {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyUrl))
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = details,
                fontSize = 10.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 14.5.sp
            )
        }
    }
}
