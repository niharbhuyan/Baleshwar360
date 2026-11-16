package com.example.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsBusFilled
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FlightLand
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import org.json.JSONObject
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppDatabase
import com.example.data.local.BusScheduleDao
import com.example.data.local.BusScheduleEntity
import com.example.data.model.AppLanguage
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.OceanBlue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Data model for Live Bus Tracker Arrival & Departure status across Balasore regions.
 */
data class BusArrivalItem(
    val busId: String,
    val routeNumber: String,
    val routeNameEn: String,
    val routeNameOd: String,
    val origin: String,
    val destination: String,
    val arrivalStatusEn: String,
    val arrivalStatusOd: String,
    val arrivalMinutes: Int,
    val currentStop: String,
    val nextStop: String,
    val platformBay: String,
    val busType: String, // AC Electric, Standard CNG, Rural Express
    val fare: String,
    val operator: String,
    val occupancyStatus: String, // SEATS_AVAILABLE, MODERATE, CROWDED
    val speedKmh: Int,
    val departureTime: String = "08:00 AM",
    val scheduledArrivalTime: String = "08:45 AM",
    val nextDepartureInMinutes: Int = 15,
    val statusTag: String = "ON_TIME", // ON_TIME, APPROACHING, BOARDING, DELAYED
    val routeProgressFraction: Float = 0.62f,
    val connectingRegions: List<String> = emptyList(),
    val stopsSequence: List<String> = emptyList()
)

/**
 * Response envelope returned by [BalasoreLocalBusApiService].
 */
data class BalasoreBusApiTelemetryResponse(
    val endpointName: String,
    val fetchedAtMillis: Long,
    val totalActiveFleet: Int,
    val onTimePercentage: Int,
    val routes: List<BusArrivalItem>
)

/**
 * Simulated Real-Time Public Transport API Service for Balasore (CRUT Mo Bus & OSRTC Regional Feed).
 * Generates live arrival and departure times connecting key Balasore regions.
 */
object BalasoreLocalBusApiService {

    private const val ENDPOINT_URL = "https://api.baleshwar360.transit.gov.in/v2/live-departures"
    const val STATIC_JSON_ASSET_FILE = "balasore_local_bus_routes.json"

    /**
     * Parses a static JSON string (matching `assets/balasore_local_bus_routes.json`) into [BusArrivalItem] models.
     */
    fun parseRoutesFromJsonString(jsonString: String): List<BusArrivalItem> {
        return try {
            val root = JSONObject(jsonString)
            val routesArray = root.optJSONArray("routes") ?: return SAMPLE_BALASORE_BUS_ARRIVALS
            val parsed = mutableListOf<BusArrivalItem>()
            for (i in 0 until routesArray.length()) {
                val obj = routesArray.getJSONObject(i)
                val regionsJson = obj.optJSONArray("connectingRegions")
                val regions = buildList {
                    if (regionsJson != null) {
                        for (j in 0 until regionsJson.length()) {
                            add(regionsJson.getString(j))
                        }
                    }
                }
                val stopsJson = obj.optJSONArray("stopsSequence")
                val stops = buildList {
                    if (stopsJson != null) {
                        for (j in 0 until stopsJson.length()) {
                            add(stopsJson.getString(j))
                        }
                    }
                }
                parsed.add(
                    BusArrivalItem(
                        busId = obj.optString("busId", "BUS_OD01_${100 + i}"),
                        routeNumber = obj.optString("routeNumber", "Route ${100 + i}"),
                        routeNameEn = obj.optString("routeNameEn", ""),
                        routeNameOd = obj.optString("routeNameOd", ""),
                        origin = obj.optString("origin", "Sahadevkhunta Bus Stand"),
                        destination = obj.optString("destination", "Balasore"),
                        arrivalStatusEn = obj.optString("arrivalStatusEn", "Arriving soon"),
                        arrivalStatusOd = obj.optString("arrivalStatusOd", "ଶୀଘ୍ର ପହଞ୍ଚିବ"),
                        arrivalMinutes = obj.optInt("arrivalMinutes", 5),
                        currentStop = obj.optString("currentStop", ""),
                        nextStop = obj.optString("nextStop", ""),
                        platformBay = obj.optString("platformBay", "Bay 1"),
                        busType = obj.optString("busType", "AC Electric (Mo Bus)"),
                        fare = obj.optString("fare", "₹15"),
                        operator = obj.optString("operator", "CRUT Mo Bus Balasore"),
                        occupancyStatus = obj.optString("occupancyStatus", "SEATS_AVAILABLE"),
                        speedKmh = obj.optInt("speedKmh", 32),
                        departureTime = obj.optString("departureTime", "08:00 AM"),
                        scheduledArrivalTime = obj.optString("scheduledArrivalTime", "08:40 AM"),
                        nextDepartureInMinutes = obj.optInt("nextDepartureInMinutes", 15),
                        statusTag = obj.optString("statusTag", "ON_TIME"),
                        routeProgressFraction = obj.optDouble("routeProgressFraction", 0.65).toFloat(),
                        connectingRegions = regions,
                        stopsSequence = stops
                    )
                )
            }
            if (parsed.isNotEmpty()) parsed else SAMPLE_BALASORE_BUS_ARRIVALS
        } catch (_: Throwable) {
            SAMPLE_BALASORE_BUS_ARRIVALS
        }
    }

    /**
     * Loads the major Balasore bus routes from the bundled `assets/balasore_local_bus_routes.json` static file,
     * falling back gracefully to [SAMPLE_BALASORE_BUS_ARRIVALS] if unavailable.
     */
    fun loadRoutesFromStaticJsonAsset(context: Context?): List<BusArrivalItem> {
        if (context == null) return SAMPLE_BALASORE_BUS_ARRIVALS
        return try {
            val jsonText = context.assets.open(STATIC_JSON_ASSET_FILE)
                .bufferedReader()
                .use { it.readText() }
            parseRoutesFromJsonString(jsonText)
        } catch (_: Throwable) {
            SAMPLE_BALASORE_BUS_ARRIVALS
        }
    }

    /**
     * Computes the estimated live arrival time in minutes at a specific Balasore boarding stop along [bus].
     */
    fun computeArrivalMinutesForStop(bus: BusArrivalItem, stopName: String): Int {
        if (stopName.isBlank() || stopName.equals("ALL", ignoreCase = true)) {
            return bus.arrivalMinutes
        }
        val matchingIdx = bus.stopsSequence.indexOfFirst {
            it.contains(stopName, ignoreCase = true)
        }
        return if (matchingIdx >= 0) {
            (bus.arrivalMinutes + (matchingIdx * 2)).coerceAtLeast(1)
        } else if (bus.connectingRegions.any { it.contains(stopName, ignoreCase = true) }) {
            (bus.arrivalMinutes + 3).coerceAtLeast(1)
        } else {
            bus.arrivalMinutes
        }
    }

