package com.example.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.model.BalasoreWeatherAlert

object CivicIssueNotificationHelper {
    private const val CHANNEL_ID = "balasore_civic_updates_high"
    const val EXTRA_OPEN_CIVIC_EYE = "extra_open_civic_eye"
    const val EXTRA_CIVIC_REPORT_ID = "extra_civic_report_id"

    fun formatCivicStatusLabel(rawStatus: String): String {
        return when (rawStatus.trim().uppercase()) {
            "RESOLVED" -> "Resolved"
            "IN_PROGRESS", "IN PROGRESS" -> "In Progress"
            "REJECTED" -> "Rejected"
            else -> "Pending"
        }
    }

    fun showStatusChangeNotification(
        context: Context,
        reportId: String,
        title: String,
        newStatus: String,
        remark: String = "",
        previousStatus: String? = null
    ) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Civic Eye Report Status Updates",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time notifications when your Balasore Civic Eye report status changes."
                enableVibration(true)
            }
            nm.createNotificationChannel(channel)
        }

        val newLabel = formatCivicStatusLabel(newStatus)
        val transitionLine = if (!previousStatus.isNullOrBlank()) {
            val prevLabel = formatCivicStatusLabel(previousStatus)
            if (!prevLabel.equals(newLabel, ignoreCase = true)) {
                "Status changed: $prevLabel → $newLabel"
            } else {
                "Status updated: $newLabel"
            }
        } else {
            "Status updated: $newLabel"
        }

        val contentText = if (remark.isNotBlank()) {
            "$title • $remark"
        } else {
            "$title • $transitionLine"
        }
        val expandedText = buildString {
            append(transitionLine)
            append("\n")
            append(title)
            if (remark.isNotBlank()) {
                append("\nNote: ")
                append(remark)
            }
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_CIVIC_EYE, true)
            putExtra(EXTRA_CIVIC_REPORT_ID, reportId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            reportId.hashCode(),
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Civic Eye #$reportId: $newLabel")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(expandedText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        nm.notify(reportId.hashCode(), notification)
    }
}

object CivicNotificationHelper {
    private const val CHANNEL_ID = "balasore_civic_municipal_alerts"

    private fun ensureChannel(context: Context, nm: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Balasore Municipal & Ward Civic Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            nm.createNotificationChannel(channel)
        }
    }

    fun notifyCivicReportSynced(
        context: Context,
        reportId: String,
        reportTitle: String,
        wardLocation: String = "Balasore Ward",
        category: String = "Civic Issue"
    ): Boolean {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return false
        ensureChannel(context, nm)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Civic Report Synced: $category")
            .setContentText("$reportTitle ($wardLocation)")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        nm.notify(reportId.hashCode(), notification)
        return true
    }

    fun notifyWardMunicipalAlert(
        context: Context,
        wardNumber: String = "1",
        wardName: String = "Balasore Town",
        title: String,
        message: String,
        alertType: String = "MUNICIPAL_UPDATE",
        department: String = "Balasore Municipality"
    ): Boolean {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return false
        ensureChannel(context, nm)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Ward $wardNumber ($wardName): $title")
            .setContentText("$message • $department")
            .setStyle(NotificationCompat.BigTextStyle().bigText("$message ($department • $alertType)"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        nm.notify((wardNumber + title).hashCode(), notification)
        return true
    }

    fun notifyWardMunicipalAlert(
        context: Context,
        wardNumber: Int,
        wardName: String = "Balasore Town",
        title: String,
        message: String,
        alertType: String = "MUNICIPAL_UPDATE",
        department: String = "Balasore Municipality"
    ): Boolean = notifyWardMunicipalAlert(
        context = context,
        wardNumber = wardNumber.toString(),
        wardName = wardName,
        title = title,
        message = message,
        alertType = alertType,
        department = department
    )
}

class LocalAlertNotificationManager(private val context: Context) {
    private val channelId = CHANNEL_ID

    companion object {
        const val CHANNEL_ID = "balasore_coastal_alerts"

        fun createNotificationChannels(context: Context) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Balasore Severe Weather & Coastal Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                )
                nm.createNotificationChannel(channel)
            }
        }
    }

    fun createNotificationChannels() {
        createNotificationChannels(context)
    }

    fun triggerSevereAlertNotification(alert: BalasoreWeatherAlert): Boolean {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return false
        createNotificationChannels()
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(alert.title)
            .setContentText(alert.summary)
            .setStyle(NotificationCompat.BigTextStyle().bigText(alert.detailedDescription.ifBlank { alert.summary }))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        nm.notify(alert.id.hashCode(), notification)
        return true
    }

    fun dispatchAlertNotification(title: String, message: String, alertId: Int = 2001): Boolean {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return false
        createNotificationChannels()
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        nm.notify(alertId, notification)
        return true
    }
}
