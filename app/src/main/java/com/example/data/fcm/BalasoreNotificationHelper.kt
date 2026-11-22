package com.example.data.fcm

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R

object BalasoreNotificationHelper {

    const val CHANNEL_ID_WEATHER = "balasore_weather_alerts"
    const val CHANNEL_NAME_WEATHER = "Urgent Weather & Tide Alerts"

    const val CHANNEL_ID_DEFENSE_TEST = "balasore_defense_advisories"
    const val CHANNEL_NAME_DEFENSE_TEST = "Coastal Defense & Missile Test Advisories"

    const val CHANNEL_ID_COASTAL_FLOOD = "balasore_coastal_flood_alerts"
    const val CHANNEL_NAME_COASTAL_FLOOD = "Coastal Flood & Sluice Warnings"

    const val CHANNEL_ID_NEWS = "balasore_breaking_news"
    const val CHANNEL_NAME_NEWS = "Balasore Breaking News"

    const val CHANNEL_ID_TRANSPORT_ALERTS = "balasore_transport_alerts"
    const val CHANNEL_NAME_TRANSPORT_ALERTS = "Balasore Transport & Delay Alerts"

    const val CHANNEL_ID_CHANDIPUR_HIGH_TIDE = "chandipur_high_tide_alerts"
    const val CHANNEL_NAME_CHANDIPUR_HIGH_TIDE = "Chandipur Beach High Tide Safety Alerts"

    const val CHANNEL_ID_TOURISM = "balasore_tourism_hotspots"
    const val CHANNEL_NAME_TOURISM = "Balasore Tourism Hotspots & Heritage Updates"

    const val EXTRA_TARGET_TAB = "target_tab"
    const val EXTRA_ARTICLE_ID = "article_id"
    const val EXTRA_SPOT_ID = "spot_id"

    const val NOTIFICATION_ID_WEATHER_BASE = 2001
    const val NOTIFICATION_ID_DEFENSE_BASE = 5001
    const val NOTIFICATION_ID_FLOOD_BASE = 4001
    const val NOTIFICATION_ID_NEWS_BASE = 3001
    const val NOTIFICATION_ID_TRANSPORT_BASE = 6001
    const val NOTIFICATION_ID_CHANDIPUR_HIGH_TIDE_BASE = 7001
    const val NOTIFICATION_ID_TOURISM_BASE = 8001

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

