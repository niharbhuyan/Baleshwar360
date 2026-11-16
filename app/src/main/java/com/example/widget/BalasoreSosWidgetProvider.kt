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

/**
 * SOS Home Screen Widget Provider for Baleshwar 360.
 * Provides instant 1-tap access from the device home screen to:
 *  - Police Emergency Call (112 / 100)
 *  - Fire & Rescue Call (101)
 *  - Ambulance & FM MCH Trauma Call (108)
 *  - Emergency SOS Message Trigger (dispatches pre-formatted GPS alert to saved emergency contacts)
 */
class BalasoreSosWidgetProvider : AppWidgetProvider() {

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
        if (intent.action == ACTION_REFRESH_SOS_WIDGET) {
            updateAllWidgets(context)
        }
    }

    companion object {
        const val ACTION_REFRESH_SOS_WIDGET = "com.example.widget.ACTION_REFRESH_SOS_WIDGET"
        const val ACTION_TRIGGER_WIDGET_SOS_MESSAGE = "com.example.action.TRIGGER_WIDGET_SOS_MESSAGE"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, BalasoreSosWidgetProvider::class.java)
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
            val views = RemoteViews(context.packageName, R.layout.widget_balasore_sos)

            views.setTextViewText(R.id.widget_sos_title, "BALESHWAR 360 EMERGENCY SOS")
            views.setTextViewText(R.id.widget_sos_status_badge, "🚨 24x7 Rapid Response")

            // 1. Police Call Button (112)
            val policeIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:112")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val policePendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId * 10 + 1,
                policeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_call_police, policePendingIntent)

            // 2. Fire Call Button (101)
            val fireIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:101")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val firePendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId * 10 + 2,
                fireIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_call_fire, firePendingIntent)

            // 3. Ambulance Call Button (108)
            val ambulanceIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:108")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val ambulancePendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId * 10 + 3,
                ambulanceIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_call_ambulance, ambulancePendingIntent)

            // 4. Emergency SOS Message Trigger Button
            val sosMessageIntent = Intent(context, MainActivity::class.java).apply {
                action = ACTION_TRIGGER_WIDGET_SOS_MESSAGE
                putExtra("trigger_sos_emergency_sms", true)
                putExtra("target_tab", "ESSENTIALS")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val sosMessagePendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId * 10 + 4,
                sosMessageIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_trigger_sos_sms, sosMessagePendingIntent)

            // 5. Root Click -> Open Emergency Essentials Tab
            val rootIntent = Intent(context, MainActivity::class.java).apply {
                action = "com.example.action.EMERGENCY_CONTACTS"
                putExtra("target_tab", "ESSENTIALS")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val rootPendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId * 10 + 5,
                rootIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_sos_root, rootPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
