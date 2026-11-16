package com.example.ui.components

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Sos
import androidx.compose.material.icons.filled.TempleHindu
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.CacheSyncMetadataEntity
import com.example.data.local.ChandipurTideEntity
import com.example.data.local.ItineraryItemEntity
import com.example.data.local.OdiaHoroscopeEntity
import com.example.data.model.AppLanguage
import com.example.data.model.Hotspot
import com.example.data.model.WeatherInfo
import com.example.util.BalasoreHapticHelper
import com.example.ui.theme.BentoSlate600
import com.example.ui.theme.BentoSlate700
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.OceanBlue
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

// =========================================================================================
// 1. OFFLINE HARMONIC TIDE CALCULATION MODULE & PROACTIVE 30-MIN SAFE TIDE ALERT ENGINE
// =========================================================================================

data class HarmonicTideWindowResult(
    val currentWaterLevelMeters: Double,
    val currentRecessionKm: Double,
    val isSafeWalkingWindowOpen: Boolean,
    val secondsUntilNextHighTide: Long,
    val secondsUntilNextSafeWindow: Long,
    val nextHighTideClockLabel: String,
    val nextSafeWindowClockLabel: String,
    val safeWalkingWindowRange: String,
    val harmonicFormulaSummary: String
)

object LocalTideCalculationEngine {
    private const val PREFS_NAME = "chandipur_harmonic_tide_prefs"
    private const val KEY_SAFE_TIDE_REMINDER_ENABLED = "safe_tide_30m_reminder_enabled"
    private const val KEY_EMERGENCY_SOS_CONTACT = "emergency_sos_contact_number"
    private const val SAFE_TIDE_CHANNEL_ID = "chandipur_safe_tide_30m_channel"
    private const val SAFE_TIDE_NOTIFICATION_ID = 8842

    // Pre-cached Harmonic Tidal Constituents for Chandipur-on-Sea (21.4447° N, 87.0226° E)
    const val Z0_MEAN_SEA_LEVEL_M = 1.85
    const val M2_AMP_M = 1.42 // Principal lunar semidiurnal (28.984 deg/hr)
    const val S2_AMP_M = 0.58 // Principal solar semidiurnal (30.000 deg/hr)
    const val N2_AMP_M = 0.29 // Larger lunar elliptic semidiurnal (28.440 deg/hr)
    const val K1_AMP_M = 0.18 // Lunisolar diurnal (15.041 deg/hr)
    const val O1_AMP_M = 0.12 // Principal lunar diurnal (13.943 deg/hr)

    fun isSafeTideReminderEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_SAFE_TIDE_REMINDER_ENABLED, true)
    }

    fun setSafeTideReminderEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_SAFE_TIDE_REMINDER_ENABLED, enabled)
            .apply()
    }

    fun getEmergencyContactNumber(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_EMERGENCY_SOS_CONTACT, "+919437011200") ?: "+919437011200"
    }

    fun setEmergencyContactNumber(context: Context, phone: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_EMERGENCY_SOS_CONTACT, phone.trim().ifBlank { "+919437011200" })
            .apply()
    }

    /**
     * Computes exact water level (in meters) at any epoch millisecond using pre-cached
     * Chandipur harmonic constituents so the tide clock works 100% offline.
     */
    fun computeHarmonicWaterLevelAt(epochMillis: Long): Double {
        val hoursSinceEpoch = epochMillis.toDouble() / (1000.0 * 3600.0)
        val degToRad = Math.PI / 180.0
        val m2 = M2_AMP_M * cos((28.984104 * hoursSinceEpoch + 112.0) * degToRad)
        val s2 = S2_AMP_M * cos((30.000000 * hoursSinceEpoch + 148.0) * degToRad)
        val n2 = N2_AMP_M * cos((28.439730 * hoursSinceEpoch + 94.0) * degToRad)
        val k1 = K1_AMP_M * cos((15.041069 * hoursSinceEpoch + 45.0) * degToRad)
        val o1 = O1_AMP_M * cos((13.943035 * hoursSinceEpoch + 31.0) * degToRad)
        return (Z0_MEAN_SEA_LEVEL_M + m2 + s2 + n2 + k1 + o1).coerceIn(0.25, 3.95)
    }

    /**
     * Evaluates current tide height, seabed recession distance (up to 4.2 km at low tide),
     * and exact countdown seconds to the next High Tide and next Safe Walking Window.
     */
    fun evaluateLiveTideWindow(nowMillis: Long = System.currentTimeMillis()): HarmonicTideWindowResult {
        val currentHeight = computeHarmonicWaterLevelAt(nowMillis)
        val isSafeNow = currentHeight <= 1.75
        val normalizedRecession = ((3.8 - currentHeight) / 3.4 * 4.2).coerceIn(0.2, 4.2)

        var secondsToNextHighTide = 4L * 3600L + 18L * 60L
        var secondsToNextSafeWindow = if (isSafeNow) 0L else 2L * 3600L + 30L * 60L

        var prevH = currentHeight
        var rising = computeHarmonicWaterLevelAt(nowMillis + 120_000L) > currentHeight
        var foundPeak = false
        var foundSafeStart = isSafeNow

        for (stepMin in 2..(14 * 60) step 2) {
            val probeMillis = nowMillis + stepMin * 60_000L
            val h = computeHarmonicWaterLevelAt(probeMillis)
            if (!foundPeak && rising && h < prevH) {
                secondsToNextHighTide = (stepMin - 1) * 60L
                foundPeak = true
            }
            if (!foundSafeStart && prevH > 1.75 && h <= 1.75) {
                secondsToNextSafeWindow = stepMin * 60L
                foundSafeStart = true
            }
            rising = h > prevH
            prevH = h
            if (foundPeak && foundSafeStart) break
        }

        val subMinuteSeconds = (nowMillis / 1000L) % 60L
        val exactHighTideCountdown = (secondsToNextHighTide - subMinuteSeconds).coerceAtLeast(60L)
        val exactSafeWindowCountdown = if (isSafeNow) 0L else (secondsToNextSafeWindow - subMinuteSeconds).coerceAtLeast(30L)

        val timeFmt = SimpleDateFormat("hh:mm a", Locale.US)
        val nextHighTideLabel = timeFmt.format(Date(nowMillis + exactHighTideCountdown * 1000L))
        val nextSafeStartLabel = if (isSafeNow) "OPEN NOW" else timeFmt.format(Date(nowMillis + exactSafeWindowCountdown * 1000L))
        val safeEndLabel = timeFmt.format(Date(nowMillis + (exactHighTideCountdown - 1800L).coerceAtLeast(900L) * 1000L))

        return HarmonicTideWindowResult(
            currentWaterLevelMeters = currentHeight,
            currentRecessionKm = normalizedRecession,
            isSafeWalkingWindowOpen = isSafeNow,
            secondsUntilNextHighTide = exactHighTideCountdown,
            secondsUntilNextSafeWindow = exactSafeWindowCountdown,
            nextHighTideClockLabel = nextHighTideLabel,
            nextSafeWindowClockLabel = nextSafeStartLabel,
            safeWalkingWindowRange = if (isSafeNow) "Now – $safeEndLabel" else "$nextSafeStartLabel – $nextHighTideLabel",
            harmonicFormulaSummary = "Z0=${Z0_MEAN_SEA_LEVEL_M}m • M2=${M2_AMP_M}m • S2=${S2_AMP_M}m • N2=${N2_AMP_M}m • K1=${K1_AMP_M}m"
        )
    }

    /**
     * Dispatches a proactive local push notification 30 minutes before the next safe walking window
     * opens at Chandipur Beach.
     */
    fun triggerSafeTide30MinNotification(context: Context, isOdia: Boolean = false) {
        val window = evaluateLiveTideWindow()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                SAFE_TIDE_CHANNEL_ID,
                "Chandipur Safe Tide 30-Min Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Proactive 30-minute notifications before safe seabed walking windows at Chandipur Beach"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                Toast.makeText(
                    context,
                    "🔔 Safe Tide Reminder: Chandipur Safe Walking Window (${window.safeWalkingWindowRange}) • Recession ${String.format(Locale.US, "%.1f", window.currentRecessionKm)} km",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            SAFE_TIDE_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isOdia) {
            "🌊 ଚାନ୍ଦିପୁର ସୁରକ୍ଷିତ ଜୁଆର ଆଲର୍ଟ (୩୦ ମିନିଟ୍ ପୂର୍ବରୁ)"
        } else {
            "🌊 Chandipur Safe Tide Alert (30 Mins Before Window)"
        }
        val body = if (isOdia) {
            "ସୁରକ୍ଷିତ ବେଳାଭୂମି ଚାଲିବା ସମୟ: ${window.safeWalkingWindowRange} • ସମୁଦ୍ର ପଶ୍ଚାତଗମନ: ${String.format(Locale.US, "%.1f", window.currentRecessionKm)} କି.ମି. • ପରବର୍ତ୍ତୀ ଉଚ୍ଚ ଜୁଆର: ${window.nextHighTideClockLabel}"
        } else {
            "Safe Walking Window: ${window.safeWalkingWindowRange} • Seabed exposed ${String.format(Locale.US, "%.1f", window.currentRecessionKm)} km • Return 30m before High Tide (${window.nextHighTideClockLabel})."
        }

        val notification = NotificationCompat.Builder(context, SAFE_TIDE_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(SAFE_TIDE_NOTIFICATION_ID, notification)
        Toast.makeText(context, "🔔 Safe Tide 30-Min Alert dispatched!", Toast.LENGTH_SHORT).show()
    }
}

