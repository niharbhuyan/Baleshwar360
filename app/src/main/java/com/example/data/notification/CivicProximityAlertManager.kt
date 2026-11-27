package com.example.data.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.data.firebase.FirestoreService
import com.example.data.local.AppDatabase
import com.example.data.local.CivicReportEntity
import com.example.ui.features.civic.normalizeCivicEyeCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

data class NearbyCivicIncident(
    val reportId: String,
    val title: String,
    val category: String,
    val wardLocation: String,
    val status: String,
    val urgency: String,
    val distanceMeters: Double
)

data class ProximityScanResult(
    val hasLocationPermission: Boolean,
    val userLatitude: Double?,
    val userLongitude: Double?,
    val radiusMeters: Int,
    val nearbyIncidents: List<NearbyCivicIncident>,
    val notificationDispatched: Boolean
)

/**
 * Manages user settings, Haversine neighborhood proximity calculations, WorkManager scheduling,
 * and high-priority notifications for nearby 'Civic Eye' incidents in Balasore.
 */
object CivicProximityAlertManager {

    private const val TAG = "CivicProximityAlert"
    private const val PREFS_NAME = "balasore_civic_proximity_prefs"
    private const val KEY_ENABLED = "civic_proximity_alerts_enabled"
    private const val KEY_RADIUS_METERS = "civic_proximity_radius_meters"
    private const val KEY_LAST_NOTIFIED_PREFIX = "notified_ts_"
    private const val CHANNEL_ID = "balasore_civic_proximity_alerts"
    private const val WORK_NAME_PERIODIC = "balasore_civic_proximity_periodic"
    private const val WORK_NAME_IMMEDIATE = "balasore_civic_proximity_immediate"
    private const val COOLDOWN_MS = 2L * 60L * 60L * 1000L // 2 hours per incident
    private const val NOTIFICATION_ID_BASE = 8420

    val AVAILABLE_RADII_METERS = listOf(500, 1000, 2000, 5000)

