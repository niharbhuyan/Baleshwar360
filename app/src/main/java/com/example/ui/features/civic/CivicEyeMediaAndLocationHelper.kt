package com.example.ui.features.civic

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.location.LocationManagerCompat
import androidx.core.os.CancellationSignal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.math.max

/**
 * Snapshot of real device GPS coordinates acquired for a Civic Eye incident report.
 */
data class CivicGpsFix(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float? = null,
    val provider: String = "gps",
    val geoAddress: String? = null,
    val acquiredAtMillis: Long = System.currentTimeMillis()
)

/**
 * Handles real Android camera file preparation (`FileProvider`), JPEG Base64 compression for
 * cross-device Firestore persistence, and real device GPS acquisition (`LocationManagerCompat`
 * + `Geocoder`) for the Civic Eye reporting form.
 */
object CivicEyeMediaAndLocationHelper {

    private const val MAX_FIRESTORE_BASE64_CHARS = 110_000
    private const val MAX_THUMBNAIL_DIMENSION_PX = 512

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

    fun hasCameraCaptureSupport(context: Context): Boolean {
        val hasHardware = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val hasHandler = intent.resolveActivity(context.packageManager) != null
        return hasHardware || hasHandler
    }

    /**
     * Creates a durable photo output file inside `filesDir/civic_eye_photos/` (backed by
     * `file_paths.xml`) and returns `(File, contentUri)` for `ActivityResultContracts.TakePicture`.
     */
    fun createDurableCameraPhotoOutput(context: Context): Pair<File, Uri>? = runCatching {
        val photoDir = File(context.filesDir, "civic_eye_photos").apply { mkdirs() }
        val photoFile = File(photoDir, "civic_eye_${System.currentTimeMillis()}.jpg")
        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
        photoFile to contentUri
    }.getOrNull()