    /**
     * Computes dynamic clock-relative departure and arrival strings based on current device time
     * so arrival/departure times always reflect realistic live regional schedules.
     */
    fun computeDynamicTimeWindow(offsetMinutesFromNow: Int, journeyDurationMinutes: Int = 35): Pair<String, String> {
        val formatter = SimpleDateFormat("hh:mm a", Locale.US)
        val depCal = Calendar.getInstance().apply {
            add(Calendar.MINUTE, offsetMinutesFromNow - (journeyDurationMinutes / 2))
        }
        val arrCal = Calendar.getInstance().apply {
            add(Calendar.MINUTE, offsetMinutesFromNow)
        }
        return formatter.format(depCal.time) to formatter.format(arrCal.time)
    }

    /**
     * Fetches live bus telemetry by combining the static JSON route dataset (`assets/balasore_local_bus_routes.json`)
     * with the real-time mock API simulation engine.
     */
    suspend fun fetchLiveBusTelemetryFromAssetOrMockApi(
        context: Context?,
        tickCycle: Int = 0,
        simulateNetworkDelayMs: Long = 280L
    ): BalasoreBusApiTelemetryResponse = withContext(Dispatchers.Default) {
        val baseSeed = loadRoutesFromStaticJsonAsset(context)
        fetchLiveBusTelemetryWithSeed(
            seedRoutes = baseSeed,
            tickCycle = tickCycle,
            simulateNetworkDelayMs = simulateNetworkDelayMs
        )
    }

    /**
     * Simulated asynchronous API call fetching live bus arrival/departure telemetry across Balasore.
     */
    suspend fun fetchLiveBusTelemetry(
        tickCycle: Int = 0,
        simulateNetworkDelayMs: Long = 280L
    ): BalasoreBusApiTelemetryResponse = fetchLiveBusTelemetryWithSeed(
        seedRoutes = SAMPLE_BALASORE_BUS_ARRIVALS,
        tickCycle = tickCycle,
        simulateNetworkDelayMs = simulateNetworkDelayMs
    )

    private suspend fun fetchLiveBusTelemetryWithSeed(
        seedRoutes: List<BusArrivalItem>,
        tickCycle: Int = 0,
        simulateNetworkDelayMs: Long = 280L
    ): BalasoreBusApiTelemetryResponse = withContext(Dispatchers.Default) {
        if (simulateNetworkDelayMs > 0) {
            delay(simulateNetworkDelayMs)
        }

        val updatedRoutes = seedRoutes.mapIndexed { index, base ->
            val delta = (tickCycle + index) % 5
            val computedEta = when {
                tickCycle == 0 -> base.arrivalMinutes
                base.arrivalMinutes - (tickCycle % 4) <= 1 -> base.arrivalMinutes + 6
                else -> (base.arrivalMinutes - (tickCycle % 3)).coerceAtLeast(1)
            }
            val (depTime, arrTime) = computeDynamicTimeWindow(
                offsetMinutesFromNow = computedEta,
                journeyDurationMinutes = 25 + (index * 6)
            )
            val liveSpeed = if (base.statusTag == "BOARDING" && tickCycle % 2 == 0) {
                0
            } else {
                (base.speedKmh + (if (delta % 2 == 0) 2 else -2)).coerceIn(18, 58)
            }
            val liveStatusTag = when {
                computedEta <= 3 -> "APPROACHING"
                base.statusTag == "DELAYED" -> "DELAYED"
                base.speedKmh == 0 && tickCycle == 0 -> "BOARDING"
                else -> "ON_TIME"
            }
            val statusEn = when (liveStatusTag) {
                "APPROACHING" -> "Arriving in $computedEta mins (Approaching Stop)"
                "BOARDING" -> "Boarding at Stand (Departs in $computedEta mins)"
                "DELAYED" -> "Delayed • Arriving in $computedEta mins"
                else -> "Arriving in $computedEta mins (Live GPS)"
            }
            val statusOd = when (liveStatusTag) {
                "APPROACHING" -> "$computedEta ମିନିଟରେ ଷ୍ଟପ୍ ନିକଟରେ ପହଞ୍ଚୁଛି"
                "BOARDING" -> "$computedEta ମିନିଟରେ ବସ୍ ଷ୍ଟାଣ୍ଡରୁ ଛାଡ଼ିବ"
                "DELAYED" -> "ବିଳମ୍ବ • $computedEta ମିନିଟରେ ପହଞ୍ଚିବ"
                else -> "$computedEta ମିନିଟରେ ପହଞ୍ଚିବ (ଲାଇଭ୍ GPS)"
            }
            val progress = ((1f - (computedEta.toFloat() / 26f)).coerceIn(0.18f, 0.94f))

            base.copy(
                arrivalMinutes = computedEta,
                arrivalStatusEn = statusEn,
                arrivalStatusOd = statusOd,
                departureTime = if (tickCycle == 0) base.departureTime else depTime,
                scheduledArrivalTime = if (tickCycle == 0) base.scheduledArrivalTime else arrTime,
                nextDepartureInMinutes = (computedEta + 12),
                speedKmh = liveSpeed,
                statusTag = liveStatusTag,
                routeProgressFraction = progress
            )
        }

        val onTimeCount = updatedRoutes.count { it.statusTag != "DELAYED" }
        val onTimePct = ((onTimeCount.toFloat() / updatedRoutes.size.coerceAtLeast(1)) * 100).toInt()

        BalasoreBusApiTelemetryResponse(
            endpointName = ENDPOINT_URL,
            fetchedAtMillis = System.currentTimeMillis(),
            totalActiveFleet = updatedRoutes.size,
            onTimePercentage = onTimePct,
            routes = updatedRoutes
        )
    }

    /**
     * Persists the latest simulated API bus schedules into Room [BusScheduleDao] for offline viewing.
     */
    suspend fun syncTelemetryToRoom(
        busScheduleDao: BusScheduleDao,
        routes: List<BusArrivalItem>
    ) = withContext(Dispatchers.IO) {
        val nowMillis = System.currentTimeMillis()
        val entities = routes.mapIndexed { idx, item ->
            BusScheduleEntity(
                id = (1000L + idx),
                busNumber = item.routeNumber,
                serviceName = "${item.operator} (${item.routeNameEn})",
                busStandName = item.origin,
                destination = item.destination,
                destinationOdia = item.routeNameOd,
                departureTime = item.departureTime,
                departureMinutesOfDay = 480 + (idx * 35),
                arrivalTime = item.scheduledArrivalTime,
                estimatedDuration = "${item.arrivalMinutes + 20} mins",
                busType = item.busType,
                bayPlatform = item.platformBay,
                fareInr = item.fare.filter { it.isDigit() }.toIntOrNull() ?: 20,
                viaRoute = "${item.currentStop} → ${item.nextStop}",
                status = item.statusTag,
                frequency = "Every 15-20 mins",
                isLiveActive = true,
                lastUpdated = nowMillis
            )
        }
        busScheduleDao.insertSchedules(entities)
    }
}

