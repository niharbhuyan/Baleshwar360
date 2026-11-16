package com.example.data.pamphlet

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * QR Code Generator for official Balasore tourism and event information posters.
 * Uses ZXing QRCodeWriter to create clean, standard QR codes that can be scanned
 * directly by Google ML Kit Barcode Scanning.
 */
object BalasoreQrCodeGenerator {

    /**
     * Generates a pure ZXing BitMatrix for QR code encoding without Android Bitmap dependencies.
     */
    fun generateBitMatrix(
        content: String,
        sizePx: Int = 512
    ): com.google.zxing.common.BitMatrix {
        val hints = mapOf(
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
            EncodeHintType.MARGIN to 1
        )
        return QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
    }

    /**
     * Generates a high-contrast QR code bitmap for a given payload string.
     */
    fun generateQrBitmap(
        content: String,
        sizePx: Int = 512,
        foregroundColor: Int = Color.BLACK,
        backgroundColor: Int = Color.WHITE
    ): Bitmap {
        val bitMatrix = generateBitMatrix(content, sizePx)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) foregroundColor else backgroundColor)
            }
        }
        return bitmap
    }

    /**
     * Generates a realistic visual poster canvas with official administration headers,
     * decorative borders, and an embedded high-resolution QR code ready for ML Kit analysis.
     */
    fun generateOfficialPosterBitmap(
        pamphlet: BalasorePamphlet,
        isOdia: Boolean = false,
        width: Int = 640,
        height: Int = 920
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Poster Background Gradient-like solid fill
        val bgPaint = Paint().apply {
            color = if (pamphlet.category == PamphletCategory.EVENT_POSTER) Color.rgb(255, 251, 235) else Color.rgb(240, 253, 250)
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Decorative Header Banner
        val headerPaint = Paint().apply {
            color = if (pamphlet.category == PamphletCategory.EVENT_POSTER) Color.rgb(180, 83, 9) else Color.rgb(13, 148, 136)
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), 130f, headerPaint)

        // Header Text: Government of Odisha / District Administration
        val headerTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 24f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("ODISHA TOURISM • DISTRICT ADMINISTRATION, BALASORE", (width / 2).toFloat(), 55f, headerTextPaint)

        val headerSubPaint = Paint().apply {
            color = Color.rgb(254, 243, 199)
            textSize = 19f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(
            if (pamphlet.category == PamphletCategory.EVENT_POSTER) "OFFICIAL EVENT INFORMATION POSTER" else "OFFICIAL TOURISM & CITIZEN DIRECTIVE",
            (width / 2).toFloat(),
            95f,
            headerSubPaint
        )

        // Title Box
        val titlePaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 28f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val displayTitle = if (isOdia) pamphlet.titleOr else pamphlet.titleEn
        val words = displayTitle.split(" ")
        var line1 = ""
        var line2 = ""
        for (w in words) {
            if ((line1 + " " + w).length < 28) {
                line1 = if (line1.isEmpty()) w else "$line1 $w"
            } else {
                line2 = if (line2.isEmpty()) w else "$line2 $w"
            }
        }
        canvas.drawText(line1, (width / 2).toFloat(), 180f, titlePaint)
        if (line2.isNotEmpty()) {
            canvas.drawText(line2, (width / 2).toFloat(), 218f, titlePaint)
        }

        // Issuing Authority Badge
        val authBadgePaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            style = Paint.Style.FILL
        }
        val authRect = RectF(40f, 245f, (width - 40).toFloat(), 290f)
        canvas.drawRoundRect(authRect, 10f, 10f, authBadgePaint)

        val authTextPaint = Paint().apply {
            color = Color.rgb(51, 65, 85)
            textSize = 18f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val displayAuth = if (isOdia) pamphlet.issuingAuthorityOr else pamphlet.issuingAuthorityEn
        canvas.drawText("ISSUED BY: $displayAuth", (width / 2).toFloat(), 276f, authTextPaint)

        // Directives Summary
        val dirHeaderPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 20f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText(if (isOdia) "ମୁଖ୍ୟ ନିର୍ଦ୍ଦେଶାବଳୀ (Key Directives):" else "Official Visitor Directives & Highlights:", 40f, 335f, dirHeaderPaint)

        val dirBodyPaint = Paint().apply {
            color = Color.rgb(51, 65, 85)
            textSize = 18f
            isAntiAlias = true
        }
        val directives = if (isOdia) pamphlet.keyDirectivesOr else pamphlet.keyDirectivesEn
        var currentY = 370f
        directives.take(4).forEachIndexed { idx, dir ->
            canvas.drawText("•  $dir", 50f, currentY, dirBodyPaint)
            currentY += 32f
        }

        // Draw Embedded QR Code in the lower section
        val qrSize = 250
        val qrBitmap = generateQrBitmap(pamphlet.qrPayload, qrSize)
        val qrLeft = (width - qrSize) / 2
        val qrTop = 520
        canvas.drawBitmap(qrBitmap, qrLeft.toFloat(), qrTop.toFloat(), null)

        // QR Frame Box
        val borderPaint = Paint().apply {
            color = if (pamphlet.category == PamphletCategory.EVENT_POSTER) Color.rgb(217, 119, 6) else Color.rgb(13, 148, 136)
            strokeWidth = 4f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        canvas.drawRoundRect(
            RectF(qrLeft - 8f, qrTop - 8f, qrLeft + qrSize + 8f, qrTop + qrSize + 8f),
            12f,
            12f,
            borderPaint
        )

        // QR instruction label
        val qrLabelPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 17f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(
            if (isOdia) "ସ୍ମାର୍ଟଫୋନ୍ କ୍ୟାମେରା ବା ML Kit ସ୍କାନର୍ ଦ୍ୱାରା ସ୍କାନ୍ କରନ୍ତୁ" else "SCAN WITH ML KIT TO ACCESS DIGITAL POSTER & PASS",
            (width / 2).toFloat(),
            qrTop + qrSize + 36f,
            qrLabelPaint
        )

        // Footer reference
        val footerPaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 15f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("REF: ${pamphlet.officialReferenceNumber} • BALASORE 360 OFFICIAL ARCHIVE", (width / 2).toFloat(), (height - 35).toFloat(), footerPaint)

        return bitmap
    }
}
