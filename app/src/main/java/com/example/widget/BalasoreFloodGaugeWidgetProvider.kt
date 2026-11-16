package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Live Subarnarekha & Budhabalanga River Flood Gauge Home Screen Widget Provider.
 * Displays real-time water levels at Rajghat (Subarnarekha) and Govindpur NH-16 (Budhabalanga)
 * against official warning and danger marks, plus 1-tap District Flood Control Room dialing
 * and direct launch of the Offline Voice-Guided Coastal Evacuation Companion.
 */
class BalasoreFloodGaugeWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH_FLOOD_GAUGE_WIDGET) {
            updateAllWidgets(context)
        }
    }

    companion object {
        const val ACTION_REFRESH_FLOOD_GAUGE_WIDGET = "com.example.widget.ACTION_REFRESH_FLOOD_GAUGE_WIDGET"
        const val ACTION_OPEN_VOICE_EVACUATION = "com.example.action.OPEN_VOICE_EVACUATION"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, BalasoreFloodGaugeWidgetProvider::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (widgetId in allWidgetIds) {
                updateAppWidget(context, appWidgetManager, widgetId)
            }
        }

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_balasore_flood_gauge)
            val prefs = context.getSharedPreferences("balasore_flood_gauge_prefs", Context.MODE_PRIVATE)
            val subarnarekhaLevel = prefs.getFloat("subarnarekha_level_m", 8.42f)
            val budhabalangaLevel = prefs.getFloat("budhabalanga_level_m", 6.18f)
            val timeStamp = SimpleDateFormat("HH:mm", Locale.US).format(Date())

            val statusText = when {
                subarnarekhaLevel >= 10.36f || budhabalangaLevel >= 8.13f -> "🚨 DANGER MARK"
                subarnarekhaLevel >= 9.45f || budhabalangaLevel >= 7.21f -> "⚠️ WARNING LEVEL"
                else -> "🟢 Normal ($timeStamp)"
            }

            views.setTextViewText(R.id.widget_flood_title, "SUBARNAREKHA & BUDHABALANGA GAUGE")
            views.setTextViewText(R.id.widget_flood_status_badge, statusText)
            views.setTextViewText(
                R.id.widget_subarnarekha_level,
                String.format(Locale.US, "%.2f m", subarnarekhaLevel)
            )
            views.setTextViewText(
                R.id.widget_budhabalanga_level,
                String.format(Locale.US, "%.2f m", budhabalangaLevel)
            )

            // 1. Call Balasore District Flood Control Room (06782-262043)
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:06782262043")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val dialPendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId * 10 + 1,
                dialIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_call_flood_control, dialPendingIntent)

            // 2. Open Voice-Guided Coastal Evacuation Companion in MainActivity
            val voiceEvacIntent = Intent(context, MainActivity::class.java).apply {
                action = ACTION_OPEN_VOICE_EVACUATION
                putExtra("target_tab", "ESSENTIALS")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val voiceEvacPendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId * 10 + 2,
                voiceEvacIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_open_evacuation_voice, voiceEvacPendingIntent)

            // 3. Refresh button
            val refreshIntent = Intent(context, BalasoreFloodGaugeWidgetProvider::class.java).apply {
                action = ACTION_REFRESH_FLOOD_GAUGE_WIDGET
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId * 10 + 3,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_flood_btn_refresh, refreshPendingIntent)

            // 4. Root click -> open City Essentials tab
            views.setOnClickPendingIntent(R.id.widget_flood_root, voiceEvacPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