/**
 * Pre-calibrated live routes connecting key Balasore regions.
 */
val SAMPLE_BALASORE_BUS_ARRIVALS = listOf(
    BusArrivalItem(
        busId = "BUS_OD01_100",
        routeNumber = "Route 100",
        routeNameEn = "Balasore Bus Stand (Sahadevkhunta) ↔ Remuna Gopinath Temple & Industrial Estate",
        routeNameOd = "ବାଲେଶ୍ୱର ବସ୍ ଷ୍ଟାଣ୍ଡ ↔ ରେମୁଣା ଗୋପୀନାଥ ମନ୍ଦିର ଓ ଶିଳ୍ପାଞ୍ଚଳ",
        origin = "Balasore Bus Stand",
        destination = "Remuna",
        arrivalStatusEn = "Arriving in 3 mins (Live GPS)",
        arrivalStatusOd = "୩ ମିନିଟରେ ପହଞ୍ଚିବ (ଲାଇଭ୍ GPS)",
        arrivalMinutes = 3,
        currentStop = "Fakir Mohan Golaye / Tamulia Bridge",
        nextStop = "Remuna Golei & Khirachora Temple",
        platformBay = "Bay 1 (Remuna Rapid Shuttle)",
        busType = "AC Electric (Mo Bus)",
        fare = "₹15",
        operator = "CRUT Mo Bus Balasore",
        occupancyStatus = "SEATS_AVAILABLE",
        speedKmh = 36,
        departureTime = "08:10 AM",
        scheduledArrivalTime = "08:35 AM",
        nextDepartureInMinutes = 15,
        statusTag = "APPROACHING",
        routeProgressFraction = 0.82f,
        connectingRegions = listOf("Sahadevkhunta", "Fakir Mohan Golaye", "Tamulia", "Remuna"),
        stopsSequence = listOf(
            "Sahadevkhunta Central Stand (Dep 08:10 AM)",
            "Fakir Mohan Golaye (08:18 AM)",
            "Tamulia Railway Overbridge (08:25 AM)",
            "Remuna Golei Industrial Hub (08:31 AM)",
            "Khirachora Gopinath Temple Gate (Arr 08:35 AM)"
        )
    ),
    BusArrivalItem(
        busId = "BUS_OD01_101",
        routeNumber = "Route 101",
        routeNameEn = "Sahadevkhunta Central Stand ↔ Chandipur Sea Beach",
        routeNameOd = "ସହଦେବଖୁଣ୍ଟା ↔ ଚାନ୍ଦିପୁର ବେଳାଭୂମି",
        origin = "Sahadevkhunta Bus Stand",
        destination = "Chandipur Beach",
        arrivalStatusEn = "Arriving in 4 mins",
        arrivalStatusOd = "୪ ମିନିଟରେ ପହଞ୍ଚିବ",
        arrivalMinutes = 4,
        currentStop = "OT Road / Police Line Chhak",
        nextStop = "Chandipur Panthanivas & Seawall",
        platformBay = "Bay 2 (Bay of Bengal Express)",
        busType = "AC Electric (Mo Bus)",
        fare = "₹20",
        operator = "CRUT Mo Bus Balasore",
        occupancyStatus = "SEATS_AVAILABLE",
        speedKmh = 38,
        departureTime = "08:05 AM",
        scheduledArrivalTime = "08:40 AM",
        nextDepartureInMinutes = 20,
        statusTag = "ON_TIME",
        routeProgressFraction = 0.76f,
        connectingRegions = listOf("Sahadevkhunta", "Police Line", "Sunhat", "Mirzapur", "Chandipur"),
        stopsSequence = listOf(
            "Sahadevkhunta Central Stand (Dep 08:05 AM)",
            "Police Line Chhak (08:14 AM)",
            "Nuniajodi Bridge / Sunhat (08:22 AM)",
            "Mirzapur Market (08:31 AM)",
            "Chandipur Panthanivas & Seawall (Arr 08:40 AM)"
        )
    ),
    BusArrivalItem(
        busId = "BUS_OD01_102",
        routeNumber = "Route 102",
        routeNameEn = "Balasore Railway Station ↔ Remuna Khirachora Gopinath Temple",
        routeNameOd = "ବାଲେଶ୍ୱର ଷ୍ଟେସନ ↔ ରେମୁଣା ଖିରଚୋରା ଗୋପୀନାଥ",
        origin = "Balasore Railway Station",
        destination = "Remuna Gopinath Temple",
        arrivalStatusEn = "Arriving in 9 mins",
        arrivalStatusOd = "୯ ମିନିଟରେ ପହଞ୍ଚିବ",
        arrivalMinutes = 9,
        currentStop = "Fakir Mohan Golaye",
        nextStop = "Remuna Block Chowk",
        platformBay = "Bay 3 (Heritage Shuttle)",
        busType = "Standard CNG",
        fare = "₹15",
        operator = "OSRTC Regional Feeder",
        occupancyStatus = "MODERATE",
        speedKmh = 32,
        departureTime = "08:15 AM",
        scheduledArrivalTime = "08:48 AM",
        nextDepartureInMinutes = 22,
        statusTag = "ON_TIME",
        routeProgressFraction = 0.58f,
        connectingRegions = listOf("Railway Station", "ITI Chhak", "Remuna"),
        stopsSequence = listOf(
            "Balasore Railway Station (Dep 08:15 AM)",
            "Fakir Mohan Golaye (08:24 AM)",
            "ITI Chhak (08:33 AM)",
            "Remuna Block Chowk (08:41 AM)",
            "Remuna Khirachora Temple (Arr 08:48 AM)"
        )
    ),
    BusArrivalItem(
        busId = "BUS_OD01_103",
        routeNumber = "Route 103",
        routeNameEn = "ITI Chhak ↔ Nilagiri Jagannath Temple & Kuldiha Sanctuary",
        routeNameOd = "ଆଇଟିଆଇ ଛକ ↔ ନୀଳଗିରି ଜଗନ୍ନାଥ ମନ୍ଦିର",
        origin = "ITI Chhak",
        destination = "Nilagiri",
        arrivalStatusEn = "Boarding at Stand (Departs in 6 mins)",
        arrivalStatusOd = "୬ ମିନିଟରେ ଛାଡ଼ିବ",
        arrivalMinutes = 6,
        currentStop = "Sahadevkhunta Terminal",
        nextStop = "Mitrapur Chhak",
        platformBay = "Bay 7",
        busType = "OSRTC Deluxe",
        fare = "₹35",
        operator = "OSRTC",
        occupancyStatus = "SEATS_AVAILABLE",
        speedKmh = 0,
        departureTime = "08:30 AM",
        scheduledArrivalTime = "09:15 AM",
        nextDepartureInMinutes = 6,
        statusTag = "BOARDING",
        routeProgressFraction = 0.22f,
        connectingRegions = listOf("ITI Chhak", "Kuruda", "Shergarh", "Mitrapur", "Nilagiri", "Panchalingeswar"),
        stopsSequence = listOf(
            "ITI Chhak / Sahadevkhunta (Dep 08:30 AM)",
            "Shergarh Highway Junction (08:45 AM)",
            "Mitrapur Market Chhak (08:58 AM)",
            "Nilagiri Rajbati & Jagannath Temple (Arr 09:15 AM)"
        )
    ),
    BusArrivalItem(
        busId = "BUS_OD01_104",
        routeNumber = "Route 104",
        routeNameEn = "Old Bus Stand (Town) ↔ Balaramgadi Fish Harbor & Estuary",
        routeNameOd = "ପୁରୁଣା ବସ୍ ଷ୍ଟାଣ୍ଡ ↔ ବଳରାମଗଡ଼ି ମାଛ ବନ୍ଦର",
        origin = "Old Bus Stand",
        destination = "Balaramgadi Estuary",
        arrivalStatusEn = "Arriving in 14 mins",
        arrivalStatusOd = "୧୪ ମିନିଟରେ ପହଞ୍ଚିବ",
        arrivalMinutes = 14,
        currentStop = "Sunhat Bypass",
        nextStop = "Budhabalanga River Estuary Jetty",
        platformBay = "Bay 4",
        busType = "Mini Feeder Bus",
        fare = "₹18",
        operator = "Balasore Private City Bus Assoc.",
        occupancyStatus = "CROWDED",
        speedKmh = 29,
        departureTime = "08:12 AM",
        scheduledArrivalTime = "08:52 AM",
        nextDepartureInMinutes = 30,
        statusTag = "ON_TIME",
        routeProgressFraction = 0.48f,
        connectingRegions = listOf("Old Bus Stand", "Motiganj", "Sunhat", "Balaramgadi"),
        stopsSequence = listOf(
            "Old Bus Stand / Motiganj (Dep 08:12 AM)",
            "Cinema Bazar (08:20 AM)",
            "Sunhat Bypass (08:32 AM)",
            "Balaramgadi Fishing Jetty (Arr 08:52 AM)"
        )
    ),
    BusArrivalItem(
        busId = "BUS_OD01_105",
        routeNumber = "Route 105",
        routeNameEn = "Balasore Central ↔ Soro NH-16 Express Corridor",
        routeNameOd = "ବାଲେଶ୍ୱର ↔ ସୋରୋ ଜାତୀୟ ରାଜପଥ",
        origin = "Sahadevkhunta Bus Stand",
        destination = "Soro",
        arrivalStatusEn = "Delayed 8 mins (Traffic at Gopalpur)",
        arrivalStatusOd = "୮ ମିନିଟ୍ ବିଳମ୍ବ",
        arrivalMinutes = 18,
        currentStop = "Khantapada Bazaar",
        nextStop = "Soro Bus Stand",
        platformBay = "Bay 9",
        busType = "Express High-Capacity",
        fare = "₹45",
        operator = "OSRTC",
        occupancyStatus = "MODERATE",
        speedKmh = 45,
        departureTime = "07:55 AM",
        scheduledArrivalTime = "08:55 AM",
        nextDepartureInMinutes = 25,
        statusTag = "DELAYED",
        routeProgressFraction = 0.65f,
        connectingRegions = listOf("Sahadevkhunta", "Kuruda", "Khantapada", "Bahanaga", "Soro"),
        stopsSequence = listOf(
            "Sahadevkhunta Stand (Dep 07:55 AM)",
            "Kuruda NH-16 Junction (08:08 AM)",
            "Khantapada Bazaar (08:25 AM)",
            "Bahanaga Bazar (08:40 AM)",
            "Soro Central Stand (Arr 08:55 AM)"
        )
    ),
    BusArrivalItem(
        busId = "BUS_OD01_106",
        routeNumber = "Route 106",
        routeNameEn = "Phandi Chhak ↔ Jaleswar / Subarnarekha Gateway",
        routeNameOd = "ଫାଣ୍ଡି ଛକ ↔ ଜଳେଶ୍ୱର ସୁବର୍ଣ୍ଣରେଖା",
        origin = "Phandi Chhak",
        destination = "Jaleswar",
        arrivalStatusEn = "Arriving in 22 mins",
        arrivalStatusOd = "୨୨ ମିନିଟରେ ପହଞ୍ଚିବ",
        arrivalMinutes = 22,
        currentStop = "Basta Station Crossing",
        nextStop = "Jaleswar Puruna Bazar",
        platformBay = "Bay 12",
        busType = "Standard Inter-district",
        fare = "₹55",
        operator = "OSRTC",
        occupancyStatus = "SEATS_AVAILABLE",
        speedKmh = 52,
        departureTime = "07:50 AM",
        scheduledArrivalTime = "09:05 AM",
        nextDepartureInMinutes = 35,
        statusTag = "ON_TIME",
        routeProgressFraction = 0.54f,
        connectingRegions = listOf("Phandi Chhak", "Haladipada", "Rupsa", "Basta", "Amarda Road", "Jaleswar"),
        stopsSequence = listOf(
            "Phandi Chhak Balasore (Dep 07:50 AM)",
            "Rupsa Junction Crossing (08:15 AM)",
            "Basta NH-60 Bypass (08:35 AM)",
            "Rajghat Subarnarekha Bridge (08:52 AM)",
            "Jaleswar Bus Stand (Arr 09:05 AM)"
        )
    ),
    BusArrivalItem(
        busId = "BUS_OD01_107",
        routeNumber = "Route 107",
        routeNameEn = "FM Autonomous College ↔ AIIMS Satellite Centre & FM University",
        routeNameOd = "ଏଫ୍.ଏମ୍. କଲେଜ ↔ ଏମ୍ସ ସାଟେଲାଇଟ୍ ହସ୍ପିଟାଲ ଓ ବିଶ୍ୱବିଦ୍ୟାଳୟ",
        origin = "FM College",
        destination = "AIIMS Satellite Centre",
        arrivalStatusEn = "Arriving in 2 mins (Approaching stop)",
        arrivalStatusOd = "୨ ମିନିଟରେ ପହଞ୍ଚିବ",
        arrivalMinutes = 2,
        currentStop = "Tamulia Gate Chhak",
        nextStop = "AIIMS Outpatient Wing",
        platformBay = "Bay 2 (Campus Ring)",
        busType = "Electric Low-Floor",
        fare = "₹10",
        operator = "Mo Bus Balasore",
        occupancyStatus = "SEATS_AVAILABLE",
        speedKmh = 25,
        departureTime = "08:20 AM",
        scheduledArrivalTime = "08:42 AM",
        nextDepartureInMinutes = 12,
        statusTag = "APPROACHING",
        routeProgressFraction = 0.89f,
        connectingRegions = listOf("FM College", "Station Square", "Tamulia", "Nuapadhi FMU", "AIIMS Balasore"),
        stopsSequence = listOf(
            "FM Autonomous College Gate (Dep 08:20 AM)",
            "Station Square (08:26 AM)",
            "Tamulia Gate Chhak (08:34 AM)",
            "AIIMS Satellite Centre & FMU (Arr 08:42 AM)"
        )
    )
)

