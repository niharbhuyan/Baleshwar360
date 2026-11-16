package com.example

import com.example.data.local.SevereWeatherAlertEntity
import com.example.data.model.AlertSeverity
import com.example.data.model.BalasoreWeatherAlert
import com.example.data.model.BalasoreZone
import com.example.data.model.WeatherAlertCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SevereWeatherAlertRoomTest {

    @Test
    fun `verify SevereWeatherAlertEntity creation and data mapping`() {
        val now = System.currentTimeMillis()
        val entity = SevereWeatherAlertEntity(
            id = "alert_test_101",
            title = "Kalbaisakhi Thunderstorm Warning",
            odiaTitle = "କାଳବୈଶାଖୀ ଝଡ଼ ସତର୍କତା",
            summary = "Gale-force gusts and convective clouds across Chandipur coastline.",
            severity = "WARNING",
            affectedZones = "Chandipur & Balaramgadi Coast, Balasore Municipality & Remuna",
            issuingAuthority = "IMD Meteorological Centre Bhubaneswar",
            validFromMillis = now,
            validUntilMillis = now + 14400000L,
            receivedAtMillis = now
        )

        assertEquals("alert_test_101", entity.id)
        assertEquals("Kalbaisakhi Thunderstorm Warning", entity.title)
        assertEquals("କାଳବୈଶାଖୀ ଝଡ଼ ସତର୍କତା", entity.odiaTitle)
        assertEquals("WARNING", entity.severity)
        assertTrue(entity.affectedZones.contains("Chandipur"))
        assertEquals("IMD Meteorological Centre Bhubaneswar", entity.issuingAuthority)
        assertTrue(entity.validUntilMillis > entity.validFromMillis)
    }

    @Test
    fun `verify BalasoreWeatherAlert conversion for Room Database storage`() {
        val domainAlert = BalasoreWeatherAlert(
            id = "cyclone_dana_test",
            title = "Very Severe Cyclonic Storm Alert",
            odiaTitle = "ବାତ୍ୟା ସତର୍କତା",
            summary = "Cyclonic system moving toward Odisha coast with 90 km/h gusts.",
            detailedDescription = "Extensive evacuation planned for low-lying coastal areas.",
            category = WeatherAlertCategory.CYCLONE,
            severity = AlertSeverity.CYCLONE_ALERT,
            affectedZones = listOf(BalasoreZone.COASTAL_CHANDIPUR, BalasoreZone.TALASARI_SUBARNAREKHA),
            issuingAuthority = "IMD Bhubaneswar & INCOIS",
            validFromMillis = 1000L,
            validUntilMillis = 5000L,
            issuedAtMillis = 1000L
        )

        val entity = SevereWeatherAlertEntity(
            id = domainAlert.id,
            title = domainAlert.title,
            odiaTitle = domainAlert.odiaTitle,
            summary = domainAlert.summary,
            severity = domainAlert.severity.name,
            affectedZones = domainAlert.affectedZones.joinToString(", ") { it.zoneName },
            issuingAuthority = domainAlert.issuingAuthority,
            validFromMillis = domainAlert.validFromMillis,
            validUntilMillis = domainAlert.validUntilMillis,
            receivedAtMillis = domainAlert.issuedAtMillis
        )

        assertEquals("CYCLONE_ALERT", entity.severity)
        assertTrue(entity.affectedZones.contains("Chandipur & Balaramgadi Coast"))
        assertTrue(entity.affectedZones.contains("Talasari & Subarnarekha Estuary"))
        assertEquals("IMD Bhubaneswar & INCOIS", entity.issuingAuthority)
    }

    @Test
    fun `verify last 5 alerts slice limit constraint`() {
        val now = System.currentTimeMillis()
        val entities = (1..10).map { i ->
            SevereWeatherAlertEntity(
                id = "alert_$i",
                title = "Alert $i",
                odiaTitle = "ସତର୍କତା $i",
                summary = "Summary $i",
                severity = if (i % 2 == 0) "WARNING" else "ADVISORY",
                affectedZones = "Balasore Zone $i",
                issuingAuthority = "IMD",
                validFromMillis = now - (i * 10000L),
                validUntilMillis = now + (i * 10000L),
                receivedAtMillis = now - (i * 10000L)
            )
        }

        val last5 = entities.take(5)
        assertEquals(5, last5.size)
        assertEquals("alert_1", last5.first().id)
        assertEquals("alert_5", last5.last().id)
    }
}
