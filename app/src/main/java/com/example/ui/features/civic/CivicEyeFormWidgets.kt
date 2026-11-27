package com.example.ui.features.civic

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.viewmodel.CivicEyeSubmitStatus
import com.google.firebase.auth.FirebaseUser
import java.util.Locale

@Composable
fun CivicEyeSubmitStatusBanner(
    status: CivicEyeSubmitStatus,
    isOdia: Boolean,
    onViewFeed: () -> Unit,
    onDismiss: () -> Unit,
    onViewOnMap: ((String) -> Unit)? = null
) {
    when (status) {
        is CivicEyeSubmitStatus.SyncedToFirestore -> {
            val coordSummary = if (status.latitude != null && status.longitude != null) {
                String.format(Locale.US, " • GPS %.4f, %.4f", status.latitude, status.longitude)
            } else {
                ""
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("civic_eye_submit_success_banner")
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CloudDone,
                            contentDescription = "Saved to Firestore",
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isOdia) "Firestore ରେ ସଫଳତାର ସହ ସେଭ୍ ହୋଇଛି!" else "Incident Saved to Firestore!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF15803D)
                            )
                            Text(
                                text = "ID: ${status.reportId}$coordSummary${if (status.hasPhoto) " • Photo attached" else ""}",
                                fontSize = 11.5.sp,
                                color = Color(0xFF166534)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (onViewOnMap != null) {
                            TextButton(
                                onClick = { onViewOnMap(status.reportId) },
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("civic_eye_banner_view_map_btn")
                            ) {
                                Text(
                                    text = stringResource(R.string.civic_osm_map_view_on_map),
                                    fontSize = 12.sp,
                                    color = Color(0xFF0284C7),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        TextButton(
                            onClick = onViewFeed,
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text("View Feed", fontSize = 12.sp, color = Color(0xFF15803D), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        is CivicEyeSubmitStatus.SavedOffline -> {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("civic_eye_submit_offline_banner")
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CloudQueue,
                            contentDescription = "Queued offline",
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Saved Locally (${status.reportId})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = Color(0xFF92400E)
                            )
                            Text(
                                text = status.message,
                                fontSize = 11.5.sp,
                                color = Color(0xFF92400E)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (onViewOnMap != null) {
                            TextButton(
                                onClick = { onViewOnMap(status.reportId) },
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("civic_eye_offline_banner_view_map_btn")
                            ) {
                                Text(
                                    text = stringResource(R.string.civic_osm_map_view_on_map),
                                    fontSize = 12.sp,
                                    color = Color(0xFF0284C7),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text("Dismiss", fontSize = 12.sp, color = Color(0xFF92400E))
                        }
                    }
                }
            }
        }
        is CivicEyeSubmitStatus.Error -> {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("civic_eye_submit_error_banner")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.ErrorOutline,
                        contentDescription = "Submission error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Could Not Save to Firestore",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = status.message,
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    TextButton(onClick = onDismiss) {
                        Text("Dismiss", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
        else -> Unit
    }
}

@Composable
fun CivicEyeAuthGateCard(
    firebaseUser: FirebaseUser?,
    isSigningIn: Boolean,
    onSignInClick: () -> Unit
) {
    if (firebaseUser == null) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("civic_eye_auth_gate_card")
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = "Google Sign-In required",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.civic_eye_sign_in_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = stringResource(R.string.civic_eye_sign_in_desc),
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = onSignInClick,
                    enabled = !isSigningIn,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("civic_eye_google_sign_in_btn")
                ) {
                    if (isSigningIn) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Signing in…", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.civic_eye_sign_in_cta), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    } else {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF0FDF4),
            border = BorderStroke(1.dp, Color(0xFF86EFAC)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("civic_eye_signed_in_banner")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = "Authenticated Citizen",
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Signed in as ${firebaseUser.displayName ?: firebaseUser.email ?: "Verified Citizen"}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF166534)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFDCFCE7)
                ) {
                    Text(
                        text = stringResource(R.string.civic_eye_signed_in_badge),
                        color = Color(0xFF15803D),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CivicEyeLiveGpsCard(
    gpsFix: CivicGpsFix?,
    isAcquiringGps: Boolean,
    locationPermissionDenied: Boolean,
    onCaptureGps: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            if (gpsFix != null) Color(0xFF16A34A).copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("civic_eye_gps_card")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0284C7).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isAcquiringGps) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF0284C7)
                        )
                    } else {
                        Icon(
                            Icons.Default.MyLocation,
                            contentDescription = "Device GPS Coordinates",
                            tint = if (gpsFix != null) Color(0xFF16A34A) else Color(0xFF0284C7),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.civic_eye_gps_title),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    when {
                        isAcquiringGps -> {
                            Text(
                                text = stringResource(R.string.civic_eye_gps_locating),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        gpsFix != null -> {
                            val accText = gpsFix.accuracyMeters?.let { " • ±${it.toInt()}m" } ?: ""
                            Text(
                                text = String.format(
                                    Locale.US,
                                    "Lat: %.5f° • Lng: %.5f°%s",
                                    gpsFix.latitude,
                                    gpsFix.longitude,
                                    accText
                                ),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (!gpsFix.geoAddress.isNullOrBlank()) {
                                Text(
                                    text = gpsFix.geoAddress,
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        locationPermissionDenied -> {
                            Text(
                                text = stringResource(R.string.civic_eye_gps_permission_denied),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        else -> {
                            Text(
                                text = stringResource(R.string.civic_eye_gps_unavailable),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when {
                        gpsFix != null -> Color(0xFFDCFCE7)
                        isAcquiringGps -> Color(0xFFE0F2FE)
                        else -> Color(0xFFFEF3C7)
                    }
                ) {
                    Text(
                        text = when {
                            gpsFix != null -> "LIVE GPS"
                            isAcquiringGps -> "LOCATING"
                            else -> "TAP GPS"
                        },
                        color = when {
                            gpsFix != null -> Color(0xFF15803D)
                            isAcquiringGps -> Color(0xFF0369A1)
                            else -> Color(0xFFB45309)
                        },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            OutlinedButton(
                onClick = onCaptureGps,
                enabled = !isAcquiringGps,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("civic_eye_refresh_gps_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.GpsFixed,
                    contentDescription = "Capture Live GPS",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (gpsFix != null) {
                        stringResource(R.string.civic_eye_gps_refresh_btn)
                    } else {
                        stringResource(R.string.civic_eye_gps_capture_btn)
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