/**
 * Autocomplete locations and destinations for quick lookup.
 */
val BALASORE_BUS_AUTOCOMPLETE_SUGGESTIONS = listOf(
    "Chandipur Beach",
    "Remuna Gopinath Temple",
    "Nilagiri Jagannath Temple",
    "Balaramgadi Estuary",
    "Soro",
    "Jaleswar",
    "AIIMS Satellite Centre",
    "Sahadevkhunta Bus Stand",
    "Balasore Railway Station",
    "FM College",
    "Old Bus Stand",
    "Kuruda Chhak",
    "ITI Chhak",
    "Basta",
    "Khantapada"
)

/**
 * Interactive 'LocalBusTracker' UI Component for the City Essentials & Transit tabs featuring:
 * 1. Simulated Real-Time API Service ([BalasoreLocalBusApiService]) with live arrival & departure times.
 * 2. Autocomplete Search Bar to filter routes by destination, origin, or connecting Balasore region.
 * 3. Arrival / Departure schedule mode switcher & regional corridor filter chips.
 * 4. Expandable stop-by-stop route timeline & automatic Room DB schedule synchronization.
 */
@Composable
fun LocalBusTracker(
    language: AppLanguage = AppLanguage.ENGLISH,
    modifier: Modifier = Modifier,
    onRouteSelected: (BusArrivalItem) -> Unit = {}
) {
    val context = LocalContext.current
    val isOdia = language == AppLanguage.ODIA || LocalAppLanguage.current.isOdia
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val busScheduleDao = remember { AppDatabase.getInstance(context).busScheduleDao() }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var selectedScheduleTab by remember { mutableStateOf("ALL") } // ALL, ARRIVALS, DEPARTURES
    var selectedBoardingStop by remember { mutableStateOf("ALL") }
    var alertSubscribedBusIds by remember { mutableStateOf(setOf<String>()) }
    var isSearchFocused by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var showAllRoutes by remember { mutableStateOf(true) }
    var expandedBusId by remember { mutableStateOf<String?>(null) }

    var apiTickCycle by remember { mutableIntStateOf(0) }
    var lastApiSyncMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var onTimeFleetPct by remember { mutableIntStateOf(92) }
    var arrivalList by remember {
        mutableStateOf(BalasoreLocalBusApiService.loadRoutesFromStaticJsonAsset(context))
    }

    val isBatterySaverActive = remember { com.example.data.sync.SyncManager.isBatterySavingModeEnabled(context) }
    val syncIntervalSec = remember(isBatterySaverActive) {
        com.example.data.sync.SyncManager.getEffectiveBusSyncIntervalSeconds(context, 15)
    }
    var secondsUntilRefresh by remember(syncIntervalSec) { mutableIntStateOf(syncIntervalSec) }

    // Initial & periodic simulated API + static JSON fetch + Room persistence
    LaunchedEffect(syncIntervalSec) {
        try {
            val initialResponse = BalasoreLocalBusApiService.fetchLiveBusTelemetryFromAssetOrMockApi(
                context = context,
                tickCycle = 0,
                simulateNetworkDelayMs = 0L
            )
            arrivalList = initialResponse.routes
            onTimeFleetPct = initialResponse.onTimePercentage
            lastApiSyncMillis = initialResponse.fetchedAtMillis
            BalasoreLocalBusApiService.syncTelemetryToRoom(busScheduleDao, initialResponse.routes)
        } catch (_: Throwable) {}

        secondsUntilRefresh = syncIntervalSec
        while (true) {
            delay(1000L)
            if (secondsUntilRefresh > 1) {
                secondsUntilRefresh -= 1
            } else {
                secondsUntilRefresh = syncIntervalSec
                apiTickCycle += 1
                val response = BalasoreLocalBusApiService.fetchLiveBusTelemetryFromAssetOrMockApi(
                    context = context,
                    tickCycle = apiTickCycle,
                    simulateNetworkDelayMs = 120L
                )
                arrivalList = response.routes
                onTimeFleetPct = response.onTimePercentage
                lastApiSyncMillis = response.fetchedAtMillis
                try {
                    BalasoreLocalBusApiService.syncTelemetryToRoom(busScheduleDao, response.routes)
                } catch (_: Throwable) {}
            }
        }
    }

    // Refresh rotation animation
    val infiniteTransition = rememberInfiniteTransition(label = "busRefreshSpin")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )

    // Autocomplete Suggestions computation
    val filteredSuggestions by remember(searchQuery) {
        derivedStateOf {
            if (searchQuery.trim().length >= 2) {
                BALASORE_BUS_AUTOCOMPLETE_SUGGESTIONS.filter {
                    it.contains(searchQuery.trim(), ignoreCase = true)
                }.take(5)
            } else {
                emptyList()
            }
        }
    }

    // Filtered bus list based on search, category, boarding stop, and arrival/departure mode
    val filteredBuses by remember(searchQuery, selectedCategoryFilter, selectedScheduleTab, selectedBoardingStop, arrivalList) {
        derivedStateOf {
            val q = searchQuery.trim().lowercase(Locale.ROOT)
            arrivalList.filter { bus ->
                val matchesQuery = if (q.isBlank()) {
                    true
                } else {
                    bus.destination.lowercase(Locale.ROOT).contains(q) ||
                        bus.origin.lowercase(Locale.ROOT).contains(q) ||
                        bus.routeNumber.lowercase(Locale.ROOT).contains(q) ||
                        bus.routeNameEn.lowercase(Locale.ROOT).contains(q) ||
                        bus.currentStop.lowercase(Locale.ROOT).contains(q) ||
                        bus.nextStop.lowercase(Locale.ROOT).contains(q) ||
                        bus.connectingRegions.any { it.lowercase(Locale.ROOT).contains(q) }
                }

                val matchesCategory = when (selectedCategoryFilter) {
                    "CHANDIPUR" -> bus.destination.contains("Chandipur", ignoreCase = true) ||
                        bus.connectingRegions.any { it.contains("Chandipur", ignoreCase = true) || it.contains("Balaramgadi", ignoreCase = true) }
                    "REMUNA" -> bus.destination.contains("Remuna", ignoreCase = true) ||
                        bus.connectingRegions.any { it.contains("Remuna", ignoreCase = true) }
                    "NILAGIRI" -> bus.destination.contains("Nilagiri", ignoreCase = true)
                    "INTERCITY" -> bus.routeNumber in listOf("Route 105", "Route 106")
                    "CAMPUS" -> bus.routeNumber == "Route 107" ||
                        bus.destination.contains("AIIMS", ignoreCase = true)
                    else -> true
                }

                val matchesBoardingStop = if (selectedBoardingStop == "ALL") {
                    true
                } else {
                    bus.origin.contains(selectedBoardingStop, ignoreCase = true) ||
                        bus.destination.contains(selectedBoardingStop, ignoreCase = true) ||
                        bus.currentStop.contains(selectedBoardingStop, ignoreCase = true) ||
                        bus.nextStop.contains(selectedBoardingStop, ignoreCase = true) ||
                        bus.stopsSequence.any { it.contains(selectedBoardingStop, ignoreCase = true) } ||
                        bus.connectingRegions.any { it.contains(selectedBoardingStop, ignoreCase = true) }
                }

                val matchesScheduleTab = when (selectedScheduleTab) {
                    "ARRIVALS" -> bus.arrivalMinutes <= 10 || bus.statusTag == "APPROACHING"
                    "DEPARTURES" -> bus.statusTag == "BOARDING" || bus.nextDepartureInMinutes <= 25
                    else -> true
                }

                matchesQuery && matchesCategory && matchesBoardingStop && matchesScheduleTab
            }
        }
    }

    val formattedSyncTime = remember(lastApiSyncMillis) {
        SimpleDateFormat("hh:mm:ss a", Locale.US).format(Date(lastApiSyncMillis))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("local_bus_tracker"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.2.dp, OceanBlue.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Header Row: Title + Simulated API Status + Refresh Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = OceanBlue.copy(alpha = 0.15f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.DirectionsBus,
                                contentDescription = "Bus Tracker Icon",
                                tint = OceanBlue,
                                modifier = Modifier.size(23.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isOdia) "ଲାଇଭ୍ ବାଲେଶ୍ୱର ବସ୍ ଟ୍ରାକର" else "Local Bus Tracker",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 16.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = EmeraldGreen.copy(alpha = 0.14f),
                                border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "LIVE API + JSON",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EmeraldGreen,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = if (isOdia) {
                                "ବାଲେଶ୍ୱର ପ୍ରମୁଖ ଅଞ୍ଚଳ ସଂଯୋଗକାରୀ ବସ୍ ଆଗମନ ଓ ପ୍ରସ୍ଥାନ ସମୟ"
                            } else {
                                "Real-time arrival & departure times for major routes in Balasore"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Refresh Button calling Simulated API Service + Static Asset Loader
                FilledTonalIconButton(
                    onClick = {
                        if (!isRefreshing) {
                            isRefreshing = true
                            coroutineScope.launch {
                                apiTickCycle += 1
                                secondsUntilRefresh = syncIntervalSec
                                val response = BalasoreLocalBusApiService.fetchLiveBusTelemetryFromAssetOrMockApi(
                                    context = context,
                                    tickCycle = apiTickCycle,
                                    simulateNetworkDelayMs = 300L
                                )
                                arrivalList = response.routes
                                onTimeFleetPct = response.onTimePercentage
                                lastApiSyncMillis = response.fetchedAtMillis
                                try {
                                    BalasoreLocalBusApiService.syncTelemetryToRoom(busScheduleDao, response.routes)
                                } catch (_: Throwable) {}
                                isRefreshing = false
                                Toast.makeText(
                                    context,
                                    if (isOdia) "ବସ୍ ଆଗମନ ଓ ପ୍ରସ୍ଥାନ ସମୟ ଅପଡେଟ୍ ହେଲା!" else "Live Balasore bus arrival times refreshed!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("bus_refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Bus Arrival Times",
                        tint = OceanBlue,
                        modifier = Modifier
                            .size(20.dp)
                            .then(if (isRefreshing) Modifier.rotate(spinAngle) else Modifier)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Simulated API Telemetry Banner (Fleet count, On-Time %, Countdown, Last Synced)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = OceanBlue.copy(alpha = 0.06f),
                border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.18f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bus_api_telemetry_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = OceanBlue,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isOdia) {
                                "CRUT Mo Bus ଓ OSRTC ଫିଡ୍ • $onTimeFleetPct% ସମୟାନୁବର୍ତ୍ତୀ"
                            } else {
                                "CRUT Mo Bus & OSRTC API • $onTimeFleetPct% On-Time"
                            },
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OceanBlue
                        )
                    }
                    Text(
                        text = "Next in ${secondsUntilRefresh}s • $formattedSyncTime",
                        fontSize = 9.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Arrival / Departure Schedule Mode Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bus_schedule_mode_row"),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Triple("ALL", if (isOdia) "ସମସ୍ତ ସମୟ ସାରଣୀ" else "All Schedules", Icons.Default.Route),
                    Triple("ARRIVALS", if (isOdia) "ଶୀଘ୍ର ଆଗମନ" else "Next Arrivals", Icons.Default.FlightLand),
                    Triple("DEPARTURES", if (isOdia) "ଷ୍ଟାଣ୍ଡ ପ୍ରସ୍ଥାନ" else "Departures", Icons.Default.FlightTakeoff)
                ).forEach { (tabKey, label, icon) ->
                    val isSelected = selectedScheduleTab == tabKey
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) OceanBlue else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) OceanBlue else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { selectedScheduleTab = tabKey }
                            .testTag("bus_mode_tab_${tabKey.lowercase(Locale.ROOT)}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else OceanBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = label,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Interactive Boarding Stop Selector (shows live arrival times at a chosen Balasore stop)
            Text(
                text = if (isOdia) "📍 ଆପଣଙ୍କ ଷ୍ଟପ୍ ଚୟନ କରନ୍ତୁ (ଷ୍ଟପ୍ ଆଗମନ ସମୟ):" else "📍 Select Boarding Stop for Exact Arrival Time:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .testTag("bus_boarding_stop_selector_row"),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "ALL" to if (isOdia) "ସମସ୍ତ ଷ୍ଟପ୍" else "All Stops",
                    "Sahadevkhunta" to if (isOdia) "ସହଦେବଖୁଣ୍ଟା" else "Sahadevkhunta Stand",
                    "Railway Station" to if (isOdia) "ରେଳ ଷ୍ଟେସନ" else "Railway Station",
                    "Fakir Mohan Golaye" to if (isOdia) "ଫକୀର ମୋହନ ଗୋଲେଇ" else "FM Golaye",
                    "ITI Chhak" to if (isOdia) "ଆଇଟିଆଇ ଛକ" else "ITI Chhak",
                    "Police Line" to if (isOdia) "ପୋଲିସ ଲାଇନ" else "Police Line",
                    "Remuna" to if (isOdia) "ରେମୁଣା" else "Remuna Golai",
                    "Chandipur" to if (isOdia) "ଚାନ୍ଦିପୁର" else "Chandipur Beach",
                    "Nilagiri" to if (isOdia) "ନୀଳଗିରି" else "Nilagiri",
                    "Soro" to if (isOdia) "ସୋର" else "Soro"
                ).forEach { (stopKey, stopLabel) ->
                    val isStopSelected = selectedBoardingStop == stopKey
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isStopSelected) EmeraldGreen.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = BorderStroke(
                            1.dp,
                            if (isStopSelected) EmeraldGreen else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedBoardingStop = stopKey }
                            .testTag("bus_stop_chip_${stopKey.lowercase(Locale.ROOT).replace(" ", "_")}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = if (isStopSelected) EmeraldGreen else OceanBlue,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stopLabel,
                                fontSize = 10.5.sp,
                                fontWeight = if (isStopSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isStopSelected) EmeraldGreen else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Autocomplete Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { isSearchFocused = it.isFocused }
                    .testTag("bus_search_input"),
                placeholder = {
                    Text(
                        text = if (isOdia) {
                            "ଗନ୍ତବ୍ୟସ୍ଥଳ ବା ଷ୍ଟପ୍ ଖୋଜନ୍ତୁ (ଯଥା: ଚାନ୍ଦିପୁର, ରେମୁଣା, ନୀଳଗିରି)..."
                        } else {
                            "Filter by region, stop or route (e.g. Chandipur, Remuna, Soro)..."
                        },
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = OceanBlue,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
            )

            // Autocomplete Suggestions Dropdown
            AnimatedVisibility(visible = filteredSuggestions.isNotEmpty() && isSearchFocused) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        filteredSuggestions.forEach { suggestion ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        searchQuery = suggestion
                                        focusManager.clearFocus()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("autocomplete_suggestion_item"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = OceanBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = suggestion,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Regional Corridor Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "ALL" to if (isOdia) "ସମସ୍ତ ରୁଟ୍" else "All Regions",
                    "CHANDIPUR" to if (isOdia) "ଚାନ୍ଦିପୁର / ବଳରାମଗଡ଼ି" else "Chandipur Coast",
                    "REMUNA" to if (isOdia) "ରେମୁଣା" else "Remuna",
                    "NILAGIRI" to if (isOdia) "ନୀଳଗିରି" else "Nilagiri Hills",
                    "INTERCITY" to if (isOdia) "ସୋରୋ / ଜଳେଶ୍ୱର" else "Soro & Jaleswar (NH-16)",
                    "CAMPUS" to if (isOdia) "ଏମ୍ସ / FMU" else "AIIMS & FMU"
                ).forEach { (key, label) ->
                    val isSelected = selectedCategoryFilter == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategoryFilter = key },
                        label = { Text(text = label, fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("bus_filter_chip_${key.lowercase(Locale.ROOT)}"),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Results count & sync interval banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isOdia) {
                        "${filteredBuses.size} ଟି ସକ୍ରିୟ ବସ୍ ଟ୍ରାକ୍ ହେଉଛି"
                    } else {
                        "${filteredBuses.size} active buses tracked"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Live GPS + Static JSON • ${syncIntervalSec}s interval",
                    fontSize = 10.sp,
                    color = EmeraldGreen,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bus Cards List
            if (filteredBuses.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBusFilled,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No buses found matching \"$searchQuery\"",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                val visibleRoutes = if (showAllRoutes) filteredBuses else filteredBuses.take(5)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    visibleRoutes.forEach { bus ->
                        val isExpanded = expandedBusId == bus.busId
                        val isAlertActive = alertSubscribedBusIds.contains(bus.busId)
                        val stopEtaMins = BalasoreLocalBusApiService.computeArrivalMinutesForStop(
                            bus = bus,
                            stopName = selectedBoardingStop
                        )
                        BusArrivalCard(
                            item = bus,
                            isOdia = isOdia,
                            isExpanded = isExpanded,
                            selectedBoardingStop = selectedBoardingStop,
                            stopEtaMinutes = stopEtaMins,
                            isAlertSubscribed = isAlertActive,
                            onToggleAlert = {
                                val updated = if (isAlertActive) {
                                    alertSubscribedBusIds - bus.busId
                                } else {
                                    alertSubscribedBusIds + bus.busId
                                }
                                alertSubscribedBusIds = updated
                                Toast.makeText(
                                    context,
                                    if (!isAlertActive) {
                                        if (isOdia) "${bus.routeNumber} ଆଗମନ ଆଲର୍ଟ ସକ୍ରିୟ ହେଲା!" else "Arrival alert set for ${bus.routeNumber} (${bus.destination})"
                                    } else {
                                        if (isOdia) "${bus.routeNumber} ଆଲର୍ଟ ବନ୍ଦ ହେଲା" else "Arrival alert removed for ${bus.routeNumber}"
                                    },
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onToggleExpand = {
                                expandedBusId = if (isExpanded) null else bus.busId
                            },
                            onClick = { onRouteSelected(bus) }
                        )
                    }
                }

                if (filteredBuses.size > 5) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { showAllRoutes = !showAllRoutes },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bus_toggle_show_all_btn")
                    ) {
                        Text(
                            text = if (showAllRoutes) {
                                if (isOdia) "ସଂକ୍ଷିପ୍ତ ତାଲିକା ଦେଖନ୍ତୁ" else "Show Compact View (Top 5)"
                            } else {
                                if (isOdia) "ସମସ୍ତ ${filteredBuses.size} ଟି ରୁଟ୍ ଦେଖନ୍ତୁ" else "View All ${filteredBuses.size} Regional Bus Routes"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = OceanBlue
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (showAllRoutes) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = OceanBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual Bus Arrival & Departure card with live telemetry pill, explicit departure & arrival times,
 * route progress indicator, connecting region tags, arrival alert toggle, and expandable stop timeline.
 */
@Composable
private fun BusArrivalCard(
    item: BusArrivalItem,
    isOdia: Boolean,
    isExpanded: Boolean,
    selectedBoardingStop: String = "ALL",
    stopEtaMinutes: Int = item.arrivalMinutes,
    isAlertSubscribed: Boolean = false,
    onToggleAlert: () -> Unit = {},
    onToggleExpand: () -> Unit,
    onClick: () -> Unit
) {
    val isUrgent = stopEtaMinutes <= 5
    val isDelayed = item.statusTag == "DELAYED"
    val statusBadgeColor = when {
        isDelayed -> Color(0xFFDC2626)
        isUrgent -> EmeraldGreen
        else -> OceanBlue
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(
            1.dp,
            if (isExpanded) OceanBlue.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onToggleExpand()
                onClick()
            }
            .testTag("bus_route_card")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Top Row: Route Number, Operator/Type & Live ETA Badge + Alert Bell
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = item.routeNumber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${item.busType} • ${item.operator}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Arrival ETA Pill
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = statusBadgeColor.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, statusBadgeColor.copy(alpha = 0.45f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = statusBadgeColor,
                                modifier = Modifier.size(6.dp)
                            ) {}
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (selectedBoardingStop != "ALL") {
                                    if (isOdia) "$selectedBoardingStop: $stopEtaMinutes ମିନିଟ୍" else "$selectedBoardingStop in ${stopEtaMinutes}m"
                                } else {
                                    if (isOdia) item.arrivalStatusOd else item.arrivalStatusEn
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusBadgeColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Arrival Alert Bell Button
                    Surface(
                        shape = CircleShape,
                        color = if (isAlertSubscribed) AmberGold.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (isAlertSubscribed) AmberGold else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .clickable { onToggleAlert() }
                            .testTag("bus_arrival_alert_btn_${item.busId}")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isAlertSubscribed) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                contentDescription = "Toggle Arrival Alert",
                                tint = if (isAlertSubscribed) Color(0xFFB45309) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Route Name
            Text(
                text = if (isOdia) item.routeNameOd else item.routeNameEn,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Explicit Departure & Scheduled Arrival Times Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(9.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FlightTakeoff,
                        contentDescription = null,
                        tint = OceanBlue,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = if (isOdia) "ପ୍ରସ୍ଥାନ (${item.origin})" else "Dep: ${item.origin}",
                            fontSize = 9.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = item.departureTime,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = AmberGold.copy(alpha = 0.16f)
                ) {
                    Text(
                        text = "Next in ${item.nextDepartureInMinutes}m",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB45309),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FlightLand,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (isOdia) "ଆଗମନ (${item.destination})" else "Arr: ${item.destination}",
                            fontSize = 9.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = item.scheduledArrivalTime,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = EmeraldGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(7.dp))

            // Route Progress Bar + Current Stop -> Next Stop
            LinearProgressIndicator(
                progress = { item.routeProgressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = statusBadgeColor,
                trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsBus,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${item.currentStop} → ${item.nextStop}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Footer info: Platform Bay, Speed, Occupancy, Fare & Expand Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${item.platformBay} • ${item.speedKmh} km/h • ${
                            when (item.occupancyStatus) {
                                "SEATS_AVAILABLE" -> "Seats Open"
                                "MODERATE" -> "Moderate Load"
                                else -> "High Load"
                            }
                        }",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Fare: ${item.fare}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse stops" else "Expand stops",
                        tint = OceanBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Expandable Stop-by-Stop Corridor Timeline
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isOdia) {
                            "📍 ସଂଯୋଗକାରୀ ଅଞ୍ଚଳ ଓ ଷ୍ଟପ୍ ସମୟ ସାରଣୀ:"
                        } else {
                            "📍 Connecting Regions & Stop-by-Stop Schedule:"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = OceanBlue
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val stops = item.stopsSequence.ifEmpty {
                        listOf(
                            "${item.origin} (Dep ${item.departureTime})",
                            item.currentStop,
                            item.nextStop,
                            "${item.destination} (Arr ${item.scheduledArrivalTime})"
                        )
                    }
                    stops.forEachIndexed { idx, stopLabel ->
                        val isPassed = idx < (stops.size * item.routeProgressFraction).toInt()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isPassed) EmeraldGreen else OceanBlue.copy(alpha = 0.4f),
                                modifier = Modifier.size(8.dp)
                            ) {}
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stopLabel,
                                fontSize = 11.sp,
                                fontWeight = if (isPassed) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isPassed) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (item.connectingRegions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            item.connectingRegions.forEach { region ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = OceanBlue.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = region,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = OceanBlue,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compact Home & News dashboard banner card showing real-time bus arrival countdowns
 * for major Balasore routes with one-tap launch into the full Local Bus Tracker.
 */
@Composable
fun BalasoreLocalBusTrackerBannerCard(
    language: AppLanguage = AppLanguage.ENGLISH,
    onOpenFullTracker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isOdia = language == AppLanguage.ODIA
    var tick by remember { mutableIntStateOf(0) }
    var topBuses by remember {
        mutableStateOf(BalasoreLocalBusApiService.loadRoutesFromStaticJsonAsset(context).take(3))
    }

    LaunchedEffect(Unit) {
        while (true) {
            val response = BalasoreLocalBusApiService.fetchLiveBusTelemetryFromAssetOrMockApi(
                context = context,
                tickCycle = tick,
                simulateNetworkDelayMs = 0L
            )
            topBuses = response.routes.sortedBy { it.arrivalMinutes }.take(3)
            delay(15_000L)
            tick += 1
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOpenFullTracker() }
            .testTag("home_local_bus_tracker_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.28f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = OceanBlue.copy(alpha = 0.14f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.DirectionsBus,
                                contentDescription = null,
                                tint = OceanBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isOdia) "ଲାଇଭ୍ ବାଲେଶ୍ୱର ବସ୍ ଟ୍ରାକର" else "Local Bus Tracker • Live Arrivals",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(5.dp),
                                color = EmeraldGreen.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "LIVE",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EmeraldGreen,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = if (isOdia) {
                                "ସହଦେବଖୁଣ୍ଟା, ଚାନ୍ଦିପୁର, ରେମୁଣା, ନୀଳଗିରି ଓ ସୋର ରୁଟ୍"
                            } else {
                                "Sahadevkhunta • Chandipur • Remuna • Nilagiri • Soro"
                            },
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                OutlinedButton(
                    onClick = onOpenFullTracker,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("open_full_bus_tracker_btn")
                ) {
                    Text(
                        text = if (isOdia) "ସମସ୍ତ ରୁଟ୍" else "All 8 Routes",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                topBuses.forEach { bus ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(5.dp),
                                    color = OceanBlue.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = bus.routeNumber,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = OceanBlue,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "${bus.origin} ↔ ${bus.destination}",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Via ${bus.currentStop} • ${bus.platformBay}",
                                        fontSize = 9.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (bus.arrivalMinutes <= 4) EmeraldGreen.copy(alpha = 0.16f) else OceanBlue.copy(alpha = 0.14f)
                            ) {
                                Text(
                                    text = if (isOdia) "${bus.arrivalMinutes} ମିନିଟ୍" else "In ${bus.arrivalMinutes} min",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (bus.arrivalMinutes <= 4) EmeraldGreen else OceanBlue,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Full-screen / Modal Bottom Sheet wrapper for the Real-Time Local Bus Tracker.
 */
@Composable
fun BalasoreLocalBusTrackerSheet(
    language: AppLanguage = AppLanguage.ENGLISH,
    onClose: () -> Unit
) {
    val isOdia = language == AppLanguage.ODIA
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("local_bus_tracker_sheet"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isOdia) "🚌 ବାଲେଶ୍ୱର ଲାଇଭ୍ ବସ୍ ଟ୍ରାକର" else "🚌 Balasore Local Bus Tracker",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Text(
                        text = if (isOdia) {
                            "CRUT Mo Bus ଓ OSRTC ପ୍ରମୁଖ ରୁଟ୍ ଆଗମନ ସମୟ"
                        } else {
                            "Live Arrival Countdown • Static Route JSON + Real-Time Mock API"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
                FilledTonalIconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("close_local_bus_tracker_sheet_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Local Bus Tracker"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LocalBusTracker(
                language = language,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            LocalBusFareCalculatorCard(
                language = language,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            BusAndTrainAlertAndDelayTrackerCard(
                language = language,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}