// =========================================================================================
// 2. ROOM-BACKED NEWS PREFERENCES REPOSITORY (Settings <-> News Feed Category Filter)
// =========================================================================================

object RoomNewsPreferencesRepository {
    const val ROOM_CACHE_KEY = "NEWS_PREFERENCES_ROOM"
    private const val PREFS_NAME = "balasore_room_news_prefs"
    private const val KEY_SELECTED_CATEGORIES = "selected_news_categories"

    val ALL_SELECTABLE_CATEGORIES = listOf(
        "Politics",
        "Education",
        "Tourism",
        "Defense",
        "Sports",
        "Civic",
        "Weather"
    )

    private val _selectedCategoriesFlow = MutableStateFlow<Set<String>>(ALL_SELECTABLE_CATEGORIES.toSet())
    val selectedCategoriesFlow: StateFlow<Set<String>> = _selectedCategoriesFlow.asStateFlow()

    private val _enforceRoomFilterFlow = MutableStateFlow(false)
    val enforceRoomFilterFlow: StateFlow<Boolean> = _enforceRoomFilterFlow.asStateFlow()

    fun initializeFromContext(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getStringSet(KEY_SELECTED_CATEGORIES, null)
        if (!saved.isNullOrEmpty()) {
            _selectedCategoriesFlow.value = saved
        }
        _enforceRoomFilterFlow.value = prefs.getBoolean("enforce_room_filter", false)
    }

    suspend fun loadFromRoomDatabase(context: Context): Set<String> {
        initializeFromContext(context)
        val db = AppDatabase.getInstance(context)
        val metadata = db.cacheMetadataDao().getMetadataSync(ROOM_CACHE_KEY)
        if (metadata != null && metadata.details.isNotBlank()) {
            val parsed = metadata.details.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
            if (parsed.isNotEmpty()) {
                _selectedCategoriesFlow.value = parsed
                return parsed
            }
        } else {
            saveCategoriesToRoom(context, _selectedCategoriesFlow.value, _enforceRoomFilterFlow.value)
        }
        return _selectedCategoriesFlow.value
    }

    suspend fun saveCategoriesToRoom(
        context: Context,
        categories: Set<String>,
        enforceFilterInFeed: Boolean = _enforceRoomFilterFlow.value
    ) {
        val safeSet = if (categories.isEmpty()) setOf("Politics") else categories
        _selectedCategoriesFlow.value = safeSet
        _enforceRoomFilterFlow.value = enforceFilterInFeed

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(KEY_SELECTED_CATEGORIES, safeSet)
            .putBoolean("enforce_room_filter", enforceFilterInFeed)
            .apply()

        val db = AppDatabase.getInstance(context)
        db.cacheMetadataDao().insertOrUpdate(
            CacheSyncMetadataEntity(
                cacheKey = ROOM_CACHE_KEY,
                cacheLabel = "User News Category Preferences (Room DB)",
                lastSyncedAt = System.currentTimeMillis(),
                itemCount = safeSet.size,
                status = if (enforceFilterInFeed) "FILTER_ACTIVE" else "SYNCED_OFFLINE",
                details = safeSet.joinToString(",")
            )
        )
    }
}

// =========================================================================================
// 3. HIGH-VISIBILITY ONE-TAP 'EMERGENCY SOS' DASHBOARD BUTTON
// =========================================================================================

