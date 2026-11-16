package com.example.data.pamphlet

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.media.Image
import android.util.Log
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.common.InputImage

/**
 * ML Kit Barcode and QR Code Scanner Manager for Balasore Official Tourism and Emergency Pamphlets.
 *
 * Provides:
 * 1. ML Kit Barcode Scanner with QR_CODE optimization
 * 2. Play Services Google Code Scanner (one-tap zero-permission scanner)
 * 3. Bitmap & Frame analysis for custom CameraX scanner or Gallery QR image imports
 * 4. Automatic resolution to official Balasore tourism & emergency pamphlets
 */
object BalasoreQrCodeScannerManager {

    private const val TAG = "BalasoreQrScanner"

    val scannerOptions: BarcodeScannerOptions by lazy {
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_AZTEC,
                Barcode.FORMAT_DATA_MATRIX
            )
            .build()
    }

    /**
     * Checks if Google Play Services ML Kit barcode module is installed or requests background installation.
     */
    fun checkAndInstallMlKitModules(context: Context) {
        try {
            val moduleInstallClient = ModuleInstall.getClient(context)
            val optionalModuleApi = BarcodeScanning.getClient(scannerOptions)
            val request = ModuleInstallRequest.newBuilder()
                .addApi(optionalModuleApi)
                .build()

            moduleInstallClient.installModules(request)
                .addOnSuccessListener {
                    Log.d(TAG, "ML Kit Barcode modules verified/installed")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Optional ML Kit module install deferred: ${e.message}")
                }
        } catch (e: Exception) {
            Log.w(TAG, "Module install error: ${e.message}")
        }
    }

    /**
     * Scans a Bitmap (e.g. from gallery pick or sample official pamphlet) using ML Kit.
     */
    fun scanFromBitmap(
        bitmap: Bitmap,
        onResult: (QrScanResult) -> Unit,
        onError: (Exception) -> Unit = {}
    ) {
        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val scanner = BarcodeScanning.getClient(scannerOptions)

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        val firstQr = barcodes.first()
                        val rawValue = firstQr.rawValue ?: firstQr.displayValue ?: ""
                        val resolved = BalasorePamphletRepository.resolveQrPayload(rawValue)
                        onResult(resolved)
                    } else {
                        onResult(
                            QrScanResult(
                                rawValue = "",
                                isOfficialBalasorePamphlet = false,
                                detectedType = QrPayloadType.TEXT
                            )
                        )
                    }
                }
                .addOnFailureListener { error ->
                    Log.e(TAG, "ML Kit scan failed: ${error.message}", error)
                    onError(error)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing ML Kit scan: ${e.message}", e)
            onError(e)
        }
    }

    /**
     * Scans a CameraX frame Image using ML Kit.
     */
    fun scanFromMediaImage(
        mediaImage: Image,
        rotationDegrees: Int,
        onResult: (QrScanResult) -> Unit,
        onError: (Exception) -> Unit = {}
    ) {
        try {
            val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)
            val scanner = BarcodeScanning.getClient(scannerOptions)

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        val first = barcodes.first()
                        val raw = first.rawValue ?: first.displayValue ?: ""
                        if (raw.isNotBlank()) {
                            val resolved = BalasorePamphletRepository.resolveQrPayload(raw)
                            onResult(resolved)
                        }
                    }
                }
                .addOnFailureListener { error ->
                    onError(error)
                }
        } catch (e: Exception) {
            onError(e)
        }
    }

    /**
     * Launches the turnkey Google Code Scanner (Play Services) which opens the official
     * Google ML Kit QR viewfinder without requiring CAMERA runtime permission.
     */
    fun startGoogleCodeScanner(
        activity: Activity,
        onSuccess: (QrScanResult) -> Unit,
        onCanceled: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        try {
            val gmsOptions = GmsBarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .enableAutoZoom()
                .build()

            val scanner = GmsBarcodeScanning.getClient(activity, gmsOptions)
            scanner.startScan()
                .addOnSuccessListener { barcode ->
                    val rawValue = barcode.rawValue ?: barcode.displayValue ?: ""
                    val resolved = BalasorePamphletRepository.resolveQrPayload(rawValue)
                    onSuccess(resolved)
                }
                .addOnCanceledListener {
                    onCanceled()
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Google Code Scanner failed: ${exception.message}", exception)
                    onError(exception)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch Google Code Scanner: ${e.message}", e)
            onError(e)
        }
    }
}
