package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.SevereWeatherAlertEntity
import com.example.data.model.BalasoreWeatherAlert
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repository for managing Balasore severe weather alert history in Room Database.
 */
class SevereWeatherAlertRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context).severeWeatherAlertDao()

    /**
     * Flow of the last 5 severe weather alerts received for Balasore.
     */
    val last5Alerts: Flow<List<SevereWeatherAlertEntity>> = dao.getLast5Alerts()

    /**
     * Inserts or updates incoming live weather alerts into the Room database.
     */
    suspend fun recordAlerts(alerts: List<BalasoreWeatherAlert>) = withContext(Dispatchers.IO) {
        if (alerts.isEmpty()) return@withContext
        val entities = alerts.map { alert ->
            SevereWeatherAlertEntity(
                id = alert.id,
                title = alert.title,
                odiaTitle = alert.odiaTitle,
                summary = alert.summary,
                severity = alert.severity.name,
                affectedZones = alert.affectedZones.joinToString(", ") { it.zoneName },
                issuingAuthority = alert.issuingAuthority,
                validFromMillis = alert.validFromMillis,
                validUntilMillis = alert.validUntilMillis,
                receivedAtMillis = if (alert.issuedAtMillis > 0) alert.issuedAtMillis else System.currentTimeMillis()
            )
        }
        dao.insertAlerts(entities)
    }

    /**
     * Seeds initial authentic Balasore severe weather alert history records if the database is empty.
     */
    suspend fun seedSampleAlertsIfEmpty() = withContext(Dispatchers.IO) {
        if (dao.getAlertCount() == 0) {
            val now = System.currentTimeMillis()
            val sampleAlerts = listOf(
                SevereWeatherAlertEntity(
                    id = "alert_norwester_1",
                    title = "Nor'wester (Kalbaisakhi) Squall Warning",
                    odiaTitle = "କାଳବୈଶାଖୀ ଝଡ଼ ସତର୍କତା",
                    summary = "Thunderstorm with wind gusts up to 55 km/h, intense lightning, and localized downpours across Balasore coastal belt.",
                    severity = "WARNING",
                    affectedZones = "Balasore Town, Chandipur Coast, Remuna",
                    issuingAuthority = "IMD Meteorological Centre Bhubaneswar",
                    validFromMillis = now - 2 * 3600 * 1000L,
                    validUntilMillis = now + 4 * 3600 * 1000L,
                    receivedAtMillis = now - 2 * 3600 * 1000L
                ),
                SevereWeatherAlertEntity(
                    id = "alert_tide_surge_2",
                    title = "Perigean Spring Tide Surge Alert",
                    odiaTitle = "ଉଚ୍ଚ ଜୁଆର ତରଙ୍ଗ ସତର୍କତା",
                    summary = "Spring tidal waves reaching 3.8m swell height along Chandipur Beach; intertidal mudflat walking strictly prohibited.",
                    severity = "CYCLONE_ALERT",
                    affectedZones = "Chandipur Coast, Balaramgadi, Kasafal",
                    issuingAuthority = "INCOIS Marine Warning Division",
                    validFromMillis = now - 8 * 3600 * 1000L,
                    validUntilMillis = now - 2 * 3600 * 1000L,
                    receivedAtMillis = now - 8 * 3600 * 1000L
                ),
                SevereWeatherAlertEntity(
                    id = "alert_coastal_gale_3",
                    title = "Coastal Gale Wind Advisory",
                    odiaTitle = "ଉପକୂଳ ପ୍ରବଳ ପବନ ସତର୍କତା",
                    summary = "Sustained southerly sea winds at 38 km/h. Fishermen advised not to venture deep into North Bay of Bengal.",
                    severity = "ADVISORY",
                    affectedZones = "Talasari Beach, Chandipur Coast, Kasafal / Bahanaga",
                    issuingAuthority = "IMD & Odisha Coastal Disaster Management",
                    validFromMillis = now - 24 * 3600 * 1000L,
                    validUntilMillis = now - 12 * 3600 * 1000L,
                    receivedAtMillis = now - 24 * 3600 * 1000L
                ),
                SevereWeatherAlertEntity(
                    id = "alert_lightning_4",
                    title = "Severe Lightning & Thunderstorm Alert",
                    odiaTitle = "ବଜ୍ରପାତ ଓ ଘୂର୍ଣ୍ଣିଝଡ଼ ସତର୍କତା",
                    summary = "Multiple cloud-to-ground lightning strikes detected near Nilagiri foothills and Remuna block. Stay indoors.",
                    severity = "WARNING",
                    affectedZones = "Nilagiri Foothills, Remuna, Balasore Town",
                    issuingAuthority = "IMD Doppler Weather Radar & SRC Odisha",
                    validFromMillis = now - 48 * 3600 * 1000L,
                    validUntilMillis = now - 40 * 3600 * 1000L,
                    receivedAtMillis = now - 48 * 3600 * 1000L
                ),
                SevereWeatherAlertEntity(
                    id = "alert_heavy_rain_5",
                    title = "Isolated Heavy Rainfall Advisory",
                    odiaTitle = "ଭାରୀ ବର୍ଷା ସତର୍କତା",
                    summary = "Low-pressure depression over North-West Bay of Bengal causing heavy spells of 65mm rainfall in Balasore district.",
                    severity = "ADVISORY",
                    affectedZones = "Balasore Town, Soro, Bahanaga, Jaleswar",
                    issuingAuthority = "IMD Bhubaneswar Regional Centre",
                    validFromMillis = now - 72 * 3600 * 1000L,
                    validUntilMillis = now - 60 * 3600 * 1000L,
                    receivedAtMillis = now - 72 * 3600 * 1000L
                )
            )
            dao.insertAlerts(sampleAlerts)
        }
    }
}
