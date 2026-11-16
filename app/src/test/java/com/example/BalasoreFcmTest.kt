package com.example

import com.example.data.fcm.BalasoreNotificationHelper
import com.example.data.fcm.FcmManager
import com.example.data.model.UserWeatherPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BalasoreFcmTest {

    @Test
    fun `verify FCM topic strings match Balasore specifications`() {
        assertEquals("balasore_weather_alerts", FcmManager.TOPIC_WEATHER_ALERTS)
        assertEquals("balasore_breaking_news", FcmManager.TOPIC_BREAKING_NEWS)
        assertEquals("balasore_tourism_hotspots", FcmManager.TOPIC_TOURISM_HOTSPOTS)
        assertEquals("chandipur_high_tide_alerts", FcmManager.TOPIC_CHANDIPUR_HIGH_TIDE)
        assertEquals("balasore_flood_alerts", FcmManager.TOPIC_EMERGENCY_FLOOD_ALERTS)
    }

    @Test
    fun `verify Notification Channel IDs match FCM topics for consistent routing`() {
        assertEquals(FcmManager.TOPIC_WEATHER_ALERTS, BalasoreNotificationHelper.CHANNEL_ID_WEATHER)
        assertEquals(FcmManager.TOPIC_BREAKING_NEWS, BalasoreNotificationHelper.CHANNEL_ID_NEWS)
        assertEquals(FcmManager.TOPIC_TOURISM_HOTSPOTS, BalasoreNotificationHelper.CHANNEL_ID_TOURISM)
    }

    @Test
    fun `verify notification intent keys and IDs are well defined`() {
        assertEquals("target_tab", BalasoreNotificationHelper.EXTRA_TARGET_TAB)
        assertEquals("article_id", BalasoreNotificationHelper.EXTRA_ARTICLE_ID)
        assertEquals("spot_id", BalasoreNotificationHelper.EXTRA_SPOT_ID)
        assertTrue(BalasoreNotificationHelper.NOTIFICATION_ID_WEATHER_BASE > 0)
        assertTrue(BalasoreNotificationHelper.NOTIFICATION_ID_NEWS_BASE > 0)
        assertTrue(BalasoreNotificationHelper.NOTIFICATION_ID_TOURISM_BASE > 0)
        assertTrue(BalasoreNotificationHelper.NOTIFICATION_ID_WEATHER_BASE != BalasoreNotificationHelper.NOTIFICATION_ID_NEWS_BASE)
        assertTrue(BalasoreNotificationHelper.NOTIFICATION_ID_TOURISM_BASE != BalasoreNotificationHelper.NOTIFICATION_ID_NEWS_BASE)
    }

    @Test
    fun `verify state flows in FcmManager are initialized`() {
        assertNotNull(FcmManager.weatherAlertsEnabled)
        assertNotNull(FcmManager.breakingNewsEnabled)
        assertNotNull(FcmManager.tourismHotspotsEnabled)
        assertNotNull(FcmManager.fcmToken)
    }

    @Test
    fun `verify UserWeatherPreferences serialization and deserialization for push categories`() {
        val original = UserWeatherPreferences(
            userId = "test_user_balasore",
            notifyBreakingNews = true,
            notifyCyclone = true,
            notifyTourismHotspots = true,
            notifyDefenseAdvisories = true,
            notifyFloodAlerts = true,
            notifyKuldihaSafari = true,
            notifyPanchalingeswar = true,
            notifySafeTideWindow = true
        )

        val firestoreMap = original.toFirestoreMap()
        assertEquals(true, firestoreMap["notifyBreakingNews"])
        assertEquals(true, firestoreMap["notifyCyclone"])
        assertEquals(true, firestoreMap["notifyTourismHotspots"])
        assertEquals(true, firestoreMap["notifyDefenseAdvisories"])
        assertEquals(true, firestoreMap["notifyFloodAlerts"])
        assertEquals(true, firestoreMap["notifyKuldihaSafari"])
        assertEquals(true, firestoreMap["notifyPanchalingeswar"])

        val parsed = UserWeatherPreferences.fromFirestoreMap(firestoreMap)
        assertEquals(original.userId, parsed.userId)
        assertEquals(original.notifyBreakingNews, parsed.notifyBreakingNews)
        assertEquals(original.notifyCyclone, parsed.notifyCyclone)
        assertEquals(original.notifyTourismHotspots, parsed.notifyTourismHotspots)
        assertEquals(original.notifyDefenseAdvisories, parsed.notifyDefenseAdvisories)
        assertEquals(original.notifyFloodAlerts, parsed.notifyFloodAlerts)
        assertEquals(original.notifyKuldihaSafari, parsed.notifyKuldihaSafari)
        assertEquals(original.notifyPanchalingeswar, parsed.notifyPanchalingeswar)
    }
}
