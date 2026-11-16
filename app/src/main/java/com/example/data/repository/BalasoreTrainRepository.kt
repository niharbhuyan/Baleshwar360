package com.example.data.repository

import com.example.data.model.TrainMovement
import com.example.data.model.TrainRunningStatus
import com.example.data.model.TrainStatusCategory
import com.example.data.model.TrainType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Repository providing real-time train running status for Balasore Railway Station (BLS).
 * South Eastern Railway mainline corridor connecting Howrah, Bhubaneswar, Puri, and Chennai.
 */
class BalasoreTrainRepository {

    private val _trainStatuses = MutableStateFlow<List<TrainRunningStatus>>(INITIAL_TRAIN_DATASET)
    val trainStatuses: Flow<List<TrainRunningStatus>> = _trainStatuses.asStateFlow()

    private var simulationTick = 0

    /**
     * Simulates a live API fetch over network with 350ms delay, returning updated telemetry.
     */
    suspend fun refreshLiveStatuses(): List<TrainRunningStatus> {
        delay(350)
        simulationTick++
        val updated = _trainStatuses.value.map { train ->
            simulateTelemetryUpdate(train, simulationTick)
        }
        _trainStatuses.value = updated
        return updated
    }

    fun getTrainByNumber(number: String): TrainRunningStatus? {
        return _trainStatuses.value.find { it.trainNumber == number }
    }

    private fun simulateTelemetryUpdate(train: TrainRunningStatus, tick: Int): TrainRunningStatus {
        val distanceDelta = (tick % 3) * 1.5
        val newDistance = (train.distanceToBalasoreKm - distanceDelta).coerceAtLeast(0.0)

        val updatedCategory = when {
            newDistance <= 0.5 && train.statusCategory != TrainStatusCategory.DEPARTED -> TrainStatusCategory.AT_STATION
            newDistance <= 12.0 && train.statusCategory != TrainStatusCategory.DEPARTED -> TrainStatusCategory.APPROACHING
            train.delayMinutes > 5 -> TrainStatusCategory.DELAYED
            else -> train.statusCategory
        }

        val updatedSpeed = when (updatedCategory) {
            TrainStatusCategory.AT_STATION -> 0
            TrainStatusCategory.APPROACHING -> (train.speedKmh - 10).coerceAtLeast(25)
            TrainStatusCategory.DEPARTED -> 65
            else -> train.speedKmh
        }

        val updatedLocation = when (updatedCategory) {
            TrainStatusCategory.AT_STATION -> "Halted at Balasore ${train.platform}"
            TrainStatusCategory.APPROACHING -> "Approaching Balasore Outer (${String.format(Locale.US, "%.1f", newDistance)} km away)"
            TrainStatusCategory.DEPARTED -> "Departed Balasore • Next: ${train.nextStationName}"
            else -> train.currentLocation
        }

        val updatedLocationOr = when (updatedCategory) {
            TrainStatusCategory.AT_STATION -> "ବାଲେଶ୍ୱର ${train.platform} ରେ ଉପସ୍ଥିତ"
            TrainStatusCategory.APPROACHING -> "ବାଲେଶ୍ୱର ନିକଟବର୍ତ୍ତୀ (${String.format(Locale.US, "%.1f", newDistance)} କି.ମି. ଦୂର)"
            TrainStatusCategory.DEPARTED -> "ବାଲେଶ୍ୱରରୁ ପ୍ରସ୍ଥାନ କରିସାରିଛି"
            else -> train.currentLocationOr
        }

        return train.copy(
            distanceToBalasoreKm = newDistance,
            speedKmh = updatedSpeed,
            statusCategory = updatedCategory,
            currentLocation = updatedLocation,
            currentLocationOr = updatedLocationOr
        )
    }

