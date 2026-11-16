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
import java.text.NumberFormat
import java.util.Locale

/**
 * Location-based Home Screen Widget provider for Balasore Cyclone Shelters & Block Emergency Helplines.
 * Displays real-time proximity to the nearest Multipurpose Cyclone Shelter (MPCS)
 * and the verified 24x7 emergency helpline for the user's specific Balasore block.
 */
class BalasoreShelterWidgetProvider : AppWidgetProvider() {

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
        if (intent.action == ACTION_REFRESH_LOCATION) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, BalasoreShelterWidgetProvider::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (widgetId in allWidgetIds) {
                updateAppWidget(context, appWidgetManager, widgetId)
            }
        }
    }

    companion object {
        const val ACTION_REFRESH_LOCATION = "com.example.widget.ACTION_REFRESH_SHELTER_WIDGET"
        const val ACTION_OPEN_CYCLONE_SHELTERS = "com.example.action.OPEN_CYCLONE_SHELTERS"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val state = BalasoreShelterWidgetHelper.resolveWidgetState(context)
            val views = RemoteViews(context.packageName, R.layout.widget_balasore_shelter)

            // Header titles and block badge
            views.setTextViewText(R.id.widget_title, "BALESHWAR 360 DISASTER RADAR")
            val gpsPrefix = if (state.isGpsActive) "📍" else "📌"
            views.setTextViewText(R.id.widget_block_badge, "$gpsPrefix ${state.block.nameEn} Block")

            // Distance calculation formatting
            if (state.distanceKm < 1.0) {
                val meters = (state.distanceKm * 1000).toInt().coerceAtLeast(50)
                views.setTextViewText(R.id.widget_distance_value, "$meters")
                views.setTextViewText(R.id.widget_distance_unit, "meters away")
            } else {
                val formattedDist = String.format(Locale.US, "%.1f", state.distanceKm)
                views.setTextViewText(R.id.widget_distance_value, formattedDist)
                views.setTextViewText(R.id.widget_distance_unit, "km away")
            }

            // Nearest shelter details
            views.setTextViewText(R.id.widget_nearest_label, "NEAREST CYCLONE SHELTER")
            views.setTextViewText(R.id.widget_shelter_name, state.nearestShelter.nameEn)
            val capStr = NumberFormat.getInstance().format(state.nearestShelter.capacity)
            val shelterSummary = "Cap: $capStr • ${state.nearestShelter.generatorStatus}"
            views.setTextViewText(R.id.widget_shelter_info, shelterSummary)

            // Block helpline button action
            views.setTextViewText(R.id.widget_btn_call_text, "Call ${state.block.emergencyHelpline}")
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${state.block.emergencyHelpline}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val dialPendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId * 10 + 1,
                dialIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_call_helpline, dialPendingIntent)

            // Direction / Map action to nearest shelter coordinates
            val mapGeoUri = Uri.parse(
                "geo:${state.nearestShelter.latitude},${state.nearestShelter.longitude}?q=" +
                        "${state.nearestShelter.latitude},${state.nearestShelter.longitude}(" +
                        Uri.encode(state.nearestShelter.nameEn) + ")"
            )
            val mapIntent = Intent(Intent.ACTION_VIEW, mapGeoUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val mapPendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId * 10 + 2,
                mapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_directions, mapPendingIntent)

            // Refresh action button
            val refreshIntent = Intent(context, BalasoreShelterWidgetProvider::class.java).apply {
                action = ACTION_REFRESH_LOCATION
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId * 10 + 3,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_refresh, refreshPendingIntent)

            // Click root to open application straight into Cyclone Shelters Radar
            val rootIntent = Intent(context, MainActivity::class.java).apply {
                action = ACTION_OPEN_CYCLONE_SHELTERS
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val rootPendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId * 10 + 4,
                rootIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, rootPendingIntent)

            // Commit RemoteViews update
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
