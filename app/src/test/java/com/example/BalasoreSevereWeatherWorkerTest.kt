package com.example

import com.example.data.notification.BalasoreSevereWeatherAlertWorker
import com.example.data.notification.CivicIssueNotificationHelper
import com.example.data.remote.OpenWeatherCondition
import com.example.data.remote.OpenWeatherCurrentResponse
import com.example.data.remote.OpenWeatherForecastItem
import com.example.data.remote.OpenWeatherMain
import com.example.data.remote.OpenWeatherWind
import com.example.ui.features.civic.normalizeCivicEyeCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BalasoreSevereWeatherWorkerTest {

    @Test
    fun evaluateSevereWeatherWarning_detectsThunderstormInBalasore() {
        val storm = OpenWeatherCurrentResponse(
            weather = listOf(OpenWeatherCondition(id = 211, main = "Thunderstorm", description = "thunderstorm with heavy rain")),
            main = OpenWeatherMain(temp = 28.5, feelsLike = 31.0, humidity = 88),
            wind = OpenWeatherWind(speed = 11.0, gust = 14.0)
        )

        val warning = BalasoreSevereWeatherAlertWorker.evaluateSevereWeatherWarning(storm)
        assertNotNull(warning)
        assertEquals("URGENT", warning?.alertLevel)
        assertTrue(warning!!.title.contains("Thunderstorm"))
    }

    @Test
    fun evaluateSevereWeatherWarning_detectsCycloneSquallAndExtremeWinds() {
        val cyclone = OpenWeatherCurrentResponse(
            weather = listOf(OpenWeatherCondition(id = 781, main = "Squall", description = "coastal cyclone squall")),
            main = OpenWeatherMain(temp = 27.0, feelsLike = 29.0, humidity = 92),
            wind = OpenWeatherWind(speed = 20.0, gust = 25.0)
        )

        val warning = BalasoreSevereWeatherAlertWorker.evaluateSevereWeatherWarning(cyclone)
        assertNotNull(warning)
        assertEquals("CRITICAL", warning?.alertLevel)
    }

    @Test
    fun evaluateSevereWeatherWarning_detectsHeatwave() {
        val heatwave = OpenWeatherCurrentResponse(
            weather = listOf(OpenWeatherCondition(id = 800, main = "Clear", description = "clear sky")),
            main = OpenWeatherMain(temp = 41.2, feelsLike = 45.0, humidity = 45),
            wind = OpenWeatherWind(speed = 3.0)
        )

        val warning = BalasoreSevereWeatherAlertWorker.evaluateSevereWeatherWarning(heatwave)
        assertNotNull(warning)
        assertEquals("URGENT", warning?.alertLevel)
        assertTrue(warning!!.title.contains("Heatwave"))
    }

    @Test
    fun evaluateSevereWeatherWarning_returnsNullForCalmWeather() {
        val calm = OpenWeatherCurrentResponse(
            weather = listOf(OpenWeatherCondition(id = 801, main = "Clouds", description = "few clouds")),
            main = OpenWeatherMain(temp = 30.0, feelsLike = 32.5, humidity = 72),
            wind = OpenWeatherWind(speed = 4.5, gust = 6.0)
        )
        val calmForecast = listOf(
            OpenWeatherForecastItem(
                weather = listOf(OpenWeatherCondition(id = 802, main = "Clouds", description = "scattered clouds")),
                wind = OpenWeatherWind(speed = 5.0),
                dtTxt = "2026-11-26 15:00:00"
            )
        )

        val warning = BalasoreSevereWeatherAlertWorker.evaluateSevereWeatherWarning(calm, calmForecast)
        assertNull(warning)
    }

    @Test
    fun civicEyeCategoryNormalization_andStatusFormatting_workAcrossCanonicalAndLegacyValues() {
        assertEquals("Pothole", normalizeCivicEyeCategory("pothole"))
        assertEquals("Water Supply", normalizeCivicEyeCategory("Water Supply / Pipeline Leak"))
        assertEquals("Drainage", normalizeCivicEyeCategory("Drain Clog / Waterlogging"))
        assertEquals("Garbage", normalizeCivicEyeCategory("Garbage Heap / Waste"))
        assertEquals("Street Light", normalizeCivicEyeCategory("electricity"))
        assertEquals("Damaged Road", normalizeCivicEyeCategory("Damaged Culvert / Road"))

        assertEquals("Pending", CivicIssueNotificationHelper.formatCivicStatusLabel("SUBMITTED"))
        assertEquals("Pending", CivicIssueNotificationHelper.formatCivicStatusLabel("PENDING"))
        assertEquals("In Progress", CivicIssueNotificationHelper.formatCivicStatusLabel("IN_PROGRESS"))
        assertEquals("Resolved", CivicIssueNotificationHelper.formatCivicStatusLabel("RESOLVED"))
    }
}