    companion object {
        val INITIAL_TRAIN_DATASET = listOf(
            TrainRunningStatus(
                trainNumber = "20831",
                trainNameEn = "Howrah - Puri Vande Bharat Express",
                trainNameOr = "ହାୱଡ଼ା - ପୁରୀ ବନ୍ଦେ ଭାରତ ଏକ୍ସପ୍ରେସ୍",
                type = TrainType.VANDE_BHARAT,
                originCode = "HWH",
                originNameEn = "Howrah Jn",
                originNameOr = "ହାୱଡ଼ା",
                destinationCode = "PURI",
                destinationNameEn = "Puri Terminus",
                destinationNameOr = "ପୁରୀ",
                scheduledArrival = "09:05 AM",
                estimatedArrival = "09:05 AM",
                scheduledDeparture = "09:07 AM",
                estimatedDeparture = "09:07 AM",
                delayMinutes = 0,
                platform = "Platform 2",
                isPlatformChanged = false,
                currentLocation = "Approaching Haldipada (6.2 km to Balasore)",
                currentLocationOr = "ହଳଦୀପଦା ନିକଟବର୍ତ୍ତୀ (୬.୨ କି.ମି. ବାଲେଶ୍ୱରକୁ)",
                speedKmh = 112,
                distanceToBalasoreKm = 6.2,
                statusCategory = TrainStatusCategory.APPROACHING,
                nextStationName = "Bhadrak (BHC) - ETA 09:42 AM",
                coachComposition = "Engine • C1-C4 • Executive E1-E2 • C5-C8",
                runningDays = "Daily (Except Thursday)",
                haltsBalasoreMins = 2
            ),
            TrainRunningStatus(
                trainNumber = "12821",
                trainNameEn = "Shalimar - Puri Dhauli Superfast Express",
                trainNameOr = "ଶାଲିମାର - ପୁରୀ ଧଉଳି ସୁପରଫାଷ୍ଟ",
                type = TrainType.SUPERFAST,
                originCode = "SHM",
                originNameEn = "Shalimar",
                originNameOr = "ଶାଲିମାର",
                destinationCode = "PURI",
                destinationNameEn = "Puri Terminus",
                destinationNameOr = "ପୁରୀ",
                scheduledArrival = "11:15 AM",
                estimatedArrival = "11:24 AM",
                scheduledDeparture = "11:18 AM",
                estimatedDeparture = "11:27 AM",
                delayMinutes = 9,
                platform = "Platform 1",
                isPlatformChanged = false,
                currentLocation = "Passed Rupsa Junction (14.5 km to Balasore)",
                currentLocationOr = "ରୂପ୍ସା ଜଙ୍କସନ ଅତିକ୍ରମ କରିଛି (୧୪.୫ କି.ମି.)",
                speedKmh = 84,
                distanceToBalasoreKm = 14.5,
                statusCategory = TrainStatusCategory.DELAYED,
                nextStationName = "Soro (SORO) - ETA 11:58 AM",
                coachComposition = "SL • D1-D5 (Chair Car) • CC1-CC3 • Gen",
                runningDays = "Daily",
                haltsBalasoreMins = 3
            ),
            TrainRunningStatus(
                trainNumber = "12841",
                trainNameEn = "Howrah - MGR Chennai Coromandel Express",
                trainNameOr = "ହାୱଡ଼ା - ଚେନ୍ନାଇ କରମଣ୍ଡଳ ଏକ୍ସପ୍ରେସ୍",
                type = TrainType.SUPERFAST,
                originCode = "HWH",
                originNameEn = "Howrah Jn",
                originNameOr = "ହାୱଡ଼ା",
                destinationCode = "MAS",
                destinationNameEn = "MGR Chennai Central",
                destinationNameOr = "ଚେନ୍ନାଇ ସେଣ୍ଟ୍ରାଲ",
                scheduledArrival = "05:10 PM",
                estimatedArrival = "05:10 PM",
                scheduledDeparture = "05:15 PM",
                estimatedDeparture = "05:15 PM",
                delayMinutes = 0,
                platform = "Platform 3",
                isPlatformChanged = false,
                currentLocation = "Between Basta and Rupsa (28.0 km away)",
                currentLocationOr = "ବସ୍ତା ଓ ରୂପ୍ସା ମଧ୍ୟରେ (୨୮ କି.ମି.)",
                speedKmh = 118,
                distanceToBalasoreKm = 28.0,
                statusCategory = TrainStatusCategory.ON_TIME,
                nextStationName = "Bhadrak (BHC) - ETA 05:50 PM",
                coachComposition = "B1-B5 • A1-A2 • H1 • S1-S8 • Gen",
                runningDays = "Daily",
                haltsBalasoreMins = 5
            ),
            TrainRunningStatus(
                trainNumber = "20832",
                trainNameEn = "Puri - Howrah Vande Bharat Express",
                trainNameOr = "ପୁରୀ - ହାୱଡ଼ା ବନ୍ଦେ ଭାରତ ଏକ୍ସପ୍ରେସ୍",
                type = TrainType.VANDE_BHARAT,
                originCode = "PURI",
                originNameEn = "Puri Terminus",
                originNameOr = "ପୁରୀ",
                destinationCode = "HWH",
                destinationNameEn = "Howrah Jn",
                destinationNameOr = "ହାୱଡ଼ା",
                scheduledArrival = "06:22 PM",
                estimatedArrival = "06:22 PM",
                scheduledDeparture = "06:24 PM",
                estimatedDeparture = "06:24 PM",
                delayMinutes = 0,
                platform = "Platform 1",
                isPlatformChanged = false,
                currentLocation = "Departed Bhadrak • Speeding towards Balasore",
                currentLocationOr = "ଭଦ୍ରକରୁ ପ୍ରସ୍ଥାନ • ବାଲେଶ୍ୱର ଅଭିମୁଖେ",
                speedKmh = 120,
                distanceToBalasoreKm = 36.0,
                statusCategory = TrainStatusCategory.ON_TIME,
                nextStationName = "Kharagpur (KGP) - ETA 07:45 PM",
                coachComposition = "C1-C4 • Executive E1-E2 • C5-C8",
                runningDays = "Daily (Except Thursday)",
                haltsBalasoreMins = 2
            ),
            TrainRunningStatus(
                trainNumber = "12822",
                trainNameEn = "Puri - Shalimar Dhauli Superfast Express",
                trainNameOr = "ପୁରୀ - ଶାଲିମାର ଧଉଳି ସୁପରଫାଷ୍ଟ",
                type = TrainType.SUPERFAST,
                originCode = "PURI",
                originNameEn = "Puri Terminus",
                originNameOr = "ପୁରୀ",
                destinationCode = "SHM",
                destinationNameEn = "Shalimar",
                destinationNameOr = "ଶାଲିମାର",
                scheduledArrival = "03:15 PM",
                estimatedArrival = "03:15 PM",
                scheduledDeparture = "03:18 PM",
                estimatedDeparture = "03:18 PM",
                delayMinutes = 0,
                platform = "Platform 1",
                isPlatformChanged = false,
                currentLocation = "Halted at Soro (32 km from Balasore)",
                currentLocationOr = "ସୋରୋ ଷ୍ଟେସନରେ ରହଣି (୩୨ କି.ମି.)",
                speedKmh = 0,
                distanceToBalasoreKm = 32.0,
                statusCategory = TrainStatusCategory.ON_TIME,
                nextStationName = "Jaleswar (JER) - ETA 03:58 PM",
                coachComposition = "CC1-CC3 • D1-D5 • General",
                runningDays = "Daily",
                haltsBalasoreMins = 3
            ),
            TrainRunningStatus(
                trainNumber = "12842",
                trainNameEn = "MGR Chennai - Howrah Coromandel Express",
                trainNameOr = "ଚେନ୍ନାଇ - ହାୱଡ଼ା କରମଣ୍ଡଳ ଏକ୍ସପ୍ରେସ୍",
                type = TrainType.SUPERFAST,
                originCode = "MAS",
                originNameEn = "MGR Chennai Central",
                originNameOr = "ଚେନ୍ନାଇ ସେଣ୍ଟ୍ରାଲ",
                destinationCode = "HWH",
                destinationNameEn = "Howrah Jn",
                destinationNameOr = "ହାୱଡ଼ା",
                scheduledArrival = "08:48 AM",
                estimatedArrival = "09:02 AM",
                scheduledDeparture = "08:52 AM",
                estimatedDeparture = "09:06 AM",
                delayMinutes = 14,
                platform = "Platform 1",
                isPlatformChanged = true, // Was PF 2, now PF 1
                currentLocation = "Bahanaga Bazar section (18 km from Balasore)",
                currentLocationOr = "ବାହାନଗା ବଜାର ସେକ୍ସନ (୧୮ କି.ମି.)",
                speedKmh = 95,
                distanceToBalasoreKm = 18.0,
                statusCategory = TrainStatusCategory.DELAYED,
                nextStationName = "Kharagpur (KGP) - ETA 10:25 AM",
                coachComposition = "S1-S8 • B1-B5 • A1-A2 • H1",
                runningDays = "Daily",
                haltsBalasoreMins = 4
            ),
            TrainRunningStatus(
                trainNumber = "12802",
                trainNameEn = "New Delhi - Puri Purushottam Express",
                trainNameOr = "ନୂଆଦିଲ୍ଲୀ - ପୁରୀ ପୁରୁଷୋତ୍ତମ ଏକ୍ସପ୍ରେସ୍",
                type = TrainType.SUPERFAST,
                originCode = "NDLS",
                originNameEn = "New Delhi",
                originNameOr = "ନୂଆଦିଲ୍ଲୀ",
                destinationCode = "PURI",
                destinationNameEn = "Puri Terminus",
                destinationNameOr = "ପୁରୀ",
                scheduledArrival = "01:45 PM",
                estimatedArrival = "01:45 PM",
                scheduledDeparture = "01:50 PM",
                estimatedDeparture = "01:50 PM",
                delayMinutes = 0,
                platform = "Platform 2",
                isPlatformChanged = false,
                currentLocation = "Passed Jaleswar • Cruising at 105 km/h",
                currentLocationOr = "ଜଳେଶ୍ୱର ଅତିକ୍ରମ • ଗତି ୧୦୫ କି.ମି./ଘଣ୍ଟା",
                speedKmh = 105,
                distanceToBalasoreKm = 48.0,
                statusCategory = TrainStatusCategory.ON_TIME,
                nextStationName = "Bhadrak (BHC) - ETA 02:35 PM",
                coachComposition = "H1 • A1-A3 • B1-B6 • S1-S7 • Gen",
                runningDays = "Daily",
                haltsBalasoreMins = 5
            ),
            TrainRunningStatus(
                trainNumber = "12073",
                trainNameEn = "Howrah - Bhubaneswar Jan Shatabdi Express",
                trainNameOr = "ହାୱଡ଼ା - ଭୁବନେଶ୍ୱର ଜନ ଶତାବ୍ଦୀ",
                type = TrainType.JAN_SHATABDI,
                originCode = "HWH",
                originNameEn = "Howrah Jn",
                originNameOr = "ହାୱଡ଼ା",
                destinationCode = "BBS",
                destinationNameEn = "Bhubaneswar",
                destinationNameOr = "ଭୁବନେଶ୍ୱର",
                scheduledArrival = "04:32 PM",
                estimatedArrival = "04:32 PM",
                scheduledDeparture = "04:34 PM",
                estimatedDeparture = "04:34 PM",
                delayMinutes = 0,
                platform = "Platform 2",
                isPlatformChanged = false,
                currentLocation = "Passed Amarda Road • 38 km to Balasore",
                currentLocationOr = "ଅମର୍ଦ୍ଦା ରୋଡ଼୍ ଅତିକ୍ରମ • ୩୮ କି.ମି. ବାଲେଶ୍ୱରକୁ",
                speedKmh = 110,
                distanceToBalasoreKm = 38.0,
                statusCategory = TrainStatusCategory.ON_TIME,
                nextStationName = "Bhadrak (BHC) - ETA 05:18 PM",
                coachComposition = "AC Chair Car CC1-CC3 • Second Sitting D1-D10",
                runningDays = "Daily (Except Sunday)",
                haltsBalasoreMins = 2
            ),
            TrainRunningStatus(
                trainNumber = "12703",
                trainNameEn = "Howrah - Secunderabad Falaknuma Express",
                trainNameOr = "ହାୱଡ଼ା - ସିକନ୍ଦରାବାଦ ଫଲକନୁମା",
                type = TrainType.SUPERFAST,
                originCode = "HWH",
                originNameEn = "Howrah Jn",
                originNameOr = "ହାୱଡ଼ା",
                destinationCode = "SC",
                destinationNameEn = "Secunderabad",
                destinationNameOr = "ସିକନ୍ଦରାବାଦ",
                scheduledArrival = "10:50 AM",
                estimatedArrival = "11:05 AM",
                scheduledDeparture = "10:55 AM",
                estimatedDeparture = "11:10 AM",
                delayMinutes = 15,
                platform = "Platform 3",
                isPlatformChanged = false,
                currentLocation = "Kharagpur division slow zone (42 km away)",
                currentLocationOr = "ଖଡ଼ଗପୁର ଧିମା ଗତି ଜୋନ୍ (୪୨ କି.ମି.)",
                speedKmh = 68,
                distanceToBalasoreKm = 42.0,
                statusCategory = TrainStatusCategory.DELAYED,
                nextStationName = "Bhadrak (BHC) - ETA 11:48 AM",
                coachComposition = "A1 • B1-B5 • S1-S10 • Pantry • Gen",
                runningDays = "Daily",
                haltsBalasoreMins = 5
            ),
            TrainRunningStatus(
                trainNumber = "18045",
                trainNameEn = "Howrah - Hyderabad East Coast Express",
                trainNameOr = "ହାୱଡ଼ା - ହାଇଦ୍ରାବାଦ ଇଷ୍ଟ କୋଷ୍ଟ ଏକ୍ସପ୍ରେସ୍",
                type = TrainType.EXPRESS_MAIL,
                originCode = "HWH",
                originNameEn = "Howrah Jn",
                originNameOr = "ହାୱଡ଼ା",
                destinationCode = "HYB",
                destinationNameEn = "Hyderabad Deccan",
                destinationNameOr = "ହାଇଦ୍ରାବାଦ",
                scheduledArrival = "02:20 PM",
                estimatedArrival = "02:20 PM",
                scheduledDeparture = "02:25 PM",
                estimatedDeparture = "02:25 PM",
                delayMinutes = 0,
                platform = "Platform 3",
                isPlatformChanged = false,
                currentLocation = "Between Belda and Jaleswar (64 km away)",
                currentLocationOr = "ବେଲଦା ଓ ଜଳେଶ୍ୱର ମଧ୍ୟରେ (୬୪ କି.ମି.)",
                speedKmh = 92,
                distanceToBalasoreKm = 64.0,
                statusCategory = TrainStatusCategory.ON_TIME,
                nextStationName = "Bhadrak (BHC) - ETA 03:10 PM",
                coachComposition = "3A • 2A • SL • Gen",
                runningDays = "Daily",
                haltsBalasoreMins = 5
            ),
            TrainRunningStatus(
                trainNumber = "18478",
                trainNameEn = "Yog Nagari Rishikesh - Puri Kalinga Utkal",
                trainNameOr = "ଋଷିକେଶ - ପୁରୀ କଳିଙ୍ଗ ଉତ୍କଳ ଏକ୍ସପ୍ରେସ୍",
                type = TrainType.EXPRESS_MAIL,
                originCode = "YNRK",
                originNameEn = "Yog Nagari Rishikesh",
                originNameOr = "ଋଷିକେଶ",
                destinationCode = "PURI",
                destinationNameEn = "Puri Terminus",
                destinationNameOr = "ପୁରୀ",
                scheduledArrival = "01:25 AM",
                estimatedArrival = "01:25 AM",
                scheduledDeparture = "01:30 AM",
                estimatedDeparture = "01:30 AM",
                delayMinutes = 0,
                platform = "Platform 2",
                isPlatformChanged = false,
                currentLocation = "Approaching Balasore Outer • Speed 45 km/h",
                currentLocationOr = "ବାଲେଶ୍ୱର ଆଉଟର ନିକଟବର୍ତ୍ତୀ • ଗତି ୪୫",
                speedKmh = 45,
                distanceToBalasoreKm = 3.2,
                statusCategory = TrainStatusCategory.APPROACHING,
                nextStationName = "Soro (SORO) - ETA 02:05 AM",
                coachComposition = "1A • 2A • 3A • SL • Gen",
                runningDays = "Daily",
                haltsBalasoreMins = 5
            ),
            TrainRunningStatus(
                trainNumber = "08063",
                trainNameEn = "Kharagpur - Bhadrak MEMU Passenger",
                trainNameOr = "ଖଡ଼ଗପୁର - ଭଦ୍ରକ ମେମୁ ପାସେଞ୍ଜର",
                type = TrainType.MEMU_PASSENGER,
                originCode = "KGP",
                originNameEn = "Kharagpur Jn",
                originNameOr = "ଖଡ଼ଗପୁର",
                destinationCode = "BHC",
                destinationNameEn = "Bhadrak",
                destinationNameOr = "ଭଦ୍ରକ",
                scheduledArrival = "07:50 AM",
                estimatedArrival = "07:50 AM",
                scheduledDeparture = "07:52 AM",
                estimatedDeparture = "07:52 AM",
                delayMinutes = 0,
                platform = "Platform 2",
                isPlatformChanged = false,
                currentLocation = "At Balasore Station Platform 2",
                currentLocationOr = "ବାଲେଶ୍ୱର ଷ୍ଟେସନ ପ୍ଲାଟଫର୍ମ ୨ ରେ ଉପସ୍ଥିତ",
                speedKmh = 0,
                distanceToBalasoreKm = 0.0,
                statusCategory = TrainStatusCategory.AT_STATION,
                nextStationName = "Nilagiri Road (NGRD) - ETA 08:08 AM",
                coachComposition = "12-Coach High-Capacity EMU Rake",
                runningDays = "Daily",
                haltsBalasoreMins = 2
            ),
            TrainRunningStatus(
                trainNumber = "08061",
                trainNameEn = "Howrah - Balasore MEMU Express Special",
                trainNameOr = "ହାୱଡ଼ା - ବାଲେଶ୍ୱର ମେମୁ ସ୍ପେଶାଲ",
                type = TrainType.MEMU_PASSENGER,
                originCode = "HWH",
                originNameEn = "Howrah Jn",
                originNameOr = "ହାୱଡ଼ା",
                destinationCode = "BLS",
                destinationNameEn = "Balasore (Terminus)",
                destinationNameOr = "ବାଲେଶ୍ୱର (ଶେଷ ଷ୍ଟେସନ)",
                scheduledArrival = "12:30 PM",
                estimatedArrival = "12:30 PM",
                scheduledDeparture = "--",
                estimatedDeparture = "--",
                delayMinutes = 0,
                platform = "Platform 4",
                isPlatformChanged = false,
                currentLocation = "Approaching Balasore Platform 4 (Bay Line)",
                currentLocationOr = "ବାଲେଶ୍ୱର ପ୍ଲାଟଫର୍ମ ୪ ନିକଟବର୍ତ୍ତୀ",
                speedKmh = 25,
                distanceToBalasoreKm = 1.1,
                statusCategory = TrainStatusCategory.APPROACHING,
                nextStationName = "Terminating at Balasore (BLS)",
                coachComposition = "12-Car MEMU Rake",
                runningDays = "Daily",
                haltsBalasoreMins = 0,
                isTerminatingAtBalasore = true
            ),
            TrainRunningStatus(
                trainNumber = "12839",
                trainNameEn = "Howrah - MGR Chennai Central Mail",
                trainNameOr = "ହାୱଡ଼ା - ଚେନ୍ନାଇ ସେଣ୍ଟ୍ରାଲ ମେଲ୍",
                type = TrainType.SUPERFAST,
                originCode = "HWH",
                originNameEn = "Howrah Jn",
                originNameOr = "ହାୱଡ଼ା",
                destinationCode = "MAS",
                destinationNameEn = "MGR Chennai Central",
                destinationNameOr = "ଚେନ୍ନାଇ ସେଣ୍ଟ୍ରାଲ",
                scheduledArrival = "03:40 AM",
                estimatedArrival = "03:40 AM",
                scheduledDeparture = "03:45 AM",
                estimatedDeparture = "03:45 AM",
                delayMinutes = 0,
                platform = "Platform 3",
                isPlatformChanged = false,
                currentLocation = "Passed Basta Station • Speeding at 98 km/h",
                currentLocationOr = "ବସ୍ତା ଷ୍ଟେସନ ଅତିକ୍ରମ • ଗତି ୯୮ କି.ମି./ଘଣ୍ଟା",
                speedKmh = 98,
                distanceToBalasoreKm = 21.0,
                statusCategory = TrainStatusCategory.ON_TIME,
                nextStationName = "Bhadrak (BHC) - ETA 04:30 AM",
                coachComposition = "H1 • A1 • B1-B4 • S1-S11 • Gen",
                runningDays = "Daily",
                haltsBalasoreMins = 5
            )
        )
    }
}
