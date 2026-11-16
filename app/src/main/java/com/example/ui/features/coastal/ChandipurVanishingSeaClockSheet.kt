package com.example.ui.features.coastal

import android.media.AudioManager
import android.media.ToneGenerator
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import android.content.Intent
import android.speech.tts.TextToSpeech
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Waves
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableIntStateOf
import com.example.util.ShareHelper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Chandipur Live Tide & "Vanishing Sea" Visual Clock Sheet.
 * Features an interactive visual tidal radial dial, safe sea-walk corridor calculator,
 * returning flood tide warning siren simulation, and 24-hour ebb/flood time-travel slider.
 */
@Composable
fun ChandipurVanishingSeaClockSheet(
    language: AppLanguage = AppLanguage.ENGLISH,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Real-time calendar reference
    val currentCalendar = remember { Calendar.getInstance() }
    val realCurrentHourFraction = remember {
        currentCalendar.get(Calendar.HOUR_OF_DAY) + currentCalendar.get(Calendar.MINUTE) / 60f
    }

    // Time slider (0 to 24 hours). Default to real-world current hour
    var simulatedHour by remember { mutableFloatStateOf(realCurrentHourFraction) }
    var isLiveClockMode by remember { mutableStateOf(true) }

    // Sound alert toggle
    var isSoundChimeActive by remember { mutableStateOf(false) }

    // Live Seabed Walk Distance Tracker (Meters walked out onto the dry seabed from embankment)
    var walkDistanceMeters by remember { mutableIntStateOf(750) }

    // TextToSpeech Audio Beach Guide state
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsPlaying by remember { mutableStateOf(false) }
    var activeAudioTrack by remember { mutableIntStateOf(0) }

    DisposableEffect(context) {
        val tts = TextToSpeech(context) { status ->
            // Initialized safely
        }
        ttsEngine = tts
        onDispose {
            try {
                tts.stop()
                tts.shutdown()
            } catch (_: Exception) {}
        }
    }

    // Compute tidal metrics dynamically for the selected hour
    // Semi-diurnal tide formula approximation for Chandipur Bay of Bengal:
    // Low tide around 05:30 AM & 05:45 PM; High tide around 11:45 AM & 11:30 PM
    val hourAngle = ((simulatedHour - 5.5f) / 12.42f) * 2f * PI.toFloat()
    // normalized height: 0.0 (extreme low tide, receded 5km) to 1.0 (extreme high tide, 0km)
    val tideHeightNorm = ((1f - cos(hourAngle)) / 2f).coerceIn(0f, 1f)
    val seaRecededKm = (4.8f * (1f - tideHeightNorm)).coerceIn(0f, 4.8f)
    val waterLevelMeters = (0.3f + 3.1f * tideHeightNorm)

    // Rate of change (derivative) to determine whether tide is Ebb (receding) or Flood (returning)
    val rateOfChange = sin(hourAngle)
    val isEbbTide = rateOfChange < 0f // Water receding away from beach
    val isLowTidePeak = tideHeightNorm < 0.20f
    val isHighTidePeak = tideHeightNorm > 0.85f

    // Safe walk window check
    // Safe when sea has receded at least 2.0 km and tide is NOT rushing in violently (heightNorm < 0.45)
    val isSafeToWalk = tideHeightNorm < 0.45f
    val isUrgentReturnWarning = !isSafeToWalk && tideHeightNorm in 0.45f..0.75f && !isEbbTide

    // Pulsing animation for safety warning
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val warningAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "warningAlpha"
    )

    // Play audible Morse/tone chime if user requests alarm simulation
    LaunchedEffect(isSoundChimeActive) {
        if (isSoundChimeActive) {
            try {
                val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 400)
            } catch (_: Exception) {}
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("chandipur_vanishing_sea_clock_sheet")
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0284C7)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🌊", fontSize = 22.sp)
                }
                Column {
                    Text(
                        text = if (language == AppLanguage.ODIA) "ଚାନ୍ଦିପୁର ଲାଇଭ୍ ଜୁଆର-ଭଟ୍ଟା ଓ ସମୁଦ୍ର ଘଣ୍ଟା" else "Chandipur Live Tide & Vanishing Sea",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (language == AppLanguage.ODIA) "ଦିନକୁ ୨ ଥର ୫ କିମି ପଛକୁ ଯାଏ ସମୁଦ୍ର" else "Recedes up to 5 km twice daily into the Bay",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(
                onClick = onClose,
                modifier = Modifier.testTag("close_vanishing_sea_clock_btn")
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hero Tidal Status Card with Live Radial Dial
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isUrgentReturnWarning) Color(0xFFFEF2F2) else Color(0xFFF0F9FF)
            ),
            border = BorderStroke(
                1.5.dp,
                if (isUrgentReturnWarning) Color(0xFFEF4444) else Color(0xFF38BDF8)
            ),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Status Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = when {
                        isUrgentReturnWarning -> Color(0xFFDC2626)
                        isLowTidePeak -> Color(0xFF0284C7)
                        isHighTidePeak -> Color(0xFF1E3A8A)
                        isEbbTide -> Color(0xFF0D9488)
                        else -> Color(0xFFEA580C)
                    },
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = when {
                                isUrgentReturnWarning -> "⚠️ DANGER: HIGH FLOOD TIDE RUSHING IN"
                                isLowTidePeak -> "🏖️ PEAK LOW TIDE: SEABED FULLY EXPOSED"
                                isHighTidePeak -> "🌊 PEAK HIGH TIDE: FULL SHORELINE"
                                isEbbTide -> "⬇️ EBB TIDE: SEA RECEDING FAST"
                                else -> "⬆️ FLOOD TIDE: SEA RETURNING TO SHORE"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        )
                    }
                }

                // Interactive Radial Canvas Visual Clock
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val radius = size.width / 2f - 16.dp.toPx()

                        // Outer track
                        drawCircle(
                            color = Color(0xFFE2E8F0),
                            radius = radius,
                            center = center,
                            style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Tidal sweep arc
                        val sweepAngle = (simulatedHour / 24f) * 360f
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(
                                    Color(0xFF0284C7),
                                    Color(0xFF0D9488),
                                    Color(0xFF38BDF8),
                                    Color(0xFF0284C7)
                                )
                            ),
                            startAngle = -90f,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round),
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2f, radius * 2f)
                        )

                        // 12 hour ticks
                        for (i in 0 until 12) {
                            val angleRad = (i * 30f - 90f) * (PI.toFloat() / 180f)
                            val startX = center.x + (radius - 16.dp.toPx()) * cos(angleRad)
                            val startY = center.y + (radius - 16.dp.toPx()) * sin(angleRad)
                            val endX = center.x + (radius - 8.dp.toPx()) * cos(angleRad)
                            val endY = center.y + (radius - 8.dp.toPx()) * sin(angleRad)
                            drawLine(
                                color = Color(0xFF94A3B8),
                                start = Offset(startX, startY),
                                end = Offset(endX, endY),
                                strokeWidth = 2.dp.toPx()
                            )
                        }

                        // Center wave animation
                        val wavePath = Path()
                        val waveWidth = radius * 1.3f
                        val waveY = center.y + (tideHeightNorm - 0.5f) * radius * 0.8f
                        wavePath.moveTo(center.x - waveWidth / 2f, waveY)
                        var x = center.x - waveWidth / 2f
                        while (x <= center.x + waveWidth / 2f) {
                            val relX = (x - (center.x - waveWidth / 2f)) / waveWidth
                            val y = waveY + sin(relX * 4f * PI.toFloat()) * 8.dp.toPx()
                            wavePath.lineTo(x, y)
                            x += 4f
                        }
                        drawPath(
                            path = wavePath,
                            color = if (isUrgentReturnWarning) Color(0xFFDC2626) else Color(0xFF0284C7),
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    // Inside Dial Metrics
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.1f km", seaRecededKm),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = if (isUrgentReturnWarning) Color(0xFFDC2626) else Color(0xFF0369A1)
                            )
                        )
                        Text(
                            text = if (language == AppLanguage.ODIA) "ସମୁଦ୍ର ପଛକୁ ହଟିଛି" else "Receded from Shore",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format(Locale.US, "Depth: %.2f m", waterLevelMeters),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // Time Display
                val hourInt = simulatedHour.toInt()
                val minuteInt = ((simulatedHour - hourInt) * 60).toInt()
                val amPm = if (hourInt >= 12) "PM" else "AM"
                val displayHour = if (hourInt % 12 == 0) 12 else hourInt % 12
                val timeString = String.format(Locale.US, "%02d:%02d %s", displayHour, minuteInt, amPm)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dial Time: $timeString",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OutlinedButton(
                        onClick = {
                            simulatedHour = realCurrentHourFraction
                            isLiveClockMode = true
                            Toast.makeText(context, "Reset to live clock", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Reset to Live", fontSize = 11.sp)
                    }
                }

                // Time Slider to scrub the day
                Slider(
                    value = simulatedHour,
                    onValueChange = {
                        simulatedHour = it
                        isLiveClockMode = false
                    },
                    valueRange = 0f..24f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF0284C7),
                        activeTrackColor = Color(0xFF38BDF8)
                    ),
                    modifier = Modifier.testTag("vanishing_sea_slider")
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("12 AM (Midnight)", fontSize = 10.sp, color = Color.Gray)
                    Text("12 PM (Noon)", fontSize = 10.sp, color = Color.Gray)
                    Text("11:59 PM", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Safe Sea-Walk Corridor Advisory
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSafeToWalk) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
            ),
            border = BorderStroke(
                1.dp,
                if (isSafeToWalk) Color(0xFF86EFAC) else Color(0xFFFCA5A5)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isSafeToWalk) Icons.Default.DirectionsWalk else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isSafeToWalk) Color(0xFF16A34A) else Color(0xFFDC2626),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = if (isSafeToWalk) {
                            if (language == AppLanguage.ODIA) "ସମୁଦ୍ର ଶଯ୍ୟାରେ ଚାଲିବା ନିରାପଦ (Safe Walk Active)" else "Safe Sea-Walk Corridor OPEN"
                        } else {
                            if (language == AppLanguage.ODIA) "ସତର୍କତା: ଜୁଆର ପାଣି ମାଡି ଆସୁଛି (Return to Shore!)" else "DANGER: Flood Tide Returning - Return to Shore!"
                        },
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isSafeToWalk) Color(0xFF166534) else Color(0xFF991B1B)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isSafeToWalk) {
                        "Water has receded beyond 2.0 km. You can safely explore the hard sand seabed, observe red crabs & horseshoe crabs. Maintain sight of the coastline."
                    } else {
                        "Chandipur's tidal surge rushes inward at over 15-20 km/h due to exceptionally flat mudflats. Do NOT venture past the shoreline!"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Safe Hours Window: 05:00 AM - 09:30 AM & 05:00 PM - 09:00 PM",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.outline
                        )
                    )
                    IconButton(
                        onClick = {
                            isSoundChimeActive = !isSoundChimeActive
                            Toast.makeText(
                                context,
                                if (isSoundChimeActive) "Tide siren chime armed" else "Tide siren silenced",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isSoundChimeActive) Icons.Default.NotificationsActive else Icons.Default.VolumeUp,
                            contentDescription = "Test Chime",
                            tint = if (isSoundChimeActive) Color(0xFFDC2626) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Daily 4-Phase Tide Schedule Table
        Text(
            text = if (language == AppLanguage.ODIA) "ଆଜିର ୪ଟି ଜୁଆର-ଭଟ୍ଟା ସମୟ ସାରଣୀ" else "Today's 4-Phase Tidal Forecast",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        val tidePhases = listOf(
            Triple("05:32 AM", "Low Tide 1 (ଭଟ୍ଟା - Peak Sea Recession)", "4.8 km Out • 0.35m Depth"),
            Triple("11:48 AM", "High Tide 1 (ଜୁଆର - Full Water Line)", "0.0 km • 3.25m Depth"),
            Triple("05:54 PM", "Low Tide 2 (ଭଟ୍ଟା - Evening Walk Zone)", "4.6 km Out • 0.40m Depth"),
            Triple("11:58 PM", "High Tide 2 (ଜୁଆର - Midnight Surge)", "0.0 km • 3.38m Depth")
        )

        tidePhases.forEach { (time, name, desc) ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = time,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (name.contains("Low")) Color(0xFFDCFCE7) else Color(0xFFDBEAFE)
                    ) {
                        Text(
                            text = if (name.contains("Low")) "SAFE WALK" else "NO SWIMMING",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (name.contains("Low")) Color(0xFF166534) else Color(0xFF1E40AF)
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // 1. Interactive Seabed Walk Distance Radar
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    walkDistanceMeters > 3500 || !isSafeToWalk -> Color(0xFFFEF2F2)
                    walkDistanceMeters > 2000 -> Color(0xFFFFFBEB)
                    else -> Color(0xFFF0FDF4)
                }
            ),
            border = BorderStroke(
                1.dp,
                when {
                    walkDistanceMeters > 3500 || !isSafeToWalk -> Color(0xFFFCA5A5)
                    walkDistanceMeters > 2000 -> Color(0xFFFDE68A)
                    else -> Color(0xFFBBF7D0)
                }
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DirectionsWalk,
                            contentDescription = "Walk Meter",
                            tint = when {
                                walkDistanceMeters > 3500 || !isSafeToWalk -> Color(0xFFDC2626)
                                walkDistanceMeters > 2000 -> Color(0xFFD97706)
                                else -> Color(0xFF16A34A)
                            },
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == AppLanguage.ODIA) "ସମୁଦ୍ର ଶଯ୍ୟାରେ ଚାଲିବା ଦୂରତା ମିଟର" else "Seabed Walk Distance Meter",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = Color(0xFF0F172A)
                        )
                    }

                    Text(
                        text = "${String.format(Locale.US, "%.2f", walkDistanceMeters / 1000f)} km",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = when {
                            walkDistanceMeters > 3500 || !isSafeToWalk -> Color(0xFFDC2626)
                            walkDistanceMeters > 2000 -> Color(0xFFD97706)
                            else -> Color(0xFF16A34A)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { (walkDistanceMeters / 5000f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = when {
                        walkDistanceMeters > 3500 || !isSafeToWalk -> Color(0xFFEF4444)
                        walkDistanceMeters > 2000 -> Color(0xFFF59E0B)
                        else -> Color(0xFF10B981)
                    },
                    trackColor = Color(0xFFE2E8F0)
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = when {
                        !isSafeToWalk -> if (language == AppLanguage.ODIA) "⚠️ ସତର୍କତା: ଜୁଆର ପାଣି ଫେରୁଛି! ତୁରନ୍ତ ସୁରକ୍ଷିତ ବନ୍ଧ ଆଡ଼କୁ ଫେରନ୍ତୁ।" else "⚠️ CAUTION: High tide water is returning! Return to the beach embankment immediately."
                        walkDistanceMeters > 3500 -> if (language == AppLanguage.ODIA) "🚨 ଅତିରିକ୍ତ ଦୂରତା: ଆପଣ ୩.୫ କିମିରୁ ଅଧିକ ଆଗକୁ ଆସିଛନ୍ତି! ଆଉ ଆଗକୁ ଯାଆନ୍ତୁ ନାହିଁ।" else "🚨 HIGH-RISK PERIMETER: You are >3.5 km out! Do not proceed further into the sea."
                        walkDistanceMeters > 2000 -> if (language == AppLanguage.ODIA) "🟡 ମଧ୍ୟମ ସୀମା: ୨ କିମିରୁ ଅଧିକ ଆଗକୁ ଯାଉଛନ୍ତି, ସମୟ ଉପରେ ନଜର ରଖନ୍ତୁ।" else "🟡 CAUTION ZONE: Walking >2 km out. Keep track of return high-tide countdown."
                        else -> if (language == AppLanguage.ODIA) "🟢 ସୁରକ୍ଷିତ ଚାଲିବା ବଳୟ (Safe Zone): ପାଣି ସୁରକ୍ଷିତ ଭାବେ ୪.୮ କିମି ପଛକୁ ଯାଇଛି।" else "🟢 SAFE WALK ZONE: Safe firm sand. Water receded ~4.8 km."
                    },
                    fontSize = 11.5.sp,
                    color = when {
                        walkDistanceMeters > 3500 || !isSafeToWalk -> Color(0xFFB91C1C)
                        walkDistanceMeters > 2000 -> Color(0xFF92400E)
                        else -> Color(0xFF15803D)
                    },
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { walkDistanceMeters = (walkDistanceMeters + 250).coerceAtMost(5000) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("+250m", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = { walkDistanceMeters = (walkDistanceMeters + 500).coerceAtMost(5000) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("+500m", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = { walkDistanceMeters = 0 },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Reset", fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Bilingual Audio Beach Guide Companion (TextToSpeech)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
            border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Headphones,
                            contentDescription = null,
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (language == AppLanguage.ODIA) "ଚାନ୍ଦିପୁର ଅଡିଓ ଗାଇଡ୍ ଓ ଇକୋଲୋଜି ଟ୍ରାକ୍" else "Chandipur Audio Beach Guide & Ecology",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = Color(0xFF0369A1)
                        )
                    }

                    IconButton(
                        onClick = {
                            if (isTtsPlaying) {
                                ttsEngine?.stop()
                                isTtsPlaying = false
                            } else {
                                val textToRead = when (activeAudioTrack) {
                                    0 -> if (language == AppLanguage.ODIA)
                                        "ଚାନ୍ଦିପୁର ବେଳାଭୂମିର ସବୁଠାରୁ ଆଶ୍ଚର୍ଯ୍ୟଜନକ ବିଶେଷତ୍ୱ ହେଉଛି ଏହାର ଭଟ୍ଟା ସମୟରେ ସମୁଦ୍ର ଜଳ ପାଖାପାଖି ୫ କିଲୋମିଟର ପର୍ଯ୍ୟନ୍ତ ପଛକୁ ଚାଲିଯାଏ। ଏହି ସମୟରେ ପର୍ଯ୍ୟଟକମାନେ ନିରାପଦରେ ସମୁଦ୍ର ଶଯ୍ୟାରେ ଚାଲି ଚାଲି ଯାଇପାରନ୍ତି।"
                                    else
                                        "Welcome to Chandipur Beach. Chandipur is world-famous for its vanishing sea phenomenon, where the water recedes up to 5 kilometers during low tide twice every day due to its exceptionally gentle underwater gradient."
                                    1 -> if (language == AppLanguage.ODIA)
                                        "ଚାନ୍ଦିପୁର ସମୁଦ୍ର ଶଯ୍ୟାରେ ଲିଭିଙ୍ଗ ଫସିଲ୍ ବା ଜୀବନ୍ତ ଜୀବାଶ୍ମ କୁହାଯାଉଥିବା ଅଶ୍ୱକ୍ଷୁର କଙ୍କଡ଼ା ଏବଂ ଲାଲ୍ କଙ୍କଡ଼ା ବହୁ ସଂଖ୍ୟାରେ ଦେଖାଯାନ୍ତି। ଏମାନଙ୍କୁ ଆଘାତ ଦିଅନ୍ତୁ ନାହିଁ।"
                                    else
                                        "The intertidal zone of Chandipur is home to prehistoric horseshoe crabs, known as living fossils, and millions of tiny red ghost crabs that carpet the exposed sand dunes."
                                    else -> if (language == AppLanguage.ODIA)
                                        "ସାବଧାନ! ଜୁଆର ଆରମ୍ଭ ହେଲେ ସମୁଦ୍ର ପାଣି ବହୁତ ଦ୍ରୁତ ଗତିରେ ଫେରିଆସେ। ପାଣି ଆଣ୍ଠୁ ପର୍ଯ୍ୟନ୍ତ ଆସିବା ପୂର୍ବରୁ ବେଳାଭୂମି ବନ୍ଧ ଆଡ଼କୁ ଫେରିଆସନ୍ତୁ।"
                                    else
                                        "High tide safety alert: When the tide reverses, water rushes back rapidly across the flat seabed. Return to the beach embankment before the safe walk window ends."
                                }
                                val locale = if (language == AppLanguage.ODIA) Locale("or", "IN") else Locale.ENGLISH
                                try {
                                    ttsEngine?.language = locale
                                } catch (_: Exception) {}
                                ttsEngine?.speak(textToRead, TextToSpeech.QUEUE_FLUSH, null, "track_${activeAudioTrack}")
                                isTtsPlaying = true
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isTtsPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = if (isTtsPlaying) "Stop Narration" else "Play Narration",
                            tint = Color(0xFF0284C7)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val audioTracks = listOf(
                    if (language == AppLanguage.ODIA) "୧. ସମୁଦ୍ର ଅପସାରଣ" else "1. Vanishing Sea",
                    if (language == AppLanguage.ODIA) "୨. ଅଶ୍ୱକ୍ଷୁର କଙ୍କଡ଼ା" else "2. Horseshoe Crab",
                    if (language == AppLanguage.ODIA) "୩. ଜୁଆର ସୁରକ୍ଷା" else "3. Return Tide"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    audioTracks.forEachIndexed { index, trackTitle ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (activeAudioTrack == index) Color(0xFF0284C7) else Color(0xFFDBEAFE),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    activeAudioTrack = index
                                    if (isTtsPlaying) {
                                        ttsEngine?.stop()
                                        isTtsPlaying = false
                                    }
                                }
                        ) {
                            Text(
                                text = trackTitle,
                                fontSize = 10.sp,
                                fontWeight = if (activeAudioTrack == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (activeAudioTrack == index) Color.White else Color(0xFF1E40AF),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Share Walk Window & Tidal Alert Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    ShareHelper.shareTideAlert(
                        context = context,
                        status = if (isSafeToWalk) "SAFE LOW-TIDE WALK WINDOW OPEN" else "CAUTION - HIGH TIDE SURGE",
                        walkWindow = "Next 3.5 Hours (Sea receded ~${String.format(Locale.US, "%.1f", seaRecededKm)} km)",
                        highTideTime = "11:45 AM / 11:30 PM",
                        isOdia = language == AppLanguage.ODIA
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (language == AppLanguage.ODIA) "ଜୁଆର ଓ ସୁରକ୍ଷିତ ଚାଲିବା ସୂଚନା ସେୟାର୍ କରନ୍ତୁ" else "Share Walk Window & Tidal Alert",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Naturalist Facts & Survival Protocol
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "💡 Local Baleswar Fishermen's Wisdom",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• The sea recession is greatest during Amavasya (New Moon) and Purnima (Full Moon) spring tides.\n" +
                            "• If you notice small water trickles forming behind your back into shallow channels, IMMEDIATELY head back towards the casuarina trees.\n" +
                            "• Horseshoe crabs (Carcinoscorpius rotundicauda) are living fossils found on this seabed; do not overturn or step on them.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onClose,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("dismiss_vanishing_sea_clock_btn")
        ) {
            Text("Done / Close Radar", fontWeight = FontWeight.Bold)
        }
    }
}
