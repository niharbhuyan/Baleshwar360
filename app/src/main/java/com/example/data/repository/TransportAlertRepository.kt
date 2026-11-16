package com.example.data.repository

import android.content.Context
import com.example.data.fcm.BalasoreNotificationHelper
import com.example.data.local.AppDatabase
import com.example.data.local.BusScheduleEntity
import com.example.data.local.TrainScheduleEntity
import com.example.data.local.TransportAlertEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Repository coordinating Room database caching and real-time FCM delay notifications
 * for Balasore Junction trains and Sahadevkhunta Central Bus Stand schedules.
 */
class TransportAlertRepository(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val trainDao = db.trainScheduleDao()
    private val busDao = db.busScheduleDao()
    private val alertDao = db.transportAlertDao()

    val trainSchedules: Flow<List<TrainScheduleEntity>> = trainDao.getAllTrainSchedules()
    val busSchedules: Flow<List<BusScheduleEntity>> = busDao.getAllSchedules()
    val transportAlerts: Flow<List<TransportAlertEntity>> = alertDao.getAllAlerts()
    val activeDelayAlerts: Flow<List<TransportAlertEntity>> = alertDao.getActiveDelayAlerts()

    /**
     * Seeds initial authentic Balasore railway and bus schedules into Room database if empty.
     */
    suspend fun seedInitialTransportDataIfEmpty() = withContext(Dispatchers.IO) {
        if (trainDao.getCount() == 0) {
            val trains = listOf(
                TrainScheduleEntity(
                    trainNumber = "20835",
                    trainNameEn = "Vande Bharat Express",
                    trainNameOr = "ବନ୍ଦେ ଭାରତ ଏକ୍ସପ୍ରେସ୍",
                    trainType = "Vande Bharat Express",
                    origin = "Howrah (HWH)",
                    destination = "Puri (PURI)",
                    scheduledArrival = "08:52 AM",
                    estimatedArrival = "08:52 AM",
                    scheduledDeparture = "08:54 AM",
                    estimatedDeparture = "08:54 AM",
                    delayMinutes = 0,
                    platform = "PF 2",
                    status = "ON_TIME",
                    currentLocation = "Cruising past Jaleswar",
                    currentLocationOr = "ଜଳେଶ୍ୱର ଅତିକ୍ରମ କରିଛି",
                    speedKmh = 120
                ),
                TrainScheduleEntity(
                    trainNumber = "12821",
                    trainNameEn = "Dhauli Express",
                    trainNameOr = "ଧଉଳି ଏକ୍ସପ୍ରେସ୍",
                    trainType = "Superfast Express",
                    origin = "Howrah (HWH)",
                    destination = "Puri (PURI)",
                    scheduledArrival = "12:40 PM",
                    estimatedArrival = "01:05 PM",
                    scheduledDeparture = "12:45 PM",
                    estimatedDeparture = "01:10 PM",
                    delayMinutes = 25,
                    platform = "PF 1",
                    status = "DELAYED",
                    currentLocation = "Belda - Kharagpur Section",
                    currentLocationOr = "ବେଲଦା - ଖଡଗପୁର ସେକ୍ସନ",
                    speedKmh = 68
                ),
                TrainScheduleEntity(
                    trainNumber = "12841",
                    trainNameEn = "Coromandel Express",
                    trainNameOr = "କରମଣ୍ଡଳ ଏକ୍ସପ୍ରେସ୍",
                    trainType = "Superfast Express",
                    origin = "Howrah (HWH)",
                    destination = "Chennai Central (MAS)",
                    scheduledArrival = "06:25 PM",
                    estimatedArrival = "06:25 PM",
                    scheduledDeparture = "06:30 PM",
                    estimatedDeparture = "06:30 PM",
                    delayMinutes = 0,
                    platform = "PF 2",
                    status = "ON_TIME",
                    currentLocation = "Approaching Dantan",
                    currentLocationOr = "ଦାନ୍ତନ ନିକଟବର୍ତ୍ତୀ",
                    speedKmh = 105
                ),
                TrainScheduleEntity(
                    trainNumber = "12073",
                    trainNameEn = "Jan Shatabdi Express",
                    trainNameOr = "ଜନ ଶତାବ୍ଦୀ ଏକ୍ସପ୍ରେସ୍",
                    trainType = "Jan Shatabdi Express",
                    origin = "Howrah (HWH)",
                    destination = "Bhubaneswar (BBS)",
                    scheduledArrival = "04:48 PM",
                    estimatedArrival = "05:03 PM",
                    scheduledDeparture = "04:50 PM",
                    estimatedDeparture = "05:05 PM",
                    delayMinutes = 15,
                    platform = "PF 3",
                    status = "DELAYED",
                    currentLocation = "Slowed down at Basta outer",
                    currentLocationOr = "ବସ୍ତା ଆଉଟରରେ ଧୀର ଗତି",
                    speedKmh = 45
                ),
                TrainScheduleEntity(
                    trainNumber = "18045",
                    trainNameEn = "East Coast Express",
                    trainNameOr = "ଇଷ୍ଟ କୋଷ୍ଟ ଏକ୍ସପ୍ରେସ୍",
                    trainType = "Mail / Express",
                    origin = "Howrah (HWH)",
                    destination = "Hyderabad (HYB)",
                    scheduledArrival = "03:30 PM",
                    estimatedArrival = "04:10 PM",
                    scheduledDeparture = "03:35 PM",
                    estimatedDeparture = "04:15 PM",
                    delayMinutes = 40,
                    platform = "PF 1",
                    status = "DELAYED",
                    currentLocation = "Kharagpur Yard Hold",
                    currentLocationOr = "ଖଡଗପୁର ୟାର୍ଡରେ ଅଟକ",
                    speedKmh = 30
                ),
                TrainScheduleEntity(
                    trainNumber = "68001",
                    trainNameEn = "Kharagpur - Balasore MEMU",
                    trainNameOr = "ଖଡଗପୁର - ବାଲେଶ୍ୱର ମେମୁ",
                    trainType = "MEMU Local Passenger",
                    origin = "Kharagpur (KGP)",
                    destination = "Balasore (BLS)",
                    scheduledArrival = "09:15 AM",
                    estimatedArrival = "09:15 AM",
                    scheduledDeparture = "Terminates",
                    estimatedDeparture = "Terminates",
                    delayMinutes = 0,
                    platform = "PF 4",
                    status = "ON_TIME",
                    currentLocation = "Approaching Rupsa Junction",
                    currentLocationOr = "ରୂପ୍ସା ଜଙ୍କସନ ନିକଟବର୍ତ୍ତୀ",
                    speedKmh = 55
                )
            )
            trainDao.insertTrainSchedules(trains)
        }

        if (busDao.getScheduleCount() == 0) {
            val buses = listOf(
                BusScheduleEntity(
                    busNumber = "OD-01-A-4421",
                    serviceName = "OSRTC Platinum AC Sleeper",
                    busStandName = "Sahadevkhunta Central Bus Stand",
                    destination = "Bhubaneswar Baramunda",
                    destinationOdia = "ଭୁବନେଶ୍ୱର ବାରମୁଣ୍ଡା",
                    departureTime = "06:30 AM",
                    departureMinutesOfDay = 390,
                    arrivalTime = "10:15 AM",
                    estimatedDuration = "3h 45m",
                    busType = "AC Sleeper",
                    bayPlatform = "Bay 2",
                    fareInr = 380,
                    viaRoute = "Bhadrak, Panikoili, Chandikhole, Cuttack",
                    status = "ON_TIME"
                ),
                BusScheduleEntity(
                    busNumber = "OD-01-B-8920",
                    serviceName = "Mo Bus CRUT Coastal Line",
                    busStandName = "Sahadevkhunta Central Bus Stand",
                    destination = "Chandipur Beach",
                    destinationOdia = "ଚାନ୍ଦିପୁର ବେଳାଭୂମି",
                    departureTime = "08:15 AM",
                    departureMinutesOfDay = 495,
                    arrivalTime = "08:50 AM",
                    estimatedDuration = "35m",
                    busType = "Electric Low Floor",
                    bayPlatform = "Bay 7",
                    fareInr = 25,
                    viaRoute = "OT Road, ITI Chhak, Defence Colony",
                    status = "ON_TIME"
                ),
                BusScheduleEntity(
                    busNumber = "OD-01-C-1102",
                    serviceName = "OSRTC Hi-Tech Deluxe",
                    busStandName = "Sahadevkhunta Central Bus Stand",
                    destination = "Kolkata (Babughat)",
                    destinationOdia = "କୋଲକାତା (ବାବୁଘାଟ)",
                    departureTime = "11:00 AM",
                    departureMinutesOfDay = 660,
                    arrivalTime = "04:30 PM",
                    estimatedDuration = "5h 30m",
                    busType = "Deluxe Non-AC",
                    bayPlatform = "Bay 3",
                    fareInr = 340,
                    viaRoute = "Jaleswar, Dantan, Kharagpur, Kolaghat",
                    status = "DELAYED"
                ),
                BusScheduleEntity(
                    busNumber = "OD-01-D-5561",
                    serviceName = "Nilagiri Express",
                    busStandName = "Old Bus Stand Balasore",
                    destination = "Nilagiri & Panchalingeswar",
                    destinationOdia = "ନୀଳଗିରି ଓ ପଞ୍ଚଲିଙ୍ଗେଶ୍ୱର",
                    departureTime = "09:30 AM",
                    departureMinutesOfDay = 570,
                    arrivalTime = "10:30 AM",
                    estimatedDuration = "1h 00m",
                    busType = "Local Semi-Deluxe",
                    bayPlatform = "Bay 1",
                    fareInr = 45,
                    viaRoute = "Remuna Golei, Sergarh Chhak",
                    status = "ON_TIME"
                )
            )
            busDao.insertSchedules(buses)
        }

        if (alertDao.getCount() == 0) {
            val alerts = listOf(
                TransportAlertEntity(
                    alertId = "alert_train_12821",
                    transportType = "TRAIN",
                    identifier = "12821",
                    serviceName = "12821 Dhauli Express",
                    delayMinutes = 25,
                    scheduledTime = "12:40 PM",
                    estimatedTime = "01:05 PM",
                    platformOrBay = "PF 1",
                    reason = "Signal clearance and interlocking maintenance in Kharagpur division",
                    reasonOdia = "ଖଡଗପୁର ଡିଭିଜନରେ ସିଗ୍ନାଲ ରକ୍ଷଣାବେକ୍ଷଣ କାର୍ଯ୍ୟ ଯୋଗୁଁ ବିଳମ୍ବ",
                    severity = "WARNING"
                ),
                TransportAlertEntity(
                    alertId = "alert_train_12073",
                    transportType = "TRAIN",
                    identifier = "12073",
                    serviceName = "12073 Jan Shatabdi Express",
                    delayMinutes = 15,
                    scheduledTime = "04:48 PM",
                    estimatedTime = "05:03 PM",
                    platformOrBay = "PF 3",
                    reason = "Speed restriction near Basta due to track renewal",
                    reasonOdia = "ବସ୍ତା ନିକଟରେ ଟ୍ରାକ କାର୍ଯ୍ୟ ଯୋଗୁଁ ଗତି ନିୟନ୍ତ୍ରଣ",
                    severity = "INFO"
                ),
                TransportAlertEntity(
                    alertId = "alert_bus_1102",
                    transportType = "BUS",
                    identifier = "OD-01-C-1102",
                    serviceName = "OSRTC Deluxe Kolkata",
                    delayMinutes = 30,
                    scheduledTime = "11:00 AM",
                    estimatedTime = "11:30 AM",
                    platformOrBay = "Bay 3",
                    reason = "NH-16 toll plaza heavy congestion and bridge repair at Jaleswar",
                    reasonOdia = "ଜଳେଶ୍ୱର ନିକଟରେ NH-16 ବ୍ରିଜ୍ ମରାମତି ଓ ଟ୍ରାଫିକ୍ ଜାମ୍",
                    severity = "WARNING"
                )
            )
            alertDao.insertAlerts(alerts)
        }
    }

    /**
     * Records a new delay alert (from FCM push or local trigger) and updates the schedule table.
     */
    suspend fun recordDelayAlert(
        transportType: String,
        identifier: String,
        serviceName: String,
        delayMinutes: Int,
        scheduledTime: String,
        estimatedTime: String,
        platformOrBay: String,
        reason: String,
        reasonOdia: String = "",
        fcmMessageId: String? = null
    ) = withContext(Dispatchers.IO) {
        val alert = TransportAlertEntity(
            alertId = "alert_${System.currentTimeMillis()}_${identifier.hashCode()}",
            transportType = transportType,
            identifier = identifier,
            serviceName = serviceName,
            delayMinutes = delayMinutes,
            scheduledTime = scheduledTime,
            estimatedTime = estimatedTime,
            platformOrBay = platformOrBay,
            reason = reason,
            reasonOdia = reasonOdia,
            severity = if (delayMinutes >= 30) "SEVERE" else "WARNING",
            timestamp = System.currentTimeMillis(),
            fcmMessageId = fcmMessageId
        )
        alertDao.insertAlert(alert)

        // Update train schedule if train
        if (transportType.equals("TRAIN", ignoreCase = true)) {
            trainDao.updateDelay(
                trainNumber = identifier,
                delayMinutes = delayMinutes,
                estimatedArrival = estimatedTime,
                platform = platformOrBay,
                status = if (delayMinutes > 0) "DELAYED" else "ON_TIME",
                lastUpdatedMillis = System.currentTimeMillis()
            )
        }
    }

    /**
     * Simulates an incoming FCM delay push notification for testing and demonstration.
     * Updates the Room database and triggers an Android system notification.
     */
    suspend fun simulateFcmDelayPush(
        transportType: String = "TRAIN",
        identifier: String = "12821",
        serviceName: String = "12821 Dhauli Express",
        delayMinutes: Int = 35,
        platformOrBay: String = "PF 1",
        reason: String = "Signal upgrade and train regulation in Kharagpur-Balasore trunk line"
    ) = withContext(Dispatchers.IO) {
        val scheduled = "12:40 PM"
        val estimated = "01:15 PM"

        recordDelayAlert(
            transportType = transportType,
            identifier = identifier,
            serviceName = serviceName,
            delayMinutes = delayMinutes,
            scheduledTime = scheduled,
            estimatedTime = estimated,
            platformOrBay = platformOrBay,
            reason = reason,
            reasonOdia = "ଖଡଗପୁର-ବାଲେଶ୍ୱର ମୁଖ୍ୟ ଲାଇନରେ ସିଗ୍ନାଲ କାର୍ଯ୍ୟ ଯୋଗୁଁ ବିଳମ୍ବ",
            fcmMessageId = "fcm_sim_${UUID.randomUUID()}"
        )

        // Post system notification
        BalasoreNotificationHelper.showTransportDelayNotification(
            context = context,
            title = "$serviceName Delay Alert (+${delayMinutes}m)",
            message = "$serviceName is running ${delayMinutes}m late. Expected at Balasore $platformOrBay at $estimated ($reason).",
            transportNumber = identifier,
            delayMinutes = delayMinutes,
            platformOrBay = platformOrBay,
            transportType = transportType
        )
    }

    suspend fun acknowledgeAlert(alertId: String) = withContext(Dispatchers.IO) {
        alertDao.acknowledgeAlert(alertId)
    }

    suspend fun toggleTrackedTrain(trainNumber: String, isTracked: Boolean) = withContext(Dispatchers.IO) {
        trainDao.updateTrackedStatus(trainNumber, isTracked)
    }
}