            // 1. Weather Alerts Channel
            val weatherChannel = NotificationChannel(
                CHANNEL_ID_WEATHER,
                CHANNEL_NAME_WEATHER,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical alerts for Bay of Bengal squalls, cyclones, Nor'westers, and Chandipur tidal surges"
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 350, 150, 350)
            }

            // 2. Coastal Flood Channel
            val floodChannel = NotificationChannel(
                CHANNEL_ID_COASTAL_FLOOD,
                CHANNEL_NAME_COASTAL_FLOOD,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Live Subarnarekha and Budhabalanga river danger gauge, sluice gate release, and storm surge warnings"
                enableLights(true)
                lightColor = Color.rgb(0, 150, 255)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400)
            }

            // 3. Coastal Defense & Missile Test Advisories Channel
            val defenseChannel = NotificationChannel(
                CHANNEL_ID_DEFENSE_TEST,
                CHANNEL_NAME_DEFENSE_TEST,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "DRDO Chandipur ITR and Kalam Island missile test windows, NOTAM airspace clearance, and coastal sea exclusions"
                enableLights(true)
                lightColor = Color.YELLOW
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }

            // 4. Breaking News Channel
            val newsChannel = NotificationChannel(
                CHANNEL_ID_NEWS,
                CHANNEL_NAME_NEWS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Breaking local news wire, district magistrate orders, and civic bulletins"
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
            }

            // 5. Transport & Delay Alerts Channel
            val transportChannel = NotificationChannel(
                CHANNEL_ID_TRANSPORT_ALERTS,
                CHANNEL_NAME_TRANSPORT_ALERTS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Live delay warnings, platform/bay reassignments, and cancellations for Balasore trains and buses"
                enableLights(true)
                lightColor = Color.rgb(234, 88, 12)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 150, 300)
            }

            // 6. Chandipur Beach High Tide Coastal Safety Channel
            val chandipurTideChannel = NotificationChannel(
                CHANNEL_ID_CHANDIPUR_HIGH_TIDE,
                CHANNEL_NAME_CHANDIPUR_HIGH_TIDE,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High tide coastal safety warnings for Chandipur-on-Sea based on local tidal data, water levels, and safe seabed return windows"
                enableLights(true)
                lightColor = Color.rgb(2, 132, 199)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 450, 200, 450, 200, 450)
            }

            // 7. Tourism Hotspots & Heritage Channel
            val tourismChannel = NotificationChannel(
                CHANNEL_ID_TOURISM,
                CHANNEL_NAME_TOURISM,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Live updates for Chandipur tidal walks, Kuldiha wildlife safaris, Panchalingeswar hill streams, and coastal heritage hotspots"
                enableLights(true)
                lightColor = Color.rgb(0, 150, 136)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 150, 250)
            }

            notificationManager.createNotificationChannel(weatherChannel)
            notificationManager.createNotificationChannel(defenseChannel)
            notificationManager.createNotificationChannel(floodChannel)
            notificationManager.createNotificationChannel(newsChannel)
            notificationManager.createNotificationChannel(transportChannel)
            notificationManager.createNotificationChannel(chandipurTideChannel)
            notificationManager.createNotificationChannel(tourismChannel)
        }
    }

    fun showWeatherAlertNotification(
        context: Context,
        title: String,
        message: String,
        alertLevel: String = "URGENT"
    ) {
        createNotificationChannels(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("target_tab", "WEATHER")
            putExtra("alert_title", title)
            putExtra("alert_message", message)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_WEATHER_BASE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_WEATHER)
            .setSmallIcon(R.drawable.ic_notification_weather)
            .setContentTitle("⚠️ $title")
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(message)
                    .setSummaryText("Baleshwar 360 Weather Warning • $alertLevel")
            )
            .setColor(0xFFD9381E.toInt()) // Alert Crimson
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(soundUri)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_notification_weather,
                "Check Tides & Forecast",
                pendingIntent
            )
            .build()

        val notificationId = NOTIFICATION_ID_WEATHER_BASE + (System.currentTimeMillis() % 100).toInt()
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    fun showCoastalFloodNotification(
        context: Context,
        title: String,
        message: String,
        riverLevelGauge: String = "Rajghat: 9.45m",
        alertLevel: String = "DANGER"
    ) {
        createNotificationChannels(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("target_tab", "WEATHER")
            putExtra("flood_alert", true)
            putExtra("alert_title", title)
            putExtra("alert_message", message)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_FLOOD_BASE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_COASTAL_FLOOD)
            .setSmallIcon(R.drawable.ic_notification_weather)
            .setContentTitle("🌊 $title")
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$message\n\nHydro Gauge: $riverLevelGauge\nAction: Evacuate low-lying riverine wards & follow ODRAF advisories.")
                    .setSummaryText("Balasore Coastal Flood Alert • $alertLevel")
            )
            .setColor(0xFF0284C7.toInt()) // Deep Cyan Blue
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(soundUri)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_notification_weather,
                "Open Flood & Sluice Radar",
                pendingIntent
            )
            .build()

        val notificationId = NOTIFICATION_ID_FLOOD_BASE + (System.currentTimeMillis() % 100).toInt()
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    fun showBreakingNewsNotification(
        context: Context,
        title: String,
        message: String,
        articleId: String? = null
    ) {
        createNotificationChannels(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("target_tab", "NEWS")
            putExtra("article_id", articleId)
            putExtra("news_title", title)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_NEWS_BASE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_NEWS)
            .setSmallIcon(R.drawable.ic_notification_news)
            .setContentTitle("⚡ $title")
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(message)
                    .setSummaryText("Baleshwar 360 Breaking Wire • Nihar Sales")
            )
            .setColor(0xFF1A56DB.toInt()) // Bento Primary Blue
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setSound(soundUri)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_notification_news,
                "Read Full Bulletin",
                pendingIntent
            )
            .build()

        val notificationId = NOTIFICATION_ID_NEWS_BASE + (System.currentTimeMillis() % 100).toInt()
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    fun showDefenseAdvisoryNotification(
        context: Context,
        title: String,
        message: String,
        notamWindow: String = "08:00 AM - 12:30 PM",
        exclusionPerimeterKm: String = "15 km"
    ) {
        createNotificationChannels(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("target_tab", "SPECIAL_FEATURES")
            putExtra("defense_advisory", true)
            putExtra("alert_title", title)
            putExtra("alert_message", message)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_DEFENSE_BASE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_DEFENSE_TEST)
            .setSmallIcon(R.drawable.ic_notification_weather)
            .setContentTitle("🚀 $title")
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$message\n\nNOTAM Window: $notamWindow\nExclusion Perimeter: $exclusionPerimeterKm seaward\nAdvisory: Artisanal & mechanized fishing vessels must steer clear of outer Wheeler Island coordinates.")
                    .setSummaryText("ITR Chandipur Coastal Defense Advisory")
            )
            .setColor(0xFFF59E0B.toInt()) // Defense Amber Gold
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setSound(soundUri)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_notification_weather,
                "Open ITR Radar",
                pendingIntent
            )
            .build()

        val notificationId = NOTIFICATION_ID_DEFENSE_BASE + (System.currentTimeMillis() % 100).toInt()
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    /**
     * Dispatches a high-priority heads-up notification for Train or Bus delays in Balasore.
     */
    fun showTransportDelayNotification(
        context: Context,
        title: String,
        message: String,
        transportNumber: String,
        delayMinutes: Int,
        platformOrBay: String,
        transportType: String = "TRAIN"
    ) {
        createNotificationChannels(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("target_tab", "ESSENTIALS")
            putExtra("open_feature_sheet", "TRANSPORT_ALERTS")
            putExtra("highlight_transport_number", transportNumber)
        }

        val notificationId = NOTIFICATION_ID_TRANSPORT_BASE + (transportNumber.hashCode() % 500).let { if (it < 0) -it else it }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val iconRes = if (transportType.equals("BUS", ignoreCase = true)) {
            R.drawable.ic_notification_weather
        } else {
            R.drawable.ic_notification_train
        }

        val badgeEmoji = if (transportType.equals("BUS", ignoreCase = true)) "🚌" else "🚆"
        val formattedTitle = "$badgeEmoji $title"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_TRANSPORT_ALERTS)
            .setSmallIcon(iconRes)
            .setContentTitle(formattedTitle)
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$message\n\n• Delay: +$delayMinutes mins\n• Platform/Bay: $platformOrBay\n• Balasore Junction / Central Bus Stand Hub")
                    .setSummaryText("Balasore Transport Alert • $transportNumber")
            )
            .setColor(if (delayMinutes >= 30) Color.rgb(220, 38, 38) else Color.rgb(234, 88, 12))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setSound(soundUri)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                iconRes,
                "View Live Alerts",
                pendingIntent
            )
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: Exception) {}
    }

    /**
     * Dispatches a high-priority heads-up push notification for Chandipur Beach 'High Tide' alerts
     * enriched with local tidal telemetry (water level, receded seabed distance, next high tide, and safe walk countdown).
     */
    fun showChandipurHighTideNotification(
        context: Context,
        title: String,
        message: String,
        tideHeightMeters: Double = 3.6,
        recededDistanceKm: Double = 0.4,
        nextHighTideTime: String = "08:15 PM",
        safeWalkMinutesRemaining: Int = 25,
        lunarCondition: String = "Spring Tide (Purnima / Amavasya Surge)"
    ) {
        createNotificationChannels(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            action = "com.example.action.OPEN_CHANDIPUR_TIDES"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("target_tab", "SPECIAL_FEATURES")
            putExtra("open_feature_sheet", "CHANDIPUR_TIDE")
            putExtra("tide_alert", true)
            putExtra("alert_title", title)
            putExtra("alert_message", message)
        }

        val notificationId = NOTIFICATION_ID_CHANDIPUR_HIGH_TIDE_BASE + (System.currentTimeMillis() % 100).toInt()

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val expandedTidalReport = buildString {
            append(message)
            append("\n\n🌊 Local Chandipur Tidal Telemetry:")
            append("\n• Tide Level: ${String.format("%.2f", tideHeightMeters)}m (Incoming High Tide)")
            append("\n• Seabed Exposure: ${String.format("%.1f", recededDistanceKm)} km from berm")
            append("\n• Next High Tide Peak: $nextHighTideTime ($lunarCondition)")
            append("\n• Safe Return Window: $safeWalkMinutesRemaining mins remaining — return to shore promenade immediately.")
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_CHANDIPUR_HIGH_TIDE)
            .setSmallIcon(R.drawable.ic_notification_weather)
            .setContentTitle("🌊 $title")
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(expandedTidalReport)
                    .setSummaryText("Chandipur Beach High Tide Safety • ${String.format("%.1f", tideHeightMeters)}m")
            )
            .setColor(Color.rgb(2, 132, 199))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(soundUri)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_notification_weather,
                "Open Chandipur Tide Timer",
                pendingIntent
            )
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: Exception) {}
    }

    fun showTourismHotspotNotification(
        context: Context,
        title: String,
        message: String,
        spotId: String = "chandipur_beach",
        spotName: String = "Chandipur Beach",
        timingWindow: String = "Open Daily • Low Tide Phenomenon",
        hotspotStatus: String = "OPEN"
    ) {
        createNotificationChannels(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TARGET_TAB, "TOURISM")
            putExtra("spot_id", spotId)
            putExtra("spot_name", spotName)
            putExtra("tourism_update_title", title)
        }

        val notificationId = NOTIFICATION_ID_TOURISM_BASE + (System.currentTimeMillis() % 100).toInt()

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val expandedBody = buildString {
            append(message)
            append("\n\n📍 Hotspot: $spotName")
            append("\n⏱️ Hours/Timings: $timingWindow")
            append("\n🏷️ Operating Status: $hotspotStatus")
            append("\n👉 Tap to open visitor guide, map route & live updates.")
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_TOURISM)
            .setSmallIcon(R.drawable.ic_notification_tourism)
            .setContentTitle("🏖️ $title")
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(expandedBody)
                    .setSummaryText("Balasore Tourism Hotspot • $spotName")
            )
            .setColor(Color.rgb(0, 121, 107)) // Deep Teal
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setSound(soundUri)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_notification_tourism,
                "Explore Hotspot Guide",
                pendingIntent
            )
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: Exception) {}
    }

    fun triggerSampleBreakingNews(context: Context) {
        showBreakingNewsNotification(
            context = context,
            title = "Balasore Medical College Opens Advanced 24/7 Cardiac Unit",
            message = "Fakir Mohan Medical College & Hospital (FMMCH) Balasore inaugurates modern Cath Lab facility and emergency cardiac care for coastal district citizens.",
            articleId = "fmmch_cardiac_unit_2026"
        )
    }

    fun triggerSampleWeatherAlert(context: Context) {
        showWeatherAlertNotification(
            context = context,
            title = "IMD Yellow Alert: Heavy Coastal Squalls Expected",
            message = "Bay of Bengal depression brings gusty winds of 45-55 km/h along Chandipur and Talasari shores. Fishermen advised caution near river mouths.",
            alertLevel = "SQUALL WARNING"
        )
    }

    fun triggerSampleTourismHotspot(context: Context) {
        showTourismHotspotNotification(
            context = context,
            title = "Kuldiha Wildlife Sanctuary: Winter Eco-Safari Open",
            message = "Morning & afternoon elephant safari permits are now available at Gohiraprada gate. Golden langurs and hornbills spotted near Rishia dam eco-camp.",
            spotId = "kuldiha_wildlife_sanctuary",
            spotName = "Kuldiha Wildlife Sanctuary",
            timingWindow = "6:00 AM - 4:30 PM (Daily)",
            hotspotStatus = "ECO-SAFARI ACTIVE"
        )
    }

    fun showCommunityAnnouncementNotification(
        context: Context,
        title: String,
        message: String
    ) {
        createNotificationChannels(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("target_tab", "NEWS")
            putExtra("news_title", title)
        }

        val notificationId = 9001 + (System.currentTimeMillis() % 100).toInt()
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_NEWS)
            .setSmallIcon(R.drawable.ic_notification_news)
            .setContentTitle("📢 $title")
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(message)
                    .setSummaryText("Baleshwar 360 • Important Community Announcement")
            )
            .setColor(0xFF7C3AED.toInt())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setSound(soundUri)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_notification_news,
                "Open Community Board",
                pendingIntent
            )
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: Exception) {}
    }

    fun triggerSampleCommunityAnnouncement(context: Context) {
        showCommunityAnnouncementNotification(
            context = context,
            title = "Important Community Announcement: Balasore Ward Sabha & Public Health Camp",
            message = "Balasore Municipality & District Administration invite residents of Wards 1–31 to the weekend Civic Town Hall & Free Health Checkup Camp at Gandhi Smruti Bhawan from 8:30 AM."
        )
    }
}