    private val BALASORE_WARD_CENTROIDS: List<Pair<String, Pair<Double, Double>>> = listOf(
        "ot road" to (21.4934 to 86.9336),
        "phandi" to (21.4945 to 86.9320),
        "cinema chhak" to (21.4978 to 86.9245),
        "sahadevkhunta" to (21.5015 to 86.9182),
        "azimabad" to (21.5082 to 86.9261),
        "fm university" to (21.5094 to 86.9278),
        "motiganj" to (21.4912 to 86.9388),
        "station" to (21.4930 to 86.9219),
        "sovarampur" to (21.4846 to 86.9273),
        "proof road" to (21.4860 to 86.9475),
        "chandipur" to (21.4442 to 87.0147),
        "remuna" to (21.5292 to 86.8711),
        "soro" to (21.2881 to 86.6914),
        "jaleswar" to (21.8147 to 87.2165),
        "nilagiri" to (21.4622 to 86.7715)
    )

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ENABLED, false)

    fun getRadiusMeters(context: Context): Int =
        prefs(context).getInt(KEY_RADIUS_METERS, 1000).coerceIn(250, 10000)

    fun setEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply()
        if (enabled) {
            scheduleProximityMonitor(context, triggerImmediate = true)
        } else {
            cancelProximityMonitor(context)
        }
    }

    fun setRadiusMeters(context: Context, radiusMeters: Int) {
        val safeRadius = radiusMeters.coerceIn(250, 10000)
        prefs(context).edit().putInt(KEY_RADIUS_METERS, safeRadius).apply()
        if (isEnabled(context)) {
            scheduleProximityMonitor(context, triggerImmediate = true)
        }
    }

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    /**
     * Pure Haversine formula computing great-circle distance in meters between two WGS84 points.
     */
    fun calculateDistanceMeters(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val earthRadiusMeters = 6_371_000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2.0) * sin(dLat / 2.0) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2.0) * sin(dLon / 2.0)
        val c = 2.0 * atan2(sqrt(a), sqrt(1.0 - a))
        return earthRadiusMeters * c
    }

    /**
     * Resolves coordinates for a Civic Eye report, using its exact GPS fix if present
     * or falling back to its known Balasore ward centroid.
     */
    fun resolveReportCoordinates(report: CivicReportEntity): Pair<Double, Double>? {
        val lat = report.latitude
        val lon = report.longitude
        if (lat != null && lon != null && lat in -90.0..90.0 && lon in -180.0..180.0) {
            return lat to lon
        }
        val haystack = "${report.wardLocation} ${report.geoAddress.orEmpty()}".lowercase(Locale.US)
        val wardMatch = BALASORE_WARD_CENTROIDS.firstOrNull { (keyword, _) ->
            haystack.contains(keyword)
        }
        return wardMatch?.second
    }

    /**
     * Pure filter returning all active (unresolved) Civic Eye incidents within [radiusMeters]
     * of the user's current coordinates, sorted closest-first.
     */
    fun findNearbyOpenIncidents(
        userLat: Double,
        userLon: Double,
        reports: List<CivicReportEntity>,
        radiusMeters: Int
    ): List<NearbyCivicIncident> {
        val maxDistance = radiusMeters.toDouble().coerceAtLeast(100.0)
        return reports
            .filter { report ->
                val status = report.status.trim().uppercase(Locale.US)
                status != "RESOLVED" && status != "REJECTED"
            }
            .mapNotNull { report ->
                val coords = resolveReportCoordinates(report) ?: return@mapNotNull null
                val dist = calculateDistanceMeters(userLat, userLon, coords.first, coords.second)
                if (dist <= maxDistance) {
                    NearbyCivicIncident(
                        reportId = report.id,
                        title = report.title,
                        category = normalizeCivicEyeCategory(report.category),
                        wardLocation = report.wardLocation.ifBlank { report.geoAddress ?: "Balasore" },
                        status = report.status,
                        urgency = report.urgency,
                        distanceMeters = dist
                    )
                } else {
                    null
                }
            }
            .sortedBy { it.distanceMeters }
    }

    @SuppressLint("MissingPermission")
    fun getBestLastKnownLocation(context: Context): Location? {
        if (!hasLocationPermission(context)) return null
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        val providers = runCatching { lm.getProviders(true) }.getOrDefault(emptyList())
        var best: Location? = null
        for (provider in providers) {
            val loc = runCatching { lm.getLastKnownLocation(provider) }.getOrNull() ?: continue
            if (best == null || loc.accuracy < best.accuracy) {
                best = loc
            }
        }
        return best
    }

    /**
     * Runs a live proximity scan combining Firestore `user_reports` and local Room `civic_reports`.
     * When [forceNotify] is false, respects per-incident 2-hour cooldown deduplication.
     */
    suspend fun scanNearbyIncidentsNow(
        context: Context,
        forceNotify: Boolean = false
    ): ProximityScanResult = withContext(Dispatchers.IO) {
        val appCtx = context.applicationContext
        val radius = getRadiusMeters(appCtx)
        if (!hasLocationPermission(appCtx)) {
            return@withContext ProximityScanResult(
                hasLocationPermission = false,
                userLatitude = null,
                userLongitude = null,
                radiusMeters = radius,
                nearbyIncidents = emptyList(),
                notificationDispatched = false
            )
        }

        val location = getBestLastKnownLocation(appCtx)
        val userLat = location?.latitude ?: 21.4934
        val userLon = location?.longitude ?: 86.9336

        val localReports = runCatching {
            AppDatabase.getInstance(appCtx).civicReportDao().getAllReportsSync()
        }.getOrDefault(emptyList())

        val remoteReports = runCatching {
            FirestoreService(appCtx).fetchUserReports().map { f ->
                CivicReportEntity(
                    id = f.id,
                    title = f.title,
                    category = normalizeCivicEyeCategory(f.category),
                    wardLocation = f.wardLocation,
                    description = f.description,
                    status = f.status,
                    urgency = f.urgency,
                    assignedDepartment = "Balasore Municipality",
                    reportedTimestamp = f.reportedTimestamp,
                    lastUpdatedTimestamp = f.lastUpdatedTimestamp,
                    hasPhotoAttached = !f.photoBase64.isNullOrBlank() || !f.photoUrl.isNullOrBlank(),
                    photoUri = f.photoBase64 ?: f.photoUrl,
                    latitude = f.latitude,
                    longitude = f.longitude,
                    geoAddress = f.geoAddress ?: f.wardLocation,
                    resolutionNotes = f.resolutionNotes.orEmpty()
                )
            }
        }.getOrDefault(emptyList())

        val combined = (remoteReports + localReports).distinctBy { it.id }
        val nearby = findNearbyOpenIncidents(userLat, userLon, combined, radius)

        val unnotified = if (forceNotify) {
            nearby
        } else {
            val now = System.currentTimeMillis()
            val p = prefs(appCtx)
            nearby.filter { incident ->
                val lastTs = p.getLong("$KEY_LAST_NOTIFIED_PREFIX${incident.reportId}", 0L)
                (now - lastTs) >= COOLDOWN_MS
            }
        }

        val dispatched = if (unnotified.isNotEmpty() && (forceNotify || isEnabled(appCtx))) {
            showNearbyIncidentsNotification(appCtx, unnotified, radius)
            val now = System.currentTimeMillis()
            val editor = prefs(appCtx).edit()
            unnotified.forEach { incident ->
                editor.putLong("$KEY_LAST_NOTIFIED_PREFIX${incident.reportId}", now)
            }
            editor.apply()
            true
        } else {
            false
        }

        ProximityScanResult(
            hasLocationPermission = true,
            userLatitude = userLat,
            userLongitude = userLon,
            radiusMeters = radius,
            nearbyIncidents = nearby,
            notificationDispatched = dispatched
        )
    }

    fun formatDistanceLabel(meters: Double): String {
        return if (meters < 1000.0) {
            "${meters.roundToInt()} m"
        } else {
            String.format(Locale.US, "%.1f km", meters / 1000.0)
        }
    }

    fun formatRadiusLabel(radiusMeters: Int): String {
        return if (radiusMeters < 1000) {
            "${radiusMeters}m"
        } else {
            "${radiusMeters / 1000} km"
        }
    }

    fun showNearbyIncidentsNotification(
        context: Context,
        incidents: List<NearbyCivicIncident>,
        radiusMeters: Int
    ) {
        if (incidents.isEmpty()) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Nearby Civic Eye Proximity Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts you when entering a Balasore neighborhood with reported Civic Eye issues."
                enableVibration(true)
            }
            nm.createNotificationChannel(channel)
        }

        val closest = incidents.first()
        val distLabel = formatDistanceLabel(closest.distanceMeters)
        val title = if (incidents.size == 1) {
            "Nearby Civic Eye Issue ($distLabel) • ${closest.category}"
        } else {
            "${incidents.size} Civic Eye Issues Nearby (within ${formatRadiusLabel(radiusMeters)})"
        }
        val summaryLine = "${closest.title} • ${closest.wardLocation} ($distLabel away)"
        val expandedText = buildString {
            append("Entering area with ${incidents.size} active Civic Eye report(s):\n")
            incidents.take(3).forEach { item ->
                append("• [${item.category}] ${item.title} — ${formatDistanceLabel(item.distanceMeters)} (${item.wardLocation})\n")
            }
            append("Tap to inspect on the Balasore Civic Eye map.")
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(CivicIssueNotificationHelper.EXTRA_OPEN_CIVIC_EYE, true)
            putExtra(CivicIssueNotificationHelper.EXTRA_CIVIC_REPORT_ID, closest.reportId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_BASE,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_map)
            .setContentTitle(title)
            .setContentText(summaryLine)
            .setStyle(NotificationCompat.BigTextStyle().bigText(expandedText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        nm.notify(NOTIFICATION_ID_BASE, notification)
    }

    fun scheduleProximityMonitor(context: Context, triggerImmediate: Boolean = false) {
        val wm = WorkManager.getInstance(context.applicationContext)
        val periodicRequest = PeriodicWorkRequestBuilder<CivicProximityMonitorWorker>(
            15L,
            TimeUnit.MINUTES
        ).build()

        wm.enqueueUniquePeriodicWork(
            WORK_NAME_PERIODIC,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicRequest
        )

        if (triggerImmediate) {
            val oneTime = OneTimeWorkRequestBuilder<CivicProximityMonitorWorker>().build()
            wm.enqueue(oneTime)
        }
        Log.i(TAG, "Scheduled Civic Eye proximity alerts monitor (radius=${getRadiusMeters(context)}m)")
    }

    fun cancelProximityMonitor(context: Context) {
        val wm = WorkManager.getInstance(context.applicationContext)
        wm.cancelUniqueWork(WORK_NAME_PERIODIC)
        wm.cancelUniqueWork(WORK_NAME_IMMEDIATE)
        Log.i(TAG, "Cancelled Civic Eye proximity alerts monitor")
    }
}

/**
 * WorkManager CoroutineWorker that periodically checks the user's neighborhood proximity
 * against active Civic Eye incidents when the Settings toggle is enabled.
 */
class CivicProximityMonitorWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        if (!CivicProximityAlertManager.isEnabled(applicationContext)) {
            return Result.success()
        }
        return try {
            CivicProximityAlertManager.scanNearbyIncidentsNow(
                context = applicationContext,
                forceNotify = false
            )
            Result.success()
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w("CivicProximityWorker", "Proximity scan deferred: ${e.message}")
            if (runAttemptCount < 2) Result.retry() else Result.success()
        }
    }
}