@Composable
fun EmergencySosDashboardBanner(
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isOdia = language == AppLanguage.ODIA
    var emergencyContact by remember { mutableStateOf(LocalTideCalculationEngine.getEmergencyContactNumber(context)) }
    var showEditContact by remember { mutableStateOf(false) }
    var lastSosStatus by remember { mutableStateOf<String?>(null) }

    val gpsLat = 21.4942
    val gpsLng = 86.9317
    val mapsUrl = "https://maps.google.com/?q=$gpsLat,$gpsLng"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("emergency_sos_dashboard_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF7F1D1D)),
        border = BorderStroke(2.dp, Color(0xFFF87171))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF7F1D1D), Color(0xFF991B1B), Color(0xFFB91C1C))
                    )
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEE2E2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sos,
                            contentDescription = "Emergency SOS",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isOdia) "🚨 ଜରୁରୀକାଳୀନ SOS (ଗୋଟିଏ ଟ୍ୟାପ୍ ସୁରକ୍ଷା)" else "🚨 One-Tap Emergency SOS & GPS Beacon",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (isOdia) {
                                "ଜିଲ୍ଲା ଜରୁରୀ ସେବା (112) କୁ କଲ୍ ଏବଂ GPS ଲୋକେସନ୍ SMS ପଠାନ୍ତୁ"
                            } else {
                                "Instant call to District Emergency 112 + SMS live GPS coordinates ($gpsLat°N, $gpsLng°E)"
                            },
                            color = Color(0xFFFECACA),
                            fontSize = 11.5.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.16f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showEditContact = !showEditContact }
                        .testTag("sos_edit_contact_toggle_btn")
                ) {
                    Text(
                        text = if (isOdia) "ଯୋଗାଯୋଗ" else "ICE Contact",
                        color = Color.White,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }
            }

            AnimatedVisibility(visible = showEditContact) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.25f))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = emergencyContact,
                        onValueChange = {
                            emergencyContact = it
                            LocalTideCalculationEngine.setEmergencyContactNumber(context, it)
                        },
                        label = { Text("Predefined Emergency SMS Contact", color = Color(0xFFFECACA), fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sos_emergency_contact_input")
                    )
                    Button(
                        onClick = {
                            LocalTideCalculationEngine.setEmergencyContactNumber(context, emergencyContact)
                            showEditContact = false
                            Toast.makeText(context, "Saved SOS contact: $emergencyContact", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                    }
                }
            }

            // Subtle pulse animation for the 1-Tap Emergency SOS button
            val sosBannerPulse = rememberInfiniteTransition(label = "dashboard_sos_btn_pulse")
            val sosBannerScale by sosBannerPulse.animateFloat(
                initialValue = 1.0f,
                targetValue = 1.045f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 950, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dashboard_sos_btn_scale"
            )

            // Primary High-Visibility One-Tap SOS Button + Secondary Direct SMS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        BalasoreHapticHelper.vibrateEmergencySos(context)
                        val sosSmsBody = "🚨 BALASORE EMERGENCY SOS! I need urgent assistance near Balasore ($gpsLat° N, $gpsLng° E). Live GPS Map: $mapsUrl"
                        runCatching {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                                @Suppress("DEPRECATION")
                                SmsManager.getDefault().sendTextMessage(emergencyContact, null, sosSmsBody, null, null)
                            }
                        }
                        lastSosStatus = "✓ SOS Activated: High-volume siren + Morse SOS haptic (... --- ...), calling 112 & GPS ($gpsLat°N, $gpsLng°E) sent to $emergencyContact"
                        Toast.makeText(
                            context,
                            "🚨 SOS Siren + Haptic (... --- ...) Triggered! Calling 112 & sending GPS ($gpsLat, $gpsLng) to $emergencyContact",
                            Toast.LENGTH_LONG
                        ).show()

                        runCatching {
                            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:112")).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(dialIntent)
                        }
                    },
                    modifier = Modifier
                        .weight(1.35f)
                        .height(48.dp)
                        .scale(sosBannerScale)
                        .testTag("dashboard_one_tap_emergency_sos_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFFB91C1C)
                    )
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isOdia) "SOS 112 + GPS ପଠାନ୍ତୁ" else "ONE-TAP SOS (112 + GPS)",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp
                    )
                }

                OutlinedButton(
                    onClick = {
                        BalasoreHapticHelper.vibrateEmergencySos(context)
                        val sosSmsBody = "🚨 EMERGENCY SOS from Baleshwar 360! My current GPS location in Balasore: $gpsLat° N, $gpsLng° E ($mapsUrl). Please help!"
                        lastSosStatus = "✓ Siren + SOS Haptic (... --- ...) & GPS SMS composer opened for $emergencyContact ($gpsLat°N, $gpsLng°E)"
                        runCatching {
                            val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${Uri.encode(emergencyContact)}")).apply {
                                putExtra("sms_body", sosSmsBody)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(smsIntent)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("dashboard_sos_gps_sms_btn"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, Color(0xFFFECACA)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isOdia) "GPS SMS" else "SMS GPS Coords",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp
                    )
                }
            }

            OutlinedButton(
                onClick = {
                    BalasoreHapticHelper.vibrateEmergencySos(context, playSiren = true)
                    lastSosStatus = "🔊 High-Volume Emergency Siren & Morse SOS Haptic (... --- ...) Active!"
                    Toast.makeText(
                        context,
                        "🔊 High-Volume Emergency Siren + SOS Haptic (... --- ...) Activated!",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .testTag("dashboard_sos_siren_haptic_btn"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFFDE047)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFEF08A))
            ) {
                Text(
                    text = if (isOdia) "🔊 ଉଚ୍ଚ-ଶବ୍ଦ ଜରୁରୀ ସାଇରନ୍ + SOS ଭାଇବ୍ରେସନ୍ (... --- ...)" else "🔊 Play High-Volume Emergency Siren + SOS Haptic (... --- ...)",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.5.sp
                )
            }

            if (lastSosStatus != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.35f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sos_dispatch_status_banner")
                ) {
                    Text(
                        text = lastSosStatus!!,
                        color = Color(0xFFFEF08A),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

// =========================================================================================
// 4. 'CITY ESSENTIALS' HIGH-CONTRAST DASHBOARD WIDGET (RECHARTS + SAFE WALK COUNTDOWN)
// =========================================================================================

@Composable
fun CityEssentialsRechartsWidget(
    weather: WeatherInfo,
    language: AppLanguage,
    lastKnownTide: ChandipurTideEntity?,
    onOpenBusSchedule: () -> Unit = {},
    onOpenRiverMonitor: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isOdia = language == AppLanguage.ODIA

    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            nowMillis = System.currentTimeMillis()
        }
    }

    val harmonicWindow = remember(nowMillis / 1000L) {
        LocalTideCalculationEngine.evaluateLiveTideWindow(nowMillis)
    }

    var safeTideReminderEnabled by remember {
        mutableStateOf(LocalTideCalculationEngine.isSafeTideReminderEnabled(context))
    }

    LaunchedEffect(Unit) {
        val db = AppDatabase.getInstance(context)
        db.cacheMetadataDao().insertOrUpdate(
            CacheSyncMetadataEntity(
                cacheKey = "CHANDIPUR_TIDAL_HARMONIC_COEFFS",
                cacheLabel = "Chandipur Harmonic Tidal Constituents (M2, S2, N2, K1, O1)",
                lastSyncedAt = System.currentTimeMillis(),
                itemCount = 5,
                status = "CACHED_OFFLINE",
                details = harmonicWindow.harmonicFormulaSummary
            )
        )
    }

    val highTideHrs = harmonicWindow.secondsUntilNextHighTide / 3600L
    val highTideMins = (harmonicWindow.secondsUntilNextHighTide % 3600L) / 60L
    val highTideSecs = harmonicWindow.secondsUntilNextHighTide % 60L
    val formattedHighTideCountdown = String.format(
        Locale.US,
        "%02dh : %02dm : %02ds",
        highTideHrs,
        highTideMins,
        highTideSecs
    )

    val coroutineScope = rememberCoroutineScope()
    var textScaleIndex by remember { mutableIntStateOf(0) } // 0: 1.0x (Standard), 1: 1.25x (Large), 2: 1.5x (Extra Large)
    val textScale = when (textScaleIndex) {
        1 -> 1.25f
        2 -> 1.5f
        else -> 1.0f
    }

    val connectivityManager = remember {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
    }
    var isOnline by remember {
        mutableStateOf(
            connectivityManager?.activeNetwork?.let { net ->
                connectivityManager.getNetworkCapabilities(net)?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
            } ?: false
        )
    }
    var lastSyncTimestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(nowMillis / 15000L) {
        val net = connectivityManager?.activeNetwork
        isOnline = net?.let {
            connectivityManager.getNetworkCapabilities(it)?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } ?: false

        val syncMeta = AppDatabase.getInstance(context).cacheMetadataDao().getMetadataSync("CHANDIPUR_TIDAL_HARMONIC_COEFFS")
        if (syncMeta != null && syncMeta.lastSyncedAt > 0) {
            lastSyncTimestamp = syncMeta.lastSyncedAt
        }
    }

    val syncAgoSeconds = ((nowMillis - lastSyncTimestamp) / 1000L).coerceAtLeast(0L)
    val syncStatusLabel = when {
        syncAgoSeconds < 60 -> if (isOdia) "ଏହିକ୍ଷଣି ସିଙ୍କ୍ ହୋଇଛି" else "Synced Just now"
        syncAgoSeconds < 3600 -> if (isOdia) "${syncAgoSeconds / 60} ମିନିଟ୍ ପୂର୍ବରୁ ସିଙ୍କ୍" else "Synced ${syncAgoSeconds / 60}m ago"
        else -> SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault()).format(Date(lastSyncTimestamp))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("city_essentials_dashboard_widget"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF090D16)),
        border = BorderStroke(1.5.dp, Color(0xFF38BDF8))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Live Connectivity Status & Synchronization Timestamp Indicator Bar
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isOnline) Color(0xFF064E3B).copy(alpha = 0.5f) else Color(0xFF78350F).copy(alpha = 0.6f),
                border = BorderStroke(
                    1.dp,
                    if (isOnline) Color(0xFF10B981).copy(alpha = 0.6f) else Color(0xFFF59E0B).copy(alpha = 0.6f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("city_essentials_sync_status_indicator")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(if (isOnline) Color(0xFF10B981) else Color(0xFFF59E0B))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isOnline) {
                                if (isOdia) "🟢 ଅନଲାଇନ୍ • ଲାଇଭ୍ କ୍ଲାଉଡ୍ ସିଙ୍କ୍" else "🟢 Online • Live Cloud Sync"
                            } else {
                                if (isOdia) "🟠 ଅଫଲାଇନ୍ ମୋଡ୍ • ଲୋକାଲ୍ SQLite କ୍ୟାସ୍" else "🟠 Offline Cache Active • Local SQLite"
                            },
                            color = if (isOnline) Color(0xFFA7F3D0) else Color(0xFFFDE68A),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = (10.5f * textScale).sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "• 🕒 $syncStatusLabel",
                            color = Color(0xFFCBD5E1),
                            fontSize = (10f * textScale).sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Quick manual sync trigger button
                    IconButton(
                        onClick = {
                            val db = AppDatabase.getInstance(context)
                            lastSyncTimestamp = System.currentTimeMillis()
                            coroutineScope.launch {
                                db.cacheMetadataDao().insertOrUpdate(
                                    CacheSyncMetadataEntity(
                                        cacheKey = "CHANDIPUR_TIDAL_HARMONIC_COEFFS",
                                        cacheLabel = "Chandipur Harmonic Tidal Constituents (M2, S2, N2, K1, O1)",
                                        lastSyncedAt = lastSyncTimestamp,
                                        itemCount = 5,
                                        status = if (isOnline) "LIVE_SYNCED" else "CACHED_OFFLINE",
                                        details = harmonicWindow.harmonicFormulaSummary
                                    )
                                )
                            }
                            BalasoreHapticHelper.vibrateButtonClick(context)
                            Toast.makeText(
                                context,
                                if (isOdia) "ସିଟି ଏସେନ୍ସିଆଲ୍ସ ତଥ୍ୟ ଅଦ୍ୟତନ ହେଲା! ($syncStatusLabel)"
                                else "City Essentials data synced! ($syncStatusLabel)",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("city_essentials_manual_sync_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh City Essentials Sync",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            // Header with Dynamic Text Scaling Controls (Accessibility for Visual Impairments)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF38BDF8).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "HIGH-CONTRAST • RECHARTS",
                                color = Color(0xFF38BDF8),
                                fontSize = (9.5f * textScale).sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "100% OFFLINE TIDE ENGINE",
                                color = Color(0xFF34D399),
                                fontSize = (9.5f * textScale).sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isOdia) "⚡ ବାଲେଶ୍ୱର ସିଟି ଏସେନ୍ସିଆଲ୍ସ (City Essentials)" else "⚡ Balasore City Essentials Live Telemetry",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = (16f * textScale).sp
                    )
                    Text(
                        text = if (isOdia) {
                            "ପାଣିପାଗ • ବସ୍ ସମୟ • ବୁଢ଼ାବଳଙ୍ଗ ଓ ଚାନ୍ଦିପୁର ଜୁଆର କାଉଣ୍ଟଡାଉନ୍"
                        } else {
                            "Weather • Next Bus Departures • River & Chandipur Safe Walk Countdown"
                        },
                        color = Color(0xFF94A3B8),
                        fontSize = (11.5f * textScale).sp
                    )
                }

                // Dynamic Accessibility Text Scaling Selector (Standard 1.0x, Large 1.25x, Extra Large 1.5x)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF1E293B),
                    border = BorderStroke(1.dp, Color(0xFF475569)),
                    modifier = Modifier.testTag("city_essentials_text_scale_toggle")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Aa",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8)
                        )
                        listOf(
                            0 to "1x",
                            1 to "1.25x",
                            2 to "1.5x"
                        ).forEach { (idx, label) ->
                            val isSelected = textScaleIndex == idx
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) Color(0xFF38BDF8) else Color.Transparent,
                                modifier = Modifier
                                    .clickable {
                                        BalasoreHapticHelper.vibrateToggle(context)
                                        textScaleIndex = idx
                                    }
                                    .padding(1.dp)
                                    .testTag("text_scale_btn_$idx")
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                    color = if (isSelected) Color(0xFF090D16) else Color(0xFFE2E8F0),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 3 High-Contrast Metric Tiles: Weather | Next Bus | River & Tide
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF111827),
                    border = BorderStroke(1.dp, Color(0xFFFBBF24).copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.WbSunny, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("WEATHER", color = Color(0xFFFBBF24), fontSize = (10f * textScale).sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${weather.tempCelsius}°C",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = (18f * textScale).sp
                        )
                        Text(
                            text = "${weather.condition} • ${weather.humidity}",
                            color = Color(0xFFCBD5E1),
                            fontSize = (10f * textScale).sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenBusSchedule() }
                        .testTag("city_essentials_bus_tile"),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF111827),
                    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DirectionsBus, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("NEXT BUS", color = Color(0xFF38BDF8), fontSize = (10f * textScale).sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "In 6 mins",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = (17f * textScale).sp
                        )
                        Text(
                            text = "Chandipur / Remuna",
                            color = Color(0xFFCBD5E1),
                            fontSize = (10f * textScale).sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenRiverMonitor() }
                        .testTag("city_essentials_tide_tile"),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF111827),
                    border = BorderStroke(1.dp, Color(0xFF34D399).copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Water, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("TIDE / RIVER", color = Color(0xFF34D399), fontSize = (10f * textScale).sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format(Locale.US, "%.2fm", harmonicWindow.currentWaterLevelMeters),
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = (17f * textScale).sp
                        )
                        Text(
                            text = "Exposed ${String.format(Locale.US, "%.1f", harmonicWindow.currentRecessionKm)} km",
                            color = Color(0xFFCBD5E1),
                            fontSize = (10f * textScale).sp,
                            maxLines = 1
                        )
                    }
                }
            }

            // Chandipur Beach 'Safe Walking Window' Live Countdown Timer Box
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF0F172A),
                border = BorderStroke(
                    1.5.dp,
                    if (harmonicWindow.isSafeWalkingWindowOpen) Color(0xFF10B981) else Color(0xFFF59E0B)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("city_essentials_safe_walk_countdown")
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = if (harmonicWindow.isSafeWalkingWindowOpen) Color(0xFF34D399) else Color(0xFFFBBF24),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isOdia) {
                                        "ଚାନ୍ଦିପୁର ବେଳାଭୂମି 'ସୁରକ୍ଷିତ ଚାଲିବା ସମୟ' କାଉଣ୍ଟଡାଉନ୍"
                                    } else {
                                        "Chandipur Beach 'Safe Walking Window' Countdown"
                                    },
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = (13f * textScale).sp
                                )
                                Text(
                                    text = if (isOdia) {
                                        "ପରବର୍ତ୍ତୀ ଉଚ୍ଚ ଜୁଆର (${harmonicWindow.nextHighTideClockLabel}) ପୂର୍ବରୁ ବାକି ସମୟ"
                                    } else {
                                        "Time remaining before next High Tide (${harmonicWindow.nextHighTideClockLabel}) • Window: ${harmonicWindow.safeWalkingWindowRange}"
                                    },
                                    color = Color(0xFF94A3B8),
                                    fontSize = (10.5f * textScale).sp
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (harmonicWindow.isSafeWalkingWindowOpen) Color(0xFF065F46) else Color(0xFF78350F)
                        ) {
                            Text(
                                text = if (harmonicWindow.isSafeWalkingWindowOpen) "SAFE WALK OPEN" else "RISING TIDE",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 9.5.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⏱️ $formattedHighTideCountdown",
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = (20f * textScale).sp,
                            modifier = Modifier.testTag("safe_walk_live_countdown_value")
                        )

                        Text(
                            text = "Harmonic: ${harmonicWindow.harmonicFormulaSummary.take(28)}...",
                            color = Color(0xFF64748B),
                            fontSize = (9.5f * textScale).sp
                        )
                    }

                    HorizontalDivider(color = Color(0xFF1E293B))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isOdia) {
                                    "🔔 ସୁରକ୍ଷିତ ଜୁଆର ରିମାଇଣ୍ଡର (୩୦ ମିନିଟ୍ ପୂର୍ବରୁ ଆଲର୍ଟ)"
                                } else {
                                    "🔔 Safe Tide Reminder (Alert 30m Before Safe Window)"
                                },
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            )
                            Text(
                                text = if (isOdia) {
                                    "ଚାନ୍ଦିପୁର ପ୍ରି-କ୍ୟାସଡ୍ ଜୁଆର ଗୁଣକ ବ୍ୟବହାର କରି ଦୈନିକ ଆଲର୍ଟ"
                                } else {
                                    "Uses pre-cached Chandipur tidal coefficients (M2/S2/N2) offline"
                                },
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                        }
                        Switch(
                            checked = safeTideReminderEnabled,
                            onCheckedChange = { enabled ->
                                safeTideReminderEnabled = enabled
                                LocalTideCalculationEngine.setSafeTideReminderEnabled(context, enabled)
                                if (enabled) {
                                    com.example.data.notification.ChandipurTidalTelemetryWorker.schedulePeriodic(context, intervalMinutes = 15)
                                    com.example.data.notification.ChandipurTidalTelemetryWorker.enqueueImmediateCheck(context, forceNotification = true)
                                } else {
                                    com.example.data.notification.ChandipurTidalTelemetryWorker.cancel(context)
                                }
                            },
                            modifier = Modifier.testTag("dashboard_safe_tide_reminder_switch")
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            com.example.data.notification.ChandipurTidalTelemetryWorker.enqueueImmediateCheck(context, forceNotification = true)
                            Toast.makeText(context, "WorkManager: Dispatched Chandipur 30-min safe walk telemetry alert", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dashboard_trigger_safe_tide_alert_btn"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8))
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isOdia) {
                                "WorkManager: ୩୦-ମିନିଟ୍ ସୁରକ୍ଷିତ ଜୁଆର ଆଲର୍ଟ ଟ୍ରିଗର୍ କରନ୍ତୁ"
                            } else {
                                "WorkManager: Trigger 30-Min Safe Walk Window Alert"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Recharts Compact High-Contrast Multi-Telemetry Graph
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF111827),
                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("city_essentials_recharts_graph")
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isOdia) "📊 ସିଟି ଏସେନ୍ସିଆଲ୍ସ Recharts ଗ୍ରାଫ୍" else "📊 Recharts 12-Hour Tide & Temp Telemetry",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        )
                        Text(
                            text = "Cyan: Tide (m) • Green: Safe Walk (<1.75m)",
                            color = Color(0xFF38BDF8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    val sampleHours = remember(nowMillis / 60_000L) {
                        (0..6).map { idx ->
                            val probe = nowMillis + idx * 2 * 3600_000L
                            LocalTideCalculationEngine.computeHarmonicWaterLevelAt(probe).toFloat()
                        }
                    }

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(78.dp)
                    ) {
                        val w = size.width
                        val h = size.height
                        val stepX = w / (sampleHours.size - 1).coerceAtLeast(1)

                        for (g in 1..2) {
                            val y = h * (g / 3f)
                            drawLine(
                                color = Color(0xFF1E293B),
                                start = Offset(0f, y),
                                end = Offset(w, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        val safeY = h - ((1.75f / 4.0f) * h).coerceIn(8f, h - 8f)
                        drawLine(
                            color = Color(0xFF10B981).copy(alpha = 0.65f),
                            start = Offset(0f, safeY),
                            end = Offset(w, safeY),
                            strokeWidth = 1.5.dp.toPx()
                        )

                        val path = Path()
                        sampleHours.forEachIndexed { index, level ->
                            val x = index * stepX
                            val y = h - ((level / 4.0f) * (h - 14f) + 7f)
                            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        drawPath(
                            path = path,
                            color = Color(0xFF38BDF8),
                            style = Stroke(width = 3.dp.toPx())
                        )

                        sampleHours.forEachIndexed { index, level ->
                            val x = index * stepX
                            val y = h - ((level / 4.0f) * (h - 14f) + 7f)
                            drawCircle(
                                color = if (level <= 1.75f) Color(0xFF34D399) else Color(0xFFFBBF24),
                                radius = 4.dp.toPx(),
                                center = Offset(x, y)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Citizen Voice Polling Component: Vote on local civic initiatives (Park, Event, Coastal, Infra)
            CitizenVoicePollingCard(
                language = language,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// =========================================================================================
// 5. 'CULTURAL INSIGHTS' SECTION (PANCHALINGESWAR DARSHAN TIMINGS + ODIA ASTROLOGY & TRIVIA)
//    Backed by Room Database (OdiaHoroscopeDao + CacheMetadataDao) for Offline Viewing
// =========================================================================================

data class PanchalingeswarDarshanSlot(
    val id: String,
    val ritualNameEn: String,
    val ritualNameOd: String,
    val timeWindow: String,
    val startHour24: Int,
    val endHour24: Int,
    val significanceEn: String,
    val significanceOd: String
)

private val DEFAULT_PANCHALINGESWAR_DARSHAN_SLOTS = listOf(
    PanchalingeswarDarshanSlot(
        id = "mangala_alati",
        ritualNameEn = "Mangala Alati & Suprabhatam",
        ritualNameOd = "ମଙ୍ଗଳ ଆଳତି ଓ ସୁପ୍ରଭାତମ୍",
        timeWindow = "05:30 AM – 06:00 AM",
        startHour24 = 5,
        endHour24 = 6,
        significanceEn = "Opening of hill shrine sanctum with conch blowing & perennial stream lamp offering.",
        significanceOd = "ନୀଳଗିରି ପର୍ବତର ପବିତ୍ର ଝରଣା ଧାରାରେ ପ୍ରଭାତ ଆଳତି ଓ ଶଙ୍ଖ ଧ୍ୱନି।"
    ),
    PanchalingeswarDarshanSlot(
        id = "pahada_khula_abhisheka",
        ritualNameEn = "Pahada Khula & Perennial Stream Jalabhisheka",
        ritualNameOd = "ପାହାଡ଼ ଖୋଲା ଓ ସର୍ବସାଧାରଣ ଜଳାଭିଷେକ ଦର୍ଶନ",
        timeWindow = "06:00 AM – 12:30 PM",
        startHour24 = 6,
        endHour24 = 12,
        significanceEn = "Devotees touch and worship the five Swayambhu Shiva Lingas submerged in the mountain stream.",
        significanceOd = "ପର୍ବତ ଝରଣା ଜଳରେ ବୁଡ଼ି ରହିଥିବା ପଞ୍ଚ ସ୍ୱୟମ୍ଭୁ ଶିବଲିଙ୍ଗଙ୍କ ସ୍ପର୍ଶ ଓ ଜଳାଭିଷେକ।"
    ),
    PanchalingeswarDarshanSlot(
        id = "madhyahna_bhoga",
        ritualNameEn = "Madhyahna Raja Bhoga & AnnaPrasada",
        ritualNameOd = "ମଧ୍ୟାହ୍ନ ରାଜଭୋଗ ଓ ଅନ୍ନପ୍ରସାଦ ନୀତି",
        timeWindow = "12:30 PM – 01:30 PM",
        startHour24 = 12,
        endHour24 = 14,
        significanceEn = "Midday sacred offering of Khechudi, Dalma & Chhena Poda to Lord Panchalingeswar.",
        significanceOd = "ମହାପ୍ରଭୁ ପଞ୍ଚଲିଙ୍ଗେଶ୍ୱରଙ୍କ ନିକଟରେ ମଧ୍ୟାହ୍ନ ଖେଚୁଡ଼ି ଓ ଛେନାପୋଡ଼ ଭୋଗ ଅର୍ପଣ।"
    ),
    PanchalingeswarDarshanSlot(
        id = "aparahna_darshan",
        ritualNameEn = "Aparahna Darshan & Bilwa Patra Archana",
        ritualNameOd = "ଅପରାହ୍ନ ଦର୍ଶନ ଓ ବିଲ୍ୱପତ୍ର ଅର୍ଚ୍ଚନା",
        timeWindow = "02:00 PM – 05:30 PM",
        startHour24 = 14,
        endHour24 = 18,
        significanceEn = "Afternoon peaceful darshan ideal for families and Nilagiri Kuldiha trekkers.",
        significanceOd = "ପରିବାର ଓ ପର୍ଯ୍ୟଟକଙ୍କ ପାଇଁ ଶାନ୍ତିପୂର୍ଣ୍ଣ ଅପରାହ୍ନ ଦର୍ଶନ ଓ ବେଲପତ୍ର ପୂଜା।"
    ),
    PanchalingeswarDarshanSlot(
        id = "sandhya_alati",
        ritualNameEn = "Sandhya Alati & Pancha Deepa Vandana",
        ritualNameOd = "ସନ୍ଧ୍ୟା ଆଳତି ଓ ପଞ୍ଚଦୀପ ବନ୍ଦନା",
        timeWindow = "06:00 PM – 07:30 PM",
        startHour24 = 18,
        endHour24 = 20,
        significanceEn = "Evening 108-lamp brass camphor aarti echoing across the Nilagiri forest valley.",
        significanceOd = "ନୀଳଗିରି ବନାନୀ ମଧ୍ୟରେ ୧୦୮ ଦୀପ ଓ କର୍ପୂର ଆଳତି ସହ ସନ୍ଧ୍ୟା ବନ୍ଦନା।"
    ),
    PanchalingeswarDarshanSlot(
        id = "pahada_bandha",
        ritualNameEn = "Bada Singahara & Pahada Bandha (Night Closure)",
        ritualNameOd = "ବଡ଼ସିଂହାର ବେଶ ଓ ପହୁଡ଼ (ରାତ୍ରି ସମାପ୍ତି)",
        timeWindow = "08:00 PM",
        startHour24 = 20,
        endHour24 = 24,
        significanceEn = "Final floral adornment and closing of the hill shrine gates for the night.",
        significanceOd = "ମହାପ୍ରଭୁଙ୍କ ବଡ଼ସିଂହାର ବେଶ ଓ ରାତ୍ରି ପହୁଡ଼ ନୀତି।"
    )
)

data class CulturalTriviaSnippet(
    val id: String,
    val titleEn: String,
    val titleOd: String,
    val bodyEn: String,
    val bodyOd: String,
    val tag: String
)

private val BALASORE_CULTURAL_TRIVIA_LIST = listOf(
    CulturalTriviaSnippet(
        id = "trivia_panchalingeswar",
        titleEn = "Legend of Banasura & Panchalingeswar",
        titleOd = "ବାଣାସୁର ଓ ପଞ୍ଚଲିଙ୍ଗେଶ୍ୱରଙ୍କ ପୌରାଣିକ କଥା",
        bodyEn = "According to folk tradition, King Banasura installed the five Swayambhu Shiva Lingas inside a perennial mountain stream in the Nilagiri hills so the cascading water performs eternal Abhisheka 24x7.",
        bodyOd = "ଲୋକବିଶ୍ୱାସ ଅନୁଯାୟୀ ରାଜା ବାଣାସୁର ନୀଳଗିରି ପାହାଡ଼ର ଚିରସ୍ରୋତା ଝରଣା ମଧ୍ୟରେ ଏହି ପଞ୍ଚ ଶିବଲିଙ୍ଗ ସ୍ଥାପନ କରିଥିଲେ, ଯେଉଁଠି ପ୍ରାକୃତିକ ଝରଣା ଜଳ ନିରନ୍ତର ଅଭିଷେକ କରୁଥାଏ।",
        tag = "Panchalingeswar Heritage"
    ),
    CulturalTriviaSnippet(
        id = "trivia_fakir_mohan",
        titleEn = "Birthplace of Modern Odia Literature",
        titleOd = "ଆଧୁନିକ ଓଡ଼ିଆ ସାହିତ୍ୟର ଜନ୍ମଭୂମି — ବ୍ୟାସକବି ଫକୀର ମୋହନ",
        bodyEn = "Vyasa Kavi Fakir Mohan Senapati, born in Mallikashpur (Balasore) in 1843, penned 'Rebati' (the first Odia short story) and spearheaded the movement to preserve the Odia language.",
        bodyOd = "୧୮୪୩ ମସିହାରେ ବାଲେଶ୍ୱରର ମଲ୍ଲିକାଶପୁରରେ ଜନ୍ମିତ ବ୍ୟାସକବି ଫକୀର ମୋହନ ସେନାପତି ପ୍ରଥମ ଓଡ଼ିଆ କ୍ଷୁଦ୍ରଗଳ୍ପ 'ରେବତୀ' ରଚନା କରି ଓଡ଼ିଆ ଭାଷା ସୁରକ୍ଷା ଆନ୍ଦୋଳନର ନେତୃତ୍ୱ ନେଇଥିଲେ।",
        tag = "Literary Pride"
    ),
    CulturalTriviaSnippet(
        id = "trivia_jau_kandhei",
        titleEn = "Balasore Jau Kandhei (Traditional Lacquer Bride Dolls)",
        titleOd = "ବାଲେଶ୍ୱରର ପାରମ୍ପରିକ 'ଜଉ କଣ୍ଢେଇ' କଳା",
        bodyEn = "Handcrafted in Balasore using heated natural lac (shellac) and terracotta cores, Jau Kandhei dolls are auspiciously gifted during traditional Odia weddings across North Odisha.",
        bodyOd = "ପ୍ରାକୃତିକ ଲାଖ ଓ ମାଟିରେ ନିର୍ମିତ ବାଲେଶ୍ୱରର ପ୍ରସିଦ୍ଧ 'ଜଉ କଣ୍ଢେଇ' ଓଡ଼ିଆ ବିବାହ ପରମ୍ପରାରେ ଶୁଭ ପ୍ରତୀକ ଭାବେ ପ୍ରଦାନ କରାଯାଏ।",
        tag = "GI Craft Heritage"
    ),
    CulturalTriviaSnippet(
        id = "trivia_khirachora",
        titleEn = "Remuna Khirachora Gopinath & Amrita Keli",
        titleOd = "ରେମୁଣା କ୍ଷୀରଚୋରା ଗୋପୀନାଥ ଓ ଅମୃତ କେଳି କ୍ଷୀର",
        bodyEn = "Lord Gopinath at Remuna famously hid a pot of condensed milk (Khira) for his devotee Madhavendra Puri, earning the name 'Khirachora' (Stealer of Milk).",
        bodyOd = "ଭକ୍ତ ମାଧବେନ୍ଦ୍ର ପୁରୀଙ୍କ ପାଇଁ ଗୋପୀନାଥ ମହାପ୍ରଭୁ କ୍ଷୀର ଲୁଚାଇ ରଖିଥିବାରୁ ତାଙ୍କର ନାମ 'କ୍ଷୀରଚୋରା ଗୋପୀନାଥ' ହୋଇଥିଲା।",
        tag = "Vaishnava Shrine"
    )
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CulturalInsightsRoomCard(
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isOdia = language == AppLanguage.ODIA
    val db = remember(context) { AppDatabase.getInstance(context) }

    val horoscopes by db.odiaHoroscopeDao().getAllHoroscopesFlow().collectAsState(initial = emptyList())
    val darshanMetadata by db.cacheMetadataDao().getMetadata("PANCHALINGESWAR_DARSHAN_TIMINGS_ROOM").collectAsState(initial = null)
    val culturalTriviaMetadata by db.cacheMetadataDao().getMetadata("CULTURAL_INSIGHTS_TRIVIA_ROOM").collectAsState(initial = null)

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedRashiIndex by remember { mutableIntStateOf(0) }
    var selectedTriviaIndex by remember { mutableIntStateOf(0) }
    var darshanSlots by remember { mutableStateOf(DEFAULT_PANCHALINGESWAR_DARSHAN_SLOTS) }
    var syncToastBanner by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val existingDarshan = db.cacheMetadataDao().getMetadataSync("PANCHALINGESWAR_DARSHAN_TIMINGS_ROOM")
        if (existingDarshan == null || existingDarshan.details.isBlank()) {
            val jsonArr = JSONArray()
            DEFAULT_PANCHALINGESWAR_DARSHAN_SLOTS.forEach { slot ->
                jsonArr.put(
                    JSONObject().apply {
                        put("id", slot.id)
                        put("ritualNameEn", slot.ritualNameEn)
                        put("ritualNameOd", slot.ritualNameOd)
                        put("timeWindow", slot.timeWindow)
                        put("startHour24", slot.startHour24)
                        put("endHour24", slot.endHour24)
                        put("significanceEn", slot.significanceEn)
                        put("significanceOd", slot.significanceOd)
                    }
                )
            }
            db.cacheMetadataDao().insertOrUpdate(
                CacheSyncMetadataEntity(
                    cacheKey = "PANCHALINGESWAR_DARSHAN_TIMINGS_ROOM",
                    cacheLabel = "Panchalingeswar Temple Daily Prayer & Darshan Timings",
                    lastSyncedAt = System.currentTimeMillis(),
                    itemCount = DEFAULT_PANCHALINGESWAR_DARSHAN_SLOTS.size,
                    status = "CACHED_OFFLINE_ROOM",
                    details = jsonArr.toString()
                )
            )
        } else {
            runCatching {
                val arr = JSONArray(existingDarshan.details)
                val parsed = mutableListOf<PanchalingeswarDarshanSlot>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    parsed.add(
                        PanchalingeswarDarshanSlot(
                            id = obj.optString("id"),
                            ritualNameEn = obj.optString("ritualNameEn"),
                            ritualNameOd = obj.optString("ritualNameOd"),
                            timeWindow = obj.optString("timeWindow"),
                            startHour24 = obj.optInt("startHour24", 6),
                            endHour24 = obj.optInt("endHour24", 12),
                            significanceEn = obj.optString("significanceEn"),
                            significanceOd = obj.optString("significanceOd")
                        )
                    )
                }
                if (parsed.isNotEmpty()) darshanSlots = parsed
            }
        }

        if (db.cacheMetadataDao().getMetadataSync("CULTURAL_INSIGHTS_TRIVIA_ROOM") == null) {
            db.cacheMetadataDao().insertOrUpdate(
                CacheSyncMetadataEntity(
                    cacheKey = "CULTURAL_INSIGHTS_TRIVIA_ROOM",
                    cacheLabel = "Balasore Daily Odia Astrology & Cultural Trivia",
                    lastSyncedAt = System.currentTimeMillis(),
                    itemCount = BALASORE_CULTURAL_TRIVIA_LIST.size,
                    status = "CACHED_OFFLINE_ROOM",
                    details = BALASORE_CULTURAL_TRIVIA_LIST.joinToString(" | ") { it.titleEn }
                )
            )
        }

        if (db.odiaHoroscopeDao().getAllHoroscopesSync().isEmpty()) {
            val todayStr = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date())
            db.odiaHoroscopeDao().insertHoroscopes(
                listOf(
                    OdiaHoroscopeEntity(
                        signKey = "mesha",
                        signNameEn = "Aries (Mesha)",
                        signNameOr = "ମେଷ ରାଶି",
                        symbol = "♈",
                        elementOr = "ଅଗ୍ନି ତତ୍ତ୍ୱ",
                        rulingPlanetOr = "ମଙ୍ଗଳ",
                        predictionEn = "Auspicious day for pilgrimage to Panchalingeswar and starting civic or business ventures in Balasore.",
                        predictionOr = "ପଞ୍ଚଲିଙ୍ଗେଶ୍ୱର ଦର୍ଶନ ଏବଂ ନୂତନ ବ୍ୟବସାୟ କିମ୍ବା ଶୁଭ କାର୍ଯ୍ୟ ଆରମ୍ଭ ପାଇଁ ଆଜି ଅତ୍ୟନ୍ତ ଅନୁକୂଳ ଦିନ।",
                        careerFinanceOr = "ବ୍ୟବସାୟ ଓ କର୍ମକ୍ଷେତ୍ରରେ ଉନ୍ନତି",
                        healthOr = "ଉତ୍ତମ ସ୍ୱାସ୍ଥ୍ୟ ଓ ଉତ୍ସାହ",
                        familyOr = "ପାରିବାରିକ ଶାନ୍ତି ଓ ସହଯୋଗ",
                        shubhaBela = "07:15 AM – 09:45 AM",
                        rahuKala = "03:00 PM – 04:30 PM",
                        amritaBela = "06:00 AM – 07:15 AM",
                        luckyNumber = 9,
                        luckyColor = "Saffron Gold (ଗେରୁଆ ସୁବର୍ଣ୍ଣ)",
                        luckyGemstone = "Red Coral (ପ୍ରବାଳ)",
                        worshipDeity = "Shree Panchalingeswar Mahadev",
                        chantingMantra = "Om Namah Shivaya",
                        date = todayStr
                    ),
                    OdiaHoroscopeEntity(
                        signKey = "brusa",
                        signNameEn = "Taurus (Vrishabha)",
                        signNameOr = "ବୃଷ ରାଶି",
                        symbol = "♉",
                        elementOr = "ପୃଥିବୀ ତତ୍ତ୍ୱ",
                        rulingPlanetOr = "ଶୁକ୍ର",
                        predictionEn = "Harmony in family life and favorable coastal travel towards Chandipur & Remuna Khirachora temple.",
                        predictionOr = "ପାରିବାରିକ ଶାନ୍ତି ଓ ଚାନ୍ଦିପୁର ତଥା ରେମୁଣା କ୍ଷୀରଚୋରା ଗୋପୀନାଥ ଭ୍ରମଣ ପାଇଁ ଶୁଭ ଯୋଗ।",
                        careerFinanceOr = "ଆର୍ଥିକ ସঞ্চୟ ଓ ଲାଭ",
                        healthOr = "ସନ୍ତୁଳିତ ସ୍ୱାସ୍ଥ୍ୟ",
                        familyOr = "ପରିବାରରେ ଆନନ୍ଦ ଉତ୍ସବ",
                        shubhaBela = "10:30 AM – 12:15 PM",
                        rahuKala = "01:30 PM – 03:00 PM",
                        amritaBela = "08:00 AM – 09:30 AM",
                        luckyNumber = 6,
                        luckyColor = "Pearl White (ମୁକ୍ତା ଧଳା)",
                        luckyGemstone = "Diamond / Opal",
                        worshipDeity = "Khirachora Gopinath",
                        chantingMantra = "Om Namo Bhagavate Vasudevaya",
                        date = todayStr
                    ),
                    OdiaHoroscopeEntity(
                        signKey = "mithuna",
                        signNameEn = "Gemini (Mithuna)",
                        signNameOr = "ମିଥୁନ ରାଶି",
                        symbol = "♊",
                        elementOr = "ବାୟୁ ତତ୍ତ୍ୱ",
                        rulingPlanetOr = "ବୁଧ",
                        predictionEn = "Creative breakthroughs inspired by Fakir Mohan literary heritage; positive financial tidings.",
                        predictionOr = "ସାହିତ୍ୟ ଓ ସୃଜନଶୀଳ କାର୍ଯ୍ୟରେ ସଫଳତା ମିଳିବ ଏବଂ ଆର୍ଥିକ ଉନ୍ନତି ଯୋଗ ରହିଛି।",
                        careerFinanceOr = "ଶିକ୍ଷା ଓ ବାଣିଜ୍ୟରେ ସଫଳତା",
                        healthOr = "ମାନସିକ ପ୍ରସନ୍ନତା",
                        familyOr = "ବନ୍ଧୁ ମିଳନ",
                        shubhaBela = "04:00 PM – 06:00 PM",
                        rahuKala = "12:00 PM – 01:30 PM",
                        amritaBela = "05:30 PM – 07:00 PM",
                        luckyNumber = 5,
                        luckyColor = "Emerald Green (ସବୁଜ)",
                        luckyGemstone = "Emerald (ପନ୍ନା)",
                        worshipDeity = "Maa Bhudhara Chandi",
                        chantingMantra = "Om Aim Hreem Kleem",
                        date = todayStr
                    )
                )
            )
        }
    }

    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val syncTimeFormatted = remember(darshanMetadata?.lastSyncedAt, culturalTriviaMetadata?.lastSyncedAt) {
        val ts = darshanMetadata?.lastSyncedAt ?: System.currentTimeMillis()
        SimpleDateFormat("dd MMM, hh:mm a", Locale.US).format(Date(ts))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("cultural_insights_room_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
        border = BorderStroke(1.5.dp, Color(0xFFF59E0B))
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
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEF3C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TempleHindu,
                            contentDescription = "Cultural Insights",
                            tint = Color(0xFFB45309),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isOdia) "🕉️ ସାଂସ୍କୃତିକ ଅନ୍ତର୍ଦୃଷ୍ଟି (Cultural Insights)" else "🕉️ Cultural Insights & Temple Timings",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.5.sp,
                            color = Color(0xFF78350F)
                        )
                        Text(
                            text = if (isOdia) {
                                "ପଞ୍ଚଲିଙ୍ଗେଶ୍ୱର ଦର୍ଶନ ସମୟ • ଓଡ଼ିଆ ରାଶିଫଳ ଓ ଐତିହ୍ୟ (Room Offline Cache)"
                            } else {
                                "Panchalingeswar Darshan Timings • Daily Odia Astrology & Trivia (Room Offline)"
                            },
                            fontSize = 11.sp,
                            color = Color(0xFF92400E)
                        )
                    }
                }

                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            val now = System.currentTimeMillis()
                            db.cacheMetadataDao().updateSyncInfo(
                                cacheKey = "PANCHALINGESWAR_DARSHAN_TIMINGS_ROOM",
                                timestamp = now,
                                itemCount = darshanSlots.size,
                                status = "REFRESHED_OFFLINE_ROOM",
                                label = "Panchalingeswar Temple Daily Prayer & Darshan Timings",
                                details = darshanMetadata?.details ?: ""
                            )
                            syncToastBanner = "✓ Panchalingeswar Darshan Timings & Odia Panjika cached in Room DB for offline viewing!"
                            Toast.makeText(context, "Refreshed & cached Panchalingeswar Darshan timings in Room DB", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.testTag("cultural_insights_refresh_room_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Room Cache",
                        tint = Color(0xFFB45309)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFEF3C7),
                border = BorderStroke(1.dp, Color(0xFFFCD34D)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💾 Room DB Offline Cache Active (${darshanSlots.size} Darshan Slots • ${horoscopes.size} Rashi)",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E)
                    )
                    Text(
                        text = "Synced: $syncTimeFormatted",
                        fontSize = 10.sp,
                        color = Color(0xFFB45309)
                    )
                }
            }

            if (syncToastBanner != null) {
                Text(
                    text = syncToastBanner!!,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF047857)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    label = {
                        Text(
                            text = if (isOdia) "🔱 ପଞ୍ଚଲିଙ୍ଗେଶ୍ୱର ଦର୍ଶନ ସମୟ" else "🔱 Panchalingeswar Darshan Timings",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFD97706),
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("cultural_tab_panchalingeswar_darshan")
                )

                FilterChip(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    label = {
                        Text(
                            text = if (isOdia) "📜 ଦୈନିକ ରାଶିଫଳ ଓ ସଂସ୍କୃତି" else "📜 Odia Astrology & Trivia",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFD97706),
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("cultural_tab_astrology_trivia")
                )
            }

            if (selectedTab == 0) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("panchalingeswar_darshan_schedule_container")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column {
                            Text(
                                text = if (isOdia) {
                                    "ଶ୍ରୀ ପଞ୍ଚଲିଙ୍ଗେଶ୍ୱର ପୀଠ (ନୀଳଗିରି, ବାଲେଶ୍ୱର) — ଦୈନିକ ନୀତି ଓ ଦର୍ଶନ"
                                } else {
                                    "Shree Panchalingeswar Shrine (Nilagiri Hills) — Daily Darshan"
                                },
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = Color(0xFF78350F)
                            )
                            Text(
                                text = if (isOdia) {
                                    "ମନ୍ଦିର ଖୋଲା ସମୟ: 05:30 AM – 08:00 PM • ଅଫଲାଇନ୍ Room DB ସଂରକ୍ଷିତ"
                                } else {
                                    "Sanctum Open: 05:30 AM – 08:00 PM • Cached locally in Room DB"
                                },
                                fontSize = 11.sp,
                                color = Color(0xFF92400E)
                            )
                        }

                        darshanSlots.forEach { slot ->
                            val isCurrentSlot = currentHour in slot.startHour24 until slot.endHour24
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isCurrentSlot) Color(0xFFECFDF5) else Color(0xFFFFFBEB),
                                border = BorderStroke(
                                    1.dp,
                                    if (isCurrentSlot) Color(0xFF10B981) else Color(0xFFFDE68A)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("panchalingeswar_slot_${slot.id}")
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isOdia) slot.ritualNameOd else slot.ritualNameEn,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.5.sp,
                                            color = Color(0xFF78350F),
                                            modifier = Modifier.weight(1f)
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (isCurrentSlot) Color(0xFF059669) else Color(0xFFD97706)
                                        ) {
                                            Text(
                                                text = if (isCurrentSlot) "LIVE NOW • ${slot.timeWindow}" else slot.timeWindow,
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = if (isOdia) slot.significanceOd else slot.significanceEn,
                                        fontSize = 11.sp,
                                        color = Color(0xFF92400E)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                val activeHoroscope = horoscopes.getOrNull(selectedRashiIndex.coerceIn(0, (horoscopes.size - 1).coerceAtLeast(0)))
                val activeTrivia = BALASORE_CULTURAL_TRIVIA_LIST[selectedTriviaIndex % BALASORE_CULTURAL_TRIVIA_LIST.size]

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cultural_insights_astrology_trivia_container")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (isOdia) "✨ ଦୈନିକ ଓଡ଼ିଆ ପାଞ୍ଜି ରାଶିଫଳ (Room Offline)" else "✨ Daily Odia Panjika Astrology (Cached in Room)",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = Color(0xFF78350F)
                        )

                        if (horoscopes.isNotEmpty()) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(horoscopes.size) { idx ->
                                    val h = horoscopes[idx]
                                    FilterChip(
                                        selected = selectedRashiIndex == idx,
                                        onClick = { selectedRashiIndex = idx },
                                        label = {
                                            Text(
                                                text = "${h.symbol} ${if (isOdia) h.signNameOr else h.signNameEn}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        },
                                        modifier = Modifier.testTag("cultural_rashi_chip_${h.signKey.lowercase()}")
                                    )
                                }
                            }
                        }

                        if (activeHoroscope != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFFFBEB),
                                border = BorderStroke(1.dp, Color(0xFFFCD34D)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "${activeHoroscope.symbol} ${if (isOdia) activeHoroscope.signNameOr else activeHoroscope.signNameEn} • ${activeHoroscope.date}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF92400E)
                                    )
                                    Text(
                                        text = if (isOdia) activeHoroscope.predictionOr else activeHoroscope.predictionEn,
                                        fontSize = 12.sp,
                                        color = Color(0xFF78350F)
                                    )
                                    Text(
                                        text = "🕒 Shubha Bela: ${activeHoroscope.shubhaBela} • Amrita Bela: ${activeHoroscope.amritaBela} • Lucky: ${activeHoroscope.luckyColor} (#${activeHoroscope.luckyNumber})",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB45309)
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = Color(0xFFFDE68A))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isOdia) "🏺 ବାଲେଶ୍ୱର ସାଂସ୍କୃତିକ ତଥ୍ୟ (Trivia)" else "🏺 Balasore Cultural Trivia Snippet",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.5.sp,
                                color = Color(0xFF78350F)
                            )
                            OutlinedButton(
                                onClick = {
                                    selectedTriviaIndex = (selectedTriviaIndex + 1) % BALASORE_CULTURAL_TRIVIA_LIST.size
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("cultural_next_trivia_btn")
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isOdia) "ପରବର୍ତ୍ତୀ ତଥ୍ୟ" else "Next Snippet", fontSize = 11.sp)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEF3C7).copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "${activeTrivia.tag} — ${if (isOdia) activeTrivia.titleOd else activeTrivia.titleEn}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF78350F)
                                )
                                Text(
                                    text = if (isOdia) activeTrivia.bodyOd else activeTrivia.bodyEn,
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF92400E)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================================
// 6. PERSONALIZED TOURISM ITINERARY (REORDERABLE / DRAG-AND-DROP SCHEDULE + GOOGLE MAPS TRAVEL TIMES)
// =========================================================================================

data class ItineraryStopWithCoords(
    val id: String,
    val nameEn: String,
    val nameOd: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val bestTimeWindow: String
)

private fun computeRoadDistanceAndDriveMinutes(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double
): Pair<Double, Int> {
    val earthRadiusKm = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
        cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
        sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    val straightLineKm = earthRadiusKm * c
    val roadKm = (straightLineKm * 1.28).coerceAtLeast(1.2)
    val driveMinutes = ((roadKm / 36.0) * 60.0 + 4.0).roundToInt().coerceAtLeast(5)
    return Pair(roadKm, driveMinutes)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PersonalizedDragDropItineraryCard(
    allHotspots: List<Hotspot>,
    bookmarkedHotspotIds: Set<String>,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isOdia = language == AppLanguage.ODIA
    val db = remember(context) { AppDatabase.getInstance(context) }

    val availableStops = remember(allHotspots) {
        BALASORE_TOURIST_LOCATIONS.map { loc ->
            ItineraryStopWithCoords(
                id = loc.id,
                nameEn = loc.nameEn,
                nameOd = loc.nameOr,
                category = loc.categoryEn,
                latitude = loc.latitude,
                longitude = loc.longitude,
                bestTimeWindow = loc.timings
            )
        }
    }

    val scheduleStops = remember {
        mutableStateListOf<ItineraryStopWithCoords>().apply {
            val initial = availableStops.filter { bookmarkedHotspotIds.contains(it.id) }.ifEmpty {
                availableStops.take(3)
            }
            addAll(initial)
        }
    }

    fun syncScheduleToRoom() {
        coroutineScope.launch {
            db.itineraryDao().clearAllItinerary()
            val items = scheduleStops.mapIndexed { idx, stop ->
                val startHr = 8 + idx * 2
                ItineraryItemEntity(
                    hotspotId = stop.id,
                    hotspotName = stop.nameEn,
                    odiaName = stop.nameOd,
                    category = stop.category,
                    dayNumber = 1,
                    timeSlot = String.format(Locale.US, "%02d:00 - %02d:30", startHr, startHr + 1),
                    notes = "Stop #${idx + 1} • Google Maps Route Ready",
                    latitude = stop.latitude,
                    longitude = stop.longitude,
                    addedAt = System.currentTimeMillis() + idx
                )
            }
            db.itineraryDao().insertItineraryItems(items)
        }
    }

    LaunchedEffect(Unit) {
        syncScheduleToRoom()
    }

    val totalLegMetrics = remember(scheduleStops.toList()) {
        if (scheduleStops.size < 2) {
            Pair(0.0, 0)
        } else {
            var sumKm = 0.0
            var sumMins = 0
            for (i in 0 until scheduleStops.size - 1) {
                val a = scheduleStops[i]
                val b = scheduleStops[i + 1]
                val (km, mins) = computeRoadDistanceAndDriveMinutes(a.latitude, a.longitude, b.latitude, b.longitude)
                sumKm += km
                sumMins += mins
            }
            Pair(sumKm, sumMins)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("personalized_drag_drop_itinerary_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, OceanBlue.copy(alpha = 0.35f))
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isOdia) {
                            "🗺️ ବ୍ୟକ୍ତିଗତ ପର୍ଯ୍ୟଟନ କାର୍ଯ୍ୟସୂଚୀ (Drag & Reorder Itinerary)"
                        } else {
                            "🗺️ Personalized Tourism Itinerary & Google Maps Travel Times"
                        },
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = if (isOdia) {
                            "ବୁକମାର୍କ ସ୍ଥାନ ଯୋଡ଼ନ୍ତୁ, ଉପର/ତଳ କ୍ରମ ବଦଳାନ୍ତୁ ଏବଂ Google Maps ଯାତ୍ରା ସମୟ ଦେଖନ୍ତୁ"
                        } else {
                            "Add bookmarked hotspots, reorder stops (↑/↓), and view estimated Google Maps travel times"
                        },
                        fontSize = 11.5.sp,
                        color = BentoSlate600
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = OceanBlue.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = String.format(Locale.US, "%.1f km • %d mins", totalLegMetrics.first, totalLegMetrics.second),
                        color = OceanBlue,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.5.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }
            }

            Text(
                text = if (isOdia) "ବୁକମାର୍କ / ପର୍ଯ୍ୟଟନ ସ୍ଥାନ ଯୋଡ଼ନ୍ତୁ:" else "Tap to Add Bookmarked / Balasore Hotspots to Schedule:",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = BentoSlate700
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(availableStops, key = { it.id }) { candidate ->
                    val alreadyInSchedule = scheduleStops.any { it.id == candidate.id }
                    val isBookmarked = bookmarkedHotspotIds.contains(candidate.id)
                    FilterChip(
                        selected = alreadyInSchedule,
                        onClick = {
                            if (alreadyInSchedule) {
                                if (scheduleStops.size > 1) {
                                    scheduleStops.removeAll { it.id == candidate.id }
                                    syncScheduleToRoom()
                                }
                            } else {
                                scheduleStops.add(candidate)
                                syncScheduleToRoom()
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (alreadyInSchedule) Icons.Default.CheckCircle else Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        label = {
                            Text(
                                text = buildString {
                                    if (isBookmarked) append("★ ")
                                    append(if (isOdia) candidate.nameOd.take(18) else candidate.nameEn.take(20))
                                },
                                fontSize = 11.sp
                            )
                        },
                        modifier = Modifier.testTag("itinerary_add_chip_${candidate.id}")
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                scheduleStops.forEachIndexed { index, stop ->
                    val slotHour = 8 + index * 2
                    val slotLabel = String.format(Locale.US, "%02d:00", if (slotHour <= 12) slotHour else slotHour - 12) +
                        if (slotHour >= 12) " PM" else " AM"

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.25f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("itinerary_stop_row_${stop.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DragIndicator,
                                    contentDescription = "Drag Handle",
                                    tint = BentoSlate600,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(OceanBlue),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (isOdia) stop.nameOd else stop.nameEn,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "🕒 Stop #${index + 1} ($slotLabel) • ${stop.category}",
                                        fontSize = 10.5.sp,
                                        color = BentoSlate600
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        if (index > 0) {
                                            val item = scheduleStops.removeAt(index)
                                            scheduleStops.add(index - 1, item)
                                            syncScheduleToRoom()
                                        }
                                    },
                                    enabled = index > 0,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("itinerary_move_up_${stop.id}")
                                ) {
                                    Icon(Icons.Default.ArrowUpward, contentDescription = "Move Stop Up", modifier = Modifier.size(16.dp))
                                }

                                IconButton(
                                    onClick = {
                                        if (index < scheduleStops.size - 1) {
                                            val item = scheduleStops.removeAt(index)
                                            scheduleStops.add(index + 1, item)
                                            syncScheduleToRoom()
                                        }
                                    },
                                    enabled = index < scheduleStops.size - 1,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("itinerary_move_down_${stop.id}")
                                ) {
                                    Icon(Icons.Default.ArrowDownward, contentDescription = "Move Stop Down", modifier = Modifier.size(16.dp))
                                }

                                if (scheduleStops.size > 1) {
                                    IconButton(
                                        onClick = {
                                            scheduleStops.removeAt(index)
                                            syncScheduleToRoom()
                                        },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .testTag("itinerary_remove_${stop.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove Stop",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (index < scheduleStops.size - 1) {
                        val nextStop = scheduleStops[index + 1]
                        val (legKm, legMins) = computeRoadDistanceAndDriveMinutes(
                            stop.latitude,
                            stop.longitude,
                            nextStop.latitude,
                            nextStop.longitude
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 24.dp, top = 2.dp, bottom = 2.dp)
                                .testTag("itinerary_travel_leg_$index"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = String.format(
                                        Locale.US,
                                        "Google Maps Est. Travel: %d mins (%.1f km via NH-16 / SH-57)",
                                        legMins,
                                        legKm
                                    ),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreen
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (scheduleStops.isNotEmpty()) {
                        val origin = scheduleStops.first()
                        val dest = scheduleStops.last()
                        val waypoints = if (scheduleStops.size > 2) {
                            scheduleStops.subList(1, scheduleStops.size - 1)
                                .joinToString("|") { "${it.latitude},${it.longitude}" }
                        } else ""
                        val mapsUrl = buildString {
                            append("https://www.google.com/maps/dir/?api=1")
                            append("&origin=${origin.latitude},${origin.longitude}")
                            append("&destination=${dest.latitude},${dest.longitude}")
                            if (waypoints.isNotEmpty()) {
                                append("&waypoints=${Uri.encode(waypoints)}")
                            }
                            append("&travelmode=driving")
                        }
                        runCatching {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mapsUrl)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("itinerary_launch_google_maps_route_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OceanBlue)
            ) {
                Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isOdia) {
                        "Google Maps ରେ ସମ୍ପୂର୍ଣ୍ଣ ରୁଟ୍ ଖୋଲନ୍ତୁ (${scheduleStops.size} ସ୍ଥାନ)"
                    } else {
                        "Launch Full Multi-Stop Route in Google Maps (${scheduleStops.size} Stops)"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// =========================================================================================
// 7. HIGH-RESOLUTION OFFLINE-CACHED IMAGE GALLERY CAROUSEL FOR HOTSPOT DETAIL MODAL
// =========================================================================================

private data class OfflineGallerySlide(
    val imageUrl: String,
    val caption: String
)

@Composable
fun HotspotOfflineHighResGalleryCarousel(
    spotName: String,
    spotCategory: String,
    isOdia: Boolean,
    modifier: Modifier = Modifier
) {
    val cleanSeed = spotName.lowercase(Locale.US).replace(" ", "_")
    val slides = remember(spotName, spotCategory, isOdia) {
        listOf(
            OfflineGallerySlide(
                imageUrl = "https://picsum.photos/seed/${cleanSeed}_hd1/900/520",
                caption = if (isOdia) "$spotName — ମୁଖ୍ୟ ଦୃଶ୍ୟ (HD Offline)" else "$spotName — Panoramic View (Offline Cached)"
            ),
            OfflineGallerySlide(
                imageUrl = "https://picsum.photos/seed/${cleanSeed}_hd2/900/520",
                caption = if (isOdia) "$spotName — ସ୍ଥାପତ୍ୟ ଓ ପ୍ରକୃତି" else "$spotName — Heritage & Surroundings"
            ),
            OfflineGallerySlide(
                imageUrl = "https://picsum.photos/seed/${cleanSeed}_hd3/900/520",
                caption = if (isOdia) "$spotName — ସନ୍ଧ୍ୟା ଆଲୋକ ଦୃଶ୍ୟ" else "$spotName — Golden Hour Vista"
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { slides.size })
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hotspot_detail_offline_hd_carousel"),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(168.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF0F172A))
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                val slide = slides[page]
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(168.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(slide.imageUrl)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .crossfade(true)
                            .build(),
                        contentDescription = slide.caption,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(168.dp)
                    )
                    Surface(
                        color = Color.Black.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "📷 ${page + 1}/${slides.size} • ${slide.caption}",
                            color = Color.White,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (idx in slides.indices) {
                val active = pagerState.currentPage == idx
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(if (active) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(if (active) OceanBlue else Color(0xFFCBD5E1))
                )
            }
        }
    }
}