    /**
     * Loads a downsampled bitmap safely from a file or content URI so large camera photos never
     * cause OOM errors in Compose previews.
     */
    suspend fun decodeSampledBitmap(
        context: Context,
        file: File? = null,
        uri: Uri? = null,
        maxDimPx: Int = 1024
    ): Bitmap? = withContext(Dispatchers.IO) {
        runCatching {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            if (file != null && file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath, bounds)
            } else if (uri != null) {
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, bounds)
                }
            }
            val largest = max(bounds.outWidth, bounds.outHeight).coerceAtLeast(1)
            var sampleSize = 1
            while (largest / sampleSize > maxDimPx) {
                sampleSize *= 2
            }
            val opts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val rawBitmap = if (file != null && file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath, opts)
            } else if (uri != null) {
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, opts)
                }
            } else {
                null
            } ?: return@runCatching null

            applyExifOrientation(context, rawBitmap, file, uri)
        }.getOrNull()
    }

    /**
     * Reads the JPEG EXIF orientation tag and rotates the decoded bitmap upright so portrait
     * device camera photos display correctly in previews and Firestore attachments.
     */
    private fun applyExifOrientation(
        context: Context,
        bitmap: Bitmap,
        file: File?,
        uri: Uri?
    ): Bitmap {
        val orientation = runCatching {
            when {
                file != null && file.exists() ->
                    ExifInterface(file.absolutePath).getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                uri != null ->
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        ExifInterface(stream).getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL
                        )
                    } ?: ExifInterface.ORIENTATION_NORMAL
                else -> ExifInterface.ORIENTATION_NORMAL
            }
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

        val degrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        if (degrees == 0f) return bitmap

        return runCatching {
            val matrix = Matrix().apply { postRotate(degrees) }
            val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (rotated !== bitmap) {
                bitmap.recycle()
            }
            rotated
        }.getOrDefault(bitmap)
    }

    /**
     * Compresses a captured camera bitmap into a compact `data:image/jpeg;base64,...` string
     * guaranteed to fit comfortably within Firestore's document and security rule limits.
     */
    suspend fun encodeBitmapForFirestore(bitmap: Bitmap): String? = withContext(Dispatchers.IO) {
        runCatching {
            val width = bitmap.width
            val height = bitmap.height
            val scale = if (max(width, height) > MAX_THUMBNAIL_DIMENSION_PX) {
                MAX_THUMBNAIL_DIMENSION_PX.toFloat() / max(width, height).toFloat()
            } else {
                1f
            }
            val targetW = (width * scale).toInt().coerceAtLeast(1)
            val targetH = (height * scale).toInt().coerceAtLeast(1)
            val scaled = if (scale < 1f) {
                Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
            } else {
                bitmap
            }

            var quality = 65
            var encoded: String? = null
            while (quality >= 25) {
                val baos = ByteArrayOutputStream()
                scaled.compress(Bitmap.CompressFormat.JPEG, quality, baos)
                val b64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
                val dataUri = "data:image/jpeg;base64,$b64"
                if (dataUri.length <= MAX_FIRESTORE_BASE64_CHARS) {
                    encoded = dataUri
                    break
                }
                quality -= 10
            }
            if (scaled !== bitmap) {
                scaled.recycle()
            }
            encoded
        }.getOrNull()
    }

    /**
     * Decodes a Firestore `photoBase64` (`data:image/jpeg;base64,...` or raw base64) back into
     * a [Bitmap] for rendering in Live Feed cards across devices.
     */
    fun decodeBase64Photo(raw: String?): Bitmap? {
        if (raw.isNullOrBlank()) return null
        return runCatching {
            val payload = raw.substringAfter("base64,", raw).trim()
            val bytes = Base64.decode(payload, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }.getOrNull()
    }

    /**
     * Acquires real device GPS coordinates using Android's [LocationManager] and
     * [LocationManagerCompat.getCurrentLocation], falling back to the freshest cached provider
     * fix, and performs reverse geocoding when available.
     */
    @SuppressLint("MissingPermission")
    suspend fun acquireUserLocation(context: Context): CivicGpsFix? = withContext(Dispatchers.IO) {
        if (!hasLocationPermission(context)) return@withContext null

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return@withContext null

        val candidateProviders = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                locationManager.isProviderEnabled(LocationManager.FUSED_PROVIDER)
            ) {
                add(LocationManager.FUSED_PROVIDER)
            }
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                add(LocationManager.GPS_PROVIDER)
            }
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                add(LocationManager.NETWORK_PROVIDER)
            }
        }

        // 1. Try fresh current fix (with 6s timeout so UI stays snappy)
        var bestLocation: Location? = null
        for (provider in candidateProviders) {
            val fresh = withTimeoutOrNull(6_000L) {
                requestSingleLocation(context, locationManager, provider)
            }
            if (fresh != null) {
                bestLocation = fresh
                break
            }
        }

        // 2. Fall back to most accurate recent lastKnownLocation across enabled providers
        if (bestLocation == null) {
            val lastKnownCandidates = locationManager.getProviders(true).mapNotNull { provider ->
                runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull()
            }
            bestLocation = lastKnownCandidates.minByOrNull { loc ->
                val agePenalty = ((System.currentTimeMillis() - loc.time) / 60_000L).coerceAtLeast(0L)
                loc.accuracy + agePenalty * 10f
            }
        }

        val loc = bestLocation ?: return@withContext null
        val reverseAddress = resolveReverseGeocode(context, loc.latitude, loc.longitude)

        CivicGpsFix(
            latitude = loc.latitude,
            longitude = loc.longitude,
            accuracyMeters = if (loc.hasAccuracy()) loc.accuracy else null,
            provider = loc.provider ?: "gps",
            geoAddress = reverseAddress,
            acquiredAtMillis = System.currentTimeMillis()
        )
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestSingleLocation(
        context: Context,
        locationManager: LocationManager,
        provider: String
    ): Location? = suspendCancellableCoroutine { cont ->
        val cancellationSignal = CancellationSignal()
        cont.invokeOnCancellation { cancellationSignal.cancel() }
        try {
            LocationManagerCompat.getCurrentLocation(
                locationManager,
                provider,
                cancellationSignal,
                ContextCompat.getMainExecutor(context)
            ) { location ->
                if (cont.isActive) {
                    cont.resume(location)
                }
            }
        } catch (_: Exception) {
            if (cont.isActive) cont.resume(null)
        }
    }

    @Suppress("DEPRECATION")
    private fun resolveReverseGeocode(context: Context, lat: Double, lng: Double): String? {
        if (!Geocoder.isPresent()) return null
        return runCatching {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            val addr = addresses?.firstOrNull() ?: return@runCatching null
            listOfNotNull(
                addr.thoroughfare?.takeIf { it.isNotBlank() },
                addr.subLocality?.takeIf { it.isNotBlank() },
                addr.locality?.takeIf { it.isNotBlank() },
                addr.adminArea?.takeIf { it.isNotBlank() }
            ).distinct().joinToString(", ").takeIf { it.isNotBlank() }
        }.getOrNull()
    }
}
