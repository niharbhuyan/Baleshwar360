package com.example

import com.example.data.fcm.FcmSevereWeatherHandler
import com.example.data.notification.BalasoreSevereWeatherAlertWorker
import com.example.data.remote.OpenWeatherAlertDto
import com.example.data.remote.OpenWeatherCondition
import com.example.data.remote.OpenWeatherCurrentResponse
import com.example.data.remote.OpenWeatherMain
import com.example.data.remote.OpenWeatherWind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BalasoreSevereWeatherFcmWorkerTest {

    @Test
    fun evaluateSevereWeatherWarning_detectsOfficialOpenWeatherSevereAlertForBalasore() {
        val officialAlerts = listOf(
            OpenWeatherAlertDto(
                senderName = "IMD Bhubaneswar",
                event = "Severe Cyclone Warning",
                start = 1_764_200_000L,
                end = 1_764_221_600L,
                description = "Severe cyclonic storm approaching Balasore & Chandipur coast with 95 km/h gusts.",
                tags = listOf("Severe", "Wind", "Flood")
            )
        )

        val detection = BalasoreSevereWeatherAlertWorker.evaluateSevereWeatherWarning(
            current = null,
            upcomingForecast = emptyList(),
            officialAlerts = officialAlerts
        )

        assertNotNull(detection)
        assertEquals("SEVERE", detection!!.alertLevel)
        assertTrue(detection.title.contains("Severe Cyclone Warning"))
        assertTrue(detection.title.contains("Balasore"))
        assertTrue(detection.message.contains("IMD Bhubaneswar"))
    }

    @Test
    fun evaluateSevereWeatherWarning_detectsSevereConditionTextAndIgnoresNormalWeather() {
        val severeCurrent = OpenWeatherCurrentResponse(
            weather = listOf(
                OpenWeatherCondition(id = 212, main = "Severe Thunderstorm", description = "severe thunderstorm")
            ),
            main = OpenWeatherMain(temp = 28.5, feelsLike = 31.0),
            wind = OpenWeatherWind(speed = 16.0, gust = 22.0),
            name = "Balasore"
        )
        val severeResult = BalasoreSevereWeatherAlertWorker.evaluateSevereWeatherWarning(severeCurrent)
        assertNotNull(severeResult)
        assertEquals("SEVERE", severeResult!!.alertLevel)
        assertTrue(severeResult.title.contains("Balasore"))

        val calmCurrent = OpenWeatherCurrentResponse(
            weather = listOf(
                OpenWeatherCondition(id = 801, main = "Clouds", description = "few clouds")
            ),
            main = OpenWeatherMain(temp = 29.0, feelsLike = 31.0),
            wind = OpenWeatherWind(speed = 3.5, gust = 4.2),
            name = "Balasore"
        )
        assertNull(BalasoreSevereWeatherAlertWorker.evaluateSevereWeatherWarning(calmCurrent))
    }

    @Test
    fun fcmSevereWeatherHandler_buildsPayloadAndClassifiesTriggersCorrectly() {
        val syncData = mapOf("type" to FcmSevereWeatherHandler.ACTION_OPENWEATHER_SEVERE_CHECK)
        assertTrue(FcmSevereWeatherHandler.shouldTriggerBackgroundOpenWeatherCheck(syncData))
        assertTrue(FcmSevereWeatherHandler.shouldTriggerBackgroundOpenWeatherCheck(mapOf("check_openweather" to "true")))
        assertFalse(FcmSevereWeatherHandler.shouldTriggerBackgroundOpenWeatherCheck(mapOf("type" to "NEWS")))

        assertTrue(
            FcmSevereWeatherHandler.isSevereAlertPayload(
                data = mapOf("type" to "SEVERE_WEATHER", "severity" to "Severe"),
                title = "Severe Alert",
                body = "Balasore storm"
            )
        )
        assertFalse(
            FcmSevereWeatherHandler.isSevereAlertPayload(
                data = mapOf("type" to "NEWS"),
                title = "Festival Schedule",
                body = "Local cultural fair"
            )
        )

        val detection = BalasoreSevereWeatherAlertWorker.evaluateSevereWeatherWarning(
            current = null,
            officialAlerts = listOf(
                OpenWeatherAlertDto(
                    senderName = "IMD Odisha",
                    event = "Severe Thunderstorm Alert",
                    start = 1_764_200_000L,
                    description = "Severe lightning expected over Balasore.",
                    tags = listOf("Severe")
                )
            )
        )!!
        val payload = FcmSevereWeatherHandler.buildFcmPayload(
            detection = detection,
            senderName = "IMD Odisha",
            fcmMessageId = "msg-42",
            timestampMillis = 1_764_200_500L
        )
        assertEquals("SEVERE_WEATHER", payload["type"])
        assertEquals("SEVERE", payload["severity"])
        assertEquals("Balasore", payload["location"])
        assertEquals("OPENWEATHERMAP", payload["source"])
        assertEquals("IMD Odisha", payload["sender"])
        assertEquals("msg-42", payload["fcm_message_id"])
    }
}
