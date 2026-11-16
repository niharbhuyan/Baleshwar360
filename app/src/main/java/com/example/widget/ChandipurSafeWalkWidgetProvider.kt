package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.service.ChandipurSafeWalkService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Home Screen Widget for Chandipur Beach 'Vanishing Sea' Safe Walk Window.
 * Displays real-time low-tide safe walk windows and sea recession countdown
 * backed by cached tidal telemetry and updated via WorkManager.
 */
class ChandipurSafeWalkWidgetProvider : AppWidgetProvider() {

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
        if (intent.action == ACTION_REFRESH_TIDES || intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, ChandipurSafeWalkWidgetProvider::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (widgetId in allWidgetIds) {
                updateAppWidget(context, appWidgetManager, widgetId)
            }
        }
    }

    companion object {
        const val ACTION_REFRESH_TIDES = "com.example.widget.ACTION_REFRESH_CHANDIPUR_TIDES"
        const val ACTION_OPEN_CHANDIPUR_TIDES = "com.example.action.OPEN_CHANDIPUR_TIDES"

        fun updateAllWidgets(context: Context) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val thisWidget = ComponentName(context, ChandipurSafeWalkWidgetProvider::class.java)
                val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
                if (allWidgetIds.isNotEmpty()) {
                    for (widgetId in allWidgetIds) {
                        updateAppWidget(context, appWidgetManager, widgetId)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.w("ChandipurWidget", "Failed to update widgets: ${e.message}")
            }
        }

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_chandipur_safe_walk)
            val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

            val window = try {
                ChandipurSafeWalkService.calculateNextSafeWalkWindow(context)
            } catch (e: Exception) {
                null
            }

            if (window != null) {
                val openStr = timeFormat.format(Date(window.windowOpenEpochMs))
                val closeStr = timeFormat.format(Date(window.windowCloseEpochMs))
                val recKm = String.format(Locale.US, "%.1f", window.estimatedRecessionKm)

                if (window.isCurrentlyInWindow) {
                    // Safe walk window is currently active!
                    if (window.isEndingSoon) {
                        views.setTextViewText(R.id.widget_tide_badge, "⚠️ RETURN TO SHORE")
                        views.setTextViewText(R.id.widget_safe_walk_status_header, "HIGH TIDE RETURNING")
                    } else {
                        views.setTextViewText(R.id.widget_tide_badge, "🟢 SAFE WALK OPEN")
                        views.setTextViewText(R.id.widget_safe_walk_status_header, "SEA RECEDED ~$recKm KM")
                    }

                    views.setTextViewText(R.id.widget_safe_walk_time_val, "${window.minutesRemainingInWindow}m")
                    views.setTextViewText(R.id.widget_safe_walk_time_lbl, "REMAINING")
                    views.setTextViewText(R.id.widget_safe_walk_window_range, "Open until $closeStr (Receded: $recKm km)")
                    views.setTextViewText(
                        R.id.widget_safe_walk_description,
                        if (window.isEndingSoon) "Tide reversing! Head back to beach promenade."
                        else "Safe to walk on the dry seafloor • Chandipur Beach"
                    )
                } else {
                    // Window is upcoming
                    views.setTextViewText(R.id.widget_tide_badge, "🌊 Recedes ~$recKm km")
                    views.setTextViewText(R.id.widget_safe_walk_status_header, "UPCOMING SAFE WALK")
                    views.setTextViewText(R.id.widget_safe_walk_time_val, openStr)
                    views.setTextViewText(R.id.widget_safe_walk_time_lbl, "NEXT OPEN")
                    views.setTextViewText(R.id.widget_safe_walk_window_range, "Window: $openStr - $closeStr")
                    views.setTextViewText(
                        R.id.widget_safe_walk_description,
                        "Bay of Bengal vanishes up to $recKm km at low tide"
                    )
                }
            } else {
                views.setTextViewText(R.id.widget_tide_badge, "🌊 Chandipur Tides")
                views.setTextViewText(R.id.widget_safe_walk_time_val, "--:--")
                views.setTextViewText(R.id.widget_safe_walk_time_lbl, "LOW TIDE")
                views.setTextViewText(R.id.widget_safe_walk_status_header, "CHANDIPUR TIDAL RADAR")
                views.setTextViewText(R.id.widget_safe_walk_window_range, "Tap to open safe walk forecast")
                views.setTextViewText(R.id.widget_safe_walk_description, "Real-time tidal telemetry & safe seafloor alerts")
            }

            // Click action to open app Chandipur Tides screen
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                action = ACTION_OPEN_CHANDIPUR_TIDES
                putExtra("feature_sheet", "CHANDIPUR_TIDE")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openPendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId * 10 + 1,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, openPendingIntent)
            views.setOnClickPendingIntent(R.id.widget_btn_open_safe_walk, openPendingIntent)

            // Refresh action
            val refreshIntent = Intent(context, ChandipurSafeWalkWidgetProvider::class.java).apply {
                action = ACTION_REFRESH_TIDES
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId * 10 + 2,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_tide_btn_refresh, refreshPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
