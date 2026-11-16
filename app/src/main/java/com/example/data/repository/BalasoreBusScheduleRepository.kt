package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.BusScheduleDao
import com.example.data.local.BusScheduleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * Repository for Balasore Bus Stand Live Bus Schedules.
 * Manages Room database operations, seeding baseline timetables from
 * Sahadevkhunta Central Bus Stand & Old Bus Stand, and real-time live status evaluation.
 */
class BalasoreBusScheduleRepository(
    private val dao: BusScheduleDao
) {

    constructor(context: Context) : this(
        AppDatabase.getInstance(context).busScheduleDao()
    )

    fun getAllSchedules(): Flow<List<BusScheduleEntity>> {
        return dao.getAllSchedules()
    }

    fun getFilteredSchedules(
        destinationQuery: String?,
        minMinutes: Int?,
        maxMinutes: Int?
    ): Flow<List<BusScheduleEntity>> {
        val cleanDest = destinationQuery?.trim()?.takeIf { it.isNotBlank() }
        return dao.filterSchedules(cleanDest, minMinutes, maxMinutes)
    }

    fun getDistinctDestinations(): Flow<List<String>> {
        return dao.getDistinctDestinations()
    }

    suspend fun insertSchedule(schedule: BusScheduleEntity): Long = withContext(Dispatchers.IO) {
        dao.insertSchedule(schedule)
    }

    suspend fun updateSchedule(schedule: BusScheduleEntity) = withContext(Dispatchers.IO) {
        dao.updateSchedule(schedule)
    }

    suspend fun deleteSchedule(schedule: BusScheduleEntity) = withContext(Dispatchers.IO) {
        dao.deleteSchedule(schedule)
    }

    suspend fun resetToDefaultSchedules() = withContext(Dispatchers.IO) {
        dao.clearAll()
        dao.insertSchedules(DEFAULT_BALASORE_BUS_SCHEDULES)
    }

    suspend fun ensureSeeded() = withContext(Dispatchers.IO) {
        val count = dao.getScheduleCount()
        if (count == 0) {
            dao.insertSchedules(DEFAULT_BALASORE_BUS_SCHEDULES)
        }
    }

    companion object {
        /**
         * Converts "HH:mm AM/PM" into minutes from start of day (0..1439).
         */
        fun timeToMinutesOfDay(hour12: Int, minute: Int, isPm: Boolean): Int {
            var h = hour12 % 12
            if (isPm) h += 12
            return h * 60 + minute
        }

        /**
         * Calculates live status based on current time of day in minutes.
         */
        fun computeLiveStatus(departureMinutes: Int): String {
            val cal = Calendar.getInstance()
            val currentMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
            val diff = departureMinutes - currentMinutes

            return when {
                diff in -15..0 -> "DEPARTED"
                diff in 1..15 -> "BOARDING_NOW"
                diff in 16..35 -> "DEPARTING_SOON"
                diff > 35 -> "ON_TIME"
                diff in -45..-16 -> "DELAYED"
                else -> "SCHEDULED"
            }
        }

        val DEFAULT_BALASORE_BUS_SCHEDULES = listOf(
            // --- BHUBANESWAR ROUTE (High Frequency NH-16) ---
            BusScheduleEntity(
                busNumber = "OD-01-B-8942",
                serviceName = "OSRTC Rajdhani Deluxe",
                busStandName = "Sahadevkhunta Central Bus Stand",
                destination = "Bhubaneswar",
                destinationOdia = "ଭୁବନେଶ୍ୱର",
                departureTime = "05:00 AM",
                departureMinutesOfDay = 5 * 60,
                arrivalTime = "08:45 AM",
                estimatedDuration = "3h 45m",
                busType = "AC Deluxe Seater",
                bayPlatform = "Bay 1",
                fareInr = 240,
                viaRoute = "Via Soro, Bhadrak, Panikoili, Cuttack (NH-16)",
                status = "ON_TIME",
                frequency = "Daily Early Service",
                contactPhone = "06782-262066"
            ),
            BusScheduleEntity(
                busNumber = "OD-01-C-4512",
                serviceName = "CRUT Interdistrict Express",
                busStandName = "Sahadevkhunta Central Bus Stand",
                destination = "Bhubaneswar",
                destinationOdia = "ଭୁବନେଶ୍ୱର",
                departureTime = "06:15 AM",
                departureMinutesOfDay = 6 * 60 + 15,
                arrivalTime = "10:00 AM",
                estimatedDuration = "3h 45m",
                busType = "Mo Bus AC Electric",
                bayPlatform = "Bay 2",
                fareInr = 210,
                viaRoute = "Via Sergarh, Soro, Bhadrak, Cuttack",
                status = "BOARDING_NOW",
                frequency = "Daily Regular",
                contactPhone = "06782-262066"
            ),
            BusScheduleEntity(
                busNumber = "OD-01-D-7821",
                serviceName = "Baleswari Star Line",
                busStandName = "Sahadevkhunta Central Bus Stand",
                destination = "Bhubaneswar",
                destinationOdia = "ଭୁବନେଶ୍ୱର",
                departureTime = "07:45 AM",
                departureMinutesOfDay = 7 * 60 + 45,
                arrivalTime = "11:30 AM",
                estimatedDuration = "3h 45m",
                busType = "Non-AC Hi-Tech",
                bayPlatform = "Bay 1",
                fareInr = 160,
                viaRoute = "Via Bhadrak By-pass, Panikoili, Chandikhole",
                status = "ON_TIME",
                frequency = "Daily Regular",
                contactPhone = "06782-262066"
            ),
            BusScheduleEntity(
                busNumber = "OD-01-E-9014",
                serviceName = "OSRTC Ultra Deluxe",
                busStandName = "Sahadevkhunta Central Bus Stand",
                destination = "Bhubaneswar",
                destinationOdia = "ଭୁବନେଶ୍ୱର",
                departureTime = "09:30 AM",
                departureMinutesOfDay = 9 * 60 + 30,
                arrivalTime = "01:15 PM",
                estimatedDuration = "3h 45m",
                busType = "AC 2+2 Semi-Sleeper",
                bayPlatform = "Bay 2",
                fareInr = 260,
                viaRoute = "Via Soro, Bhadrak, Panikoili, Cuttack",
                status = "ON_TIME",
                frequency = "Daily Express",
                contactPhone = "06782-262066"
            ),
            BusScheduleEntity(
                busNumber = "OD-01-F-3329",
                serviceName = "Jagannath Travels Express",
                busStandName = "Sahadevkhunta Central Bus Stand",
                destination = "Bhubaneswar",
                destinationOdia = "ଭୁବନେଶ୍ୱର",
                departureTime = "01:15 PM",
                departureMinutesOfDay = 13 * 60 + 15,
                arrivalTime = "05:00 PM",
                estimatedDuration = "3h 45m",
                busType = "Non-AC Seater",
                bayPlatform = "Bay 1",
                fareInr = 160,
                viaRoute = "Via Bhadrak, Jajpur Road, Cuttack Badambadi",
                status = "ON_TIME",
                frequency = "Daily Afternoon",
                contactPhone = "06782-262066"
            ),
            BusScheduleEntity(
                busNumber = "OD-01-G-5510",
                serviceName = "OSRTC Evening Express",
                busStandName = "Sahadevkhunta Central Bus Stand",
                destination = "Bhubaneswar",
                destinationOdia = "ଭୁବନେଶ୍ୱର",
                departureTime = "05:30 PM",
                departureMinutesOfDay = 17 * 60 + 30,
                arrivalTime = "09:15 PM",
                estimatedDuration = "3h 45m",
                busType = "AC Deluxe",
                bayPlatform = "Bay 2",
                fareInr = 240,
                viaRoute = "Via Soro, Bhadrak, Panikoili, Cuttack",
                status = "ON_TIME",
                frequency = "Daily Evening",
                contactPhone = "06782-262066"
            ),
            BusScheduleEntity(
                busNumber = "OD-01-H-9923",
                serviceName = "Royal Cruiser Night Rider",
                busStandName = "Sahadevkhunta Central Bus Stand",
                destination = "Bhubaneswar",
                destinationOdia = "ଭୁବନେଶ୍ୱର",
                departureTime = "10:45 PM",
                departureMinutesOfDay = 22 * 60 + 45,
                arrivalTime = "02:30 AM",
                estimatedDuration = "3h 45m",
                busType = "Volvo Multi-Axle Sleeper",
                bayPlatform = "Bay 3",
                fareInr = 380,
                viaRoute = "Direct Non-Stop Expressway NH-16",
                status = "ON_TIME",
                frequency = "Daily Night",
                contactPhone = "06782-262066"
            ),

            // --- KOLKATA ROUTE (Interstate Bengal Corridor) ---
            BusScheduleEntity(
                busNumber = "WB-29-A-1102",
                serviceName = "Shyamoli Paribahan Express",
                busStandName = "Sahadevkhunta Central Bus Stand",
                destination = "Kolkata (Esplanade)",
                destinationOdia = "କୋଲକାତା (ଏସପ୍ଲାନେଡ୍)",
                departureTime = "06:00 AM",
                departureMinutesOfDay = 6 * 60,
                arrivalTime = "11:30 AM",
                estimatedDuration = "5h 30m",
                busType = "AC Volvo Seater",
                bayPlatform = "Bay 4",
                fareInr = 380,
                viaRoute = "Via Basta, Jaleswar, Belda, Kharagpur, Kolaghat",
                status = "BOARDING_NOW",
                frequency = "Daily Morning",
                contactPhone = "06782-262066"
            ),
            BusScheduleEntity(
                busNumber = "OD-01-K-7741",
                serviceName = "OSRTC Inter-State Royal",
                busStandName = "Sahadevkhunta Central Bus Stand",
                destination = "Kolkata (Babughat)",
                destinationOdia = "କୋଲକାତା (ବାବୁଘାଟ)",
                departureTime = "08:30 AM",
                departureMinutesOfDay = 8 * 60 + 30,
                arrivalTime = "02:15 PM",
                estimatedDuration = "5h 45m",
                busType = "AC Deluxe",
                bayPlatform = "Bay 4",
                fareInr = 340,
                viaRoute = "Via Jaleswar Border, Kharagpur Bypass, Kona",
                status = "ON_TIME",
                frequency = "Daily Regular",
                contactPhone = "06782-262066"
            ),
            BusScheduleEntity(
                busNumber = "WB-33-C-5011",
                serviceName = "Greenline Express",
                busStandName = "Sahadevkhunta Central Bus Stand",
                destination = "Kolkata (Esplanade)",
                destinationOdia = "କୋଲକାତା (ଏସପ୍ଲାନେଡ୍)",
                departureTime = "02:00 PM",
                departureMinutesOfDay = 14 * 60,
                arrivalTime = "07:30 PM",
                estimatedDuration = "5h 30m",
                busType = "Volvo Multi-Axle",
                bayPlatform = "Bay 4",
                fareInr = 420,
                viaRoute = "Via NH-16 Golden Quadrilateral, Vidyasagar Setu",
                status = "ON_TIME",
                frequency = "Daily Afternoon",
                contactPhone = "06782-262066"
            ),
            BusScheduleEntity(
                busNumber = "OD-01-N-6620",
                serviceName = "Dolphin Night Super",
                busStandName = "Sahadevkhunta Central Bus Stand",
                destination = "Kolkata (Esplanade)",
                destinationOdia = "କୋଲକାତା (ଏସପ୍ଲାନେଡ୍)",
                departureTime = "11:15 PM",
                departureMinutesOfDay = 23 * 60 + 15,
                arrivalTime = "04:45 AM",
                estimatedDuration = "5h 30m",
                busType = "AC Sleeper 2+1",
                bayPlatform = "Bay 4",
                fareInr = 480,
                viaRoute = "Expressway Direct via Jaleswar, Kharagpur",
                status = "ON_TIME",
                frequency = "Daily Overnight",
                contactPhone = "06782-262066"
            ),

            // --- BARIPADA / MAYURBHANJ ROUTE ---
            BusScheduleEntity(
                busNumber = "OD-01-P-3301",
                serviceName = "Mayurbhanj Express",
                busStandName = "Sahadevkhunta Central Bus Stand",
                destination = "Baripada",
                destinationOdia = "ବାରିପଦା",
                departureTime = "06:30 AM",
                departureMinutesOfDay = 6 * 60 + 30,
                arrivalTime = "08:15 AM",
                estimatedDuration = "1h 45m",
                busType = "Non-AC Deluxe",
                bayPlatform = "Bay 5",
                fareInr = 75,
                viaRoute = "Via Remuna Chhak, Betnoti, Badasahi",
                status = "DEPARTING_SOON",
                frequency = "Every 45 mins",
                contactPhone = "06782-262066"
            ),
            BusScheduleEntity(
                busNumber = "OD-11-A-4122",
                serviceName = "Bhanja Express",
                busStandName = "Sahadevkhunta Central Bus Stand",
                destination = "Baripada",
                destinationOdia = "ବାରିପଦା",
                departureTime = "08:45 AM",
                departureMinutesOfDay = 8 * 60 + 45,
                arrivalTime = "10:30 AM",
                estimatedDuration = "1h 45m",
                busType = "Standard Seater",
                bayPlatform = "Bay 5",
                fareInr = 65,
                viaRoute = "Via Sergarh, Betnoti, Krushnachandrapur",
                status = "ON_TIME",
                frequency = "Daily Regular",
                contactPhone = "06782-262066"
            ),
            BusScheduleEntity(
                busNumber = "OD-01-Q-8819",
                serviceName = "Similipal AC Shuttle",
                busStandName = "Sahadevkhunta Central Bus Stand",
                destination = "Baripada",
                destinationOdia = "ବାରିପଦା",
                departureTime = "12:15 PM",
                departureMinutesOfDay = 12 * 60 + 15,
                arrivalTime = "01:55 PM",
                estimatedDuration = "1h 40m",
                busType = "AC Mini Coach",
                bayPlatform = "Bay 5",
                fareInr = 110,
                viaRoute = "Via NH-18 Betnoti Bypass",
                status = "ON_TIME",
                frequency = "Daily Regular",
                contactPhone = "06782-262066"
            ),
            BusScheduleEntity(
                busNumber = "OD-01-R-9042",
                serviceName = "Chhau Heritage Express",
                busStandName = "Sahadevkhunta Central Bus Stand",
                destination = "Baripada",
                destinationOdia = "ବାରିପଦା",
                departureTime = "04:30 PM",
                departureMinutesOfDay = 16 * 60 + 30,
                arrivalTime = "06:15 PM",
                estimatedDuration = "1h 45m",
                busType = "Non-AC Seater",
                bayPlatform = "Bay 5",
                fareInr = 75,
                viaRoute = "Via Remuna Chhak, Betnoti, Baripada Stand",
                status = "ON_TIME",
                frequency = "Daily Regular",
                contactPhone = "06782-262066"
            ),

            // --- CHANDIPUR BEACH & COASTAL ROUTE ---
            BusScheduleEntity(
                busNumber = "OD-01-AX-7101",
                serviceName = "Mo Bus Route 71 (Beach Shuttle)",
                busStandName = "Sahadevkhunta Central Bus Stand",
                destination = "Chandipur Sea Beach",
                destinationOdia = "ଚାନ୍ଦିପୁର ବେଳାଭୂମି",
                departureTime = "06:00 AM",
                departureMinutesOfDay = 6 * 60,
                arrivalTime = "06:40 AM",
                estimatedDuration = "40m",
                busType = "Mo Bus AC Electric",
                bayPlatform = "Bay 6",
                fareInr = 20,
                viaRoute = "Via Cinema Chhak, Proof Road, DRDO Gate, OTDC",
                status = "ON_TIME",
                frequency = "Every 30 mins",
                contactPhone = "06782-262066"
            ),
            BusScheduleEntity(
                busNumber = "OD-01-AX-7103",
                serviceName = "Mo Bus Route 71 (Vanishing Sea Special)",
                busStandName = "Sahadevkhunta Central Bus Stand",
                destination = "Chandipur Sea Beach",
                destinationOdia = "ଚାନ୍ଦିପୁର ବେଳାଭୂମି",
                departureTime = "08:30 AM",
                departureMinutesOfDay = 8 * 60 + 30,
                arrivalTime = "09:10 AM",
                estimatedDuration = "40m",
                busType = "Mo Bus AC Electric",
                bayPlatform = "Bay 6",
                fareInr = 20,
                viaRoute = "Via Station Square, Proof Road, Panthanivas",
                status = "ON_TIME",
                frequency = "Every 30 mins",
                contactPhone = "06782-262066"
            ),
            BusScheduleEntity(
                busNumber = "OD-01-AX-7105",
                serviceName = "Mo Bus Route 71 (Sunset Special)",
                busStandName = "Sahadevkhunta Central Bus Stand",
                destination = "Chandipur Sea Beach",
                destinationOdia = "ଚାନ୍ଦିପୁର ବେଳାଭୂମି",
                departureTime = "04:00 PM",
                departureMinutesOfDay = 16 * 60,
                arrivalTime = "04:40 PM",
                estimatedDuration = "40m",
                busType = "Mo Bus AC Electric",
                bayPlatform = "Bay 6",
                fareInr = 20,
                viaRoute = "Via Proof Road, DRDO Main Gate, Chandipur Beach",
                status = "ON_TIME",
                frequency = "Every 30 mins",
                contactPhone = "06782-262066"
            ),

            // --- CUTTACK ROUTE ---
            BusScheduleEntity(
                busNumber = "OD-05-K-1922",
                serviceName = "Mahanadi Superfast",
                busStandName = "Sahadevkhunta Central Bus Stand",
                destination = "Cuttack",
                destinationOdia = "କଟକ",
                departureTime = "05:45 AM",
                departureMinutesOfDay = 5 * 60 + 45,
                arrivalTime = "09:00 AM",
                estimatedDuration = "3h 15m",
                busType = "AC Deluxe",
                bayPlatform = "Bay 2",
                fareInr = 210,
                viaRoute = "Via Soro, Bhadrak, Chandikhole, Badambadi",
                status = "ON_TIME",
                frequency = "Daily Regular",
                contactPhone = "06782-262066"
            ),
            BusScheduleEntity(
                busNumber = "OD-01-S-5520",
                serviceName = "Millennium City Link",
                busStandName = "Sahadevkhunta Central Bus Stand",
                destination = "Cuttack",
                destinationOdia = "କଟକ",
                departureTime = "09:15 AM",
                departureMinutesOfDay = 9 * 60 + 15,
                arrivalTime = "12:35 PM",
                estimatedDuration = "3h 20m",
                busType = "Non-AC Seater",
                bayPlatform = "Bay 2",
                fareInr = 150,
                viaRoute = "Via Bhadrak Bypass, Panikoili, Tangi",
                status = "ON_TIME",
                frequency = "Daily Regular",
                contactPhone = "06782-262066"
            ),

            // --- DIGHA & TALASARI COASTAL ROUTE ---
            BusScheduleEntity(
                busNumber = "OD-01-T-7721",
                serviceName = "Coastal Digha Inter-State",
                busStandName = "Sahadevkhunta Central Bus Stand",
                destination = "Digha",
                destinationOdia = "ଦୀଘା",
                departureTime = "06:45 AM",
                departureMinutesOfDay = 6 * 60 + 45,
                arrivalTime = "09:30 AM",
                estimatedDuration = "2h 45m",
                busType = "Non-AC Express",
                bayPlatform = "Bay 7",
                fareInr = 120,
                viaRoute = "Via Basta, Jaleswar, Chandaneswar, Talasari",
                status = "ON_TIME",
                frequency = "Daily Morning",
                contactPhone = "06782-262066"
            ),
            BusScheduleEntity(
                busNumber = "WB-30-E-9041",
                serviceName = "Bengal Sea Coast Cruiser",
                busStandName = "Sahadevkhunta Central Bus Stand",
                destination = "Digha",
                destinationOdia = "ଦୀଘା",
                departureTime = "11:45 AM",
                departureMinutesOfDay = 11 * 60 + 45,
                arrivalTime = "02:30 PM",
                estimatedDuration = "2h 45m",
                busType = "AC Deluxe",
                bayPlatform = "Bay 7",
                fareInr = 165,
                viaRoute = "Via Jaleswar, Chandaneswar Temple, New Digha",
                status = "ON_TIME",
                frequency = "Daily Regular",
                contactPhone = "06782-262066"
            ),

            // --- NILAGIRI & PANCHALINGESWAR ROUTE ---
            BusScheduleEntity(
                busNumber = "OD-01-U-2290",
                serviceName = "Kuldiha Eco-Route Shuttle",
                busStandName = "Sahadevkhunta Central Bus Stand",
                destination = "Nilagiri",
                destinationOdia = "ନୀଳଗିରି",
                departureTime = "07:00 AM",
                departureMinutesOfDay = 7 * 60,
                arrivalTime = "07:55 AM",
                estimatedDuration = "55m",
                busType = "Non-AC Seater",
                bayPlatform = "Bay 8",
                fareInr = 45,
                viaRoute = "Via Remuna, Sergarh, Mitrapur, Panchalingeswar Chhak",
                status = "ON_TIME",
                frequency = "Every 40 mins",
                contactPhone = "06782-262066"
            ),
            BusScheduleEntity(
                busNumber = "OD-01-U-3388",
                serviceName = "Panchalingeswar Temple Link",
                busStandName = "Sahadevkhunta Central Bus Stand",
                destination = "Nilagiri",
                destinationOdia = "ନୀଳଗିରି",
                departureTime = "10:15 AM",
                departureMinutesOfDay = 10 * 60 + 15,
                arrivalTime = "11:10 AM",
                estimatedDuration = "55m",
                busType = "Standard Seater",
                bayPlatform = "Bay 8",
                fareInr = 45,
                viaRoute = "Via Sergarh, Mitrapur, Nilagiri Rajbati",
                status = "ON_TIME",
                frequency = "Every 40 mins",
                contactPhone = "06782-262066"
            ),

            // --- PURI PILGRIMAGE SPECIAL ---
            BusScheduleEntity(
                busNumber = "OD-02-B-6671",
                serviceName = "Jagannath Dham Pilgrim Special",
                busStandName = "Sahadevkhunta Central Bus Stand",
                destination = "Puri",
                destinationOdia = "ପୁରୀ",
                departureTime = "06:00 AM",
                departureMinutesOfDay = 6 * 60,
                arrivalTime = "11:00 AM",
                estimatedDuration = "5h 00m",
                busType = "AC Deluxe",
                bayPlatform = "Bay 3",
                fareInr = 310,
                viaRoute = "Via Bhadrak, Cuttack, Bhubaneswar, Pipili (NH-316)",
                status = "ON_TIME",
                frequency = "Daily Direct",
                contactPhone = "06782-262066"
            ),
            BusScheduleEntity(
                busNumber = "OD-01-V-8840",
                serviceName = "Sri Kshetra Night Deluxe",
                busStandName = "Sahadevkhunta Central Bus Stand",
                destination = "Puri",
                destinationOdia = "ପୁରୀ",
                departureTime = "08:30 PM",
                departureMinutesOfDay = 20 * 60 + 30,
                arrivalTime = "01:15 AM",
                estimatedDuration = "4h 45m",
                busType = "AC Semi-Sleeper",
                bayPlatform = "Bay 3",
                fareInr = 350,
                viaRoute = "Direct NH-16 via Bhubaneswar Bypass to Puri Badadanda",
                status = "ON_TIME",
                frequency = "Daily Night",
                contactPhone = "06782-262066"
            ),

            // --- BHADRAK LOCAL CONNECT ---
            BusScheduleEntity(
                busNumber = "OD-01-W-1044",
                serviceName = "Salandi Rapid Express",
                busStandName = "Sahadevkhunta Central Bus Stand",
                destination = "Bhadrak",
                destinationOdia = "ଭଦ୍ରକ",
                departureTime = "07:30 AM",
                departureMinutesOfDay = 7 * 60 + 30,
                arrivalTime = "08:50 AM",
                estimatedDuration = "1h 20m",
                busType = "Standard Seater",
                bayPlatform = "Bay 2",
                fareInr = 60,
                viaRoute = "Via Sergarh, Khantapada, Soro, Simulia",
                status = "ON_TIME",
                frequency = "Every 30 mins",
                contactPhone = "06782-262066"
            ),

            // --- JALESWAR NORTH BALASORE CORRIDOR ---
            BusScheduleEntity(
                busNumber = "OD-01-Y-3910",
                serviceName = "Subarnarekha Border Link",
                busStandName = "Sahadevkhunta Central Bus Stand",
                destination = "Jaleswar",
                destinationOdia = "ଜଳେଶ୍ୱର",
                departureTime = "08:15 AM",
                departureMinutesOfDay = 8 * 60 + 15,
                arrivalTime = "09:30 AM",
                estimatedDuration = "1h 15m",
                busType = "Non-AC Seater",
                bayPlatform = "Bay 7",
                fareInr = 55,
                viaRoute = "Via Rupsa, Basta, Amarda Road, Jaleswar Stand",
                status = "ON_TIME",
                frequency = "Hourly",
                contactPhone = "06782-262066"
            ),

            // --- ROURKELA / SAMBALPUR WEST ODISHA EXPRESS ---
            BusScheduleEntity(
                busNumber = "OD-14-H-6712",
                serviceName = "Steel City Overnight Express",
                busStandName = "Sahadevkhunta Central Bus Stand",
                destination = "Rourkela",
                destinationOdia = "ରାଉରକେଲା",
                departureTime = "08:00 PM",
                departureMinutesOfDay = 20 * 60,
                arrivalTime = "05:30 AM",
                estimatedDuration = "9h 30m",
                busType = "AC Sleeper Multi-Axle",
                bayPlatform = "Bay 1",
                fareInr = 550,
                viaRoute = "Via Keonjhar, Barkote, Deogarh, Rourkela Stand",
                status = "ON_TIME",
                frequency = "Daily Night",
                contactPhone = "06782-262066"
            )
        )
    }
}
