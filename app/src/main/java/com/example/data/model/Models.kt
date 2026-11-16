package com.example.data.model

/**
 * Supported Indian languages (All 22 Eighth Schedule languages of the Constitution of India + English).
 * Provides ISO code, native script label, regional origin, and script tags.
 */
enum class AppLanguage(
    val code: String,
    val nativeLabel: String,
    val fullDisplay: String,
    val scriptTag: String,
    val region: String,
    val isoCode: String,
    val scriptName: String = ""
) {
    ODIA(
        code = "Odia",
        nativeLabel = "ଓଡ଼ିଆ",
        fullDisplay = "ଓଡ଼ିଆ (Odia - Local)",
        scriptTag = "OR",
        region = "Odisha (District Balasore)",
        isoCode = "or",
        scriptName = "Odia Lipi"
    ),
    ENGLISH(
        code = "English",
        nativeLabel = "English",
        fullDisplay = "English (Official)",
        scriptTag = "EN",
        region = "Pan-India & Global",
        isoCode = "en",
        scriptName = "Latin"
    ),
    HINDI(
        code = "Hindi",
        nativeLabel = "हिन्दी",
        fullDisplay = "हिन्दी (Hindi)",
        scriptTag = "HI",
        region = "North & Central India",
        isoCode = "hi",
        scriptName = "Devanagari"
    ),
    BENGALI(
        code = "Bengali",
        nativeLabel = "বাংলা",
        fullDisplay = "বাংলা (Bengali)",
        scriptTag = "BN",
        region = "West Bengal & Bordering Balasore",
        isoCode = "bn",
        scriptName = "Bangla Lipi"
    ),
    TELUGU(
        code = "Telugu",
        nativeLabel = "తెలుగు",
        fullDisplay = "తెలుగు (Telugu)",
        scriptTag = "TE",
        region = "Andhra Pradesh & Telangana",
        isoCode = "te",
        scriptName = "Telugu Lipi"
    ),
    TAMIL(
        code = "Tamil",
        nativeLabel = "தமிழ்",
        fullDisplay = "தமிழ் (Tamil)",
        scriptTag = "TA",
        region = "Tamil Nadu & Puducherry",
        isoCode = "ta",
        scriptName = "Tamil Lipi"
    ),
    MARATHI(
        code = "Marathi",
        nativeLabel = "मराठी",
        fullDisplay = "मराठी (Marathi)",
        scriptTag = "MR",
        region = "Maharashtra & Goa",
        isoCode = "mr",
        scriptName = "Devanagari"
    ),
    GUJARATI(
        code = "Gujarati",
        nativeLabel = "ગુજરાતી",
        fullDisplay = "ગુજરાતી (Gujarati)",
        scriptTag = "GU",
        region = "Gujarat, Daman & Diu",
        isoCode = "gu",
        scriptName = "Gujarati Lipi"
    ),
    KANNADA(
        code = "Kannada",
        nativeLabel = "ಕನ್ನಡ",
        fullDisplay = "ಕನ್ನಡ (Kannada)",
        scriptTag = "KN",
        region = "Karnataka",
        isoCode = "kn",
        scriptName = "Kannada Lipi"
    ),
    MALAYALAM(
        code = "Malayalam",
        nativeLabel = "മലയാളം",
        fullDisplay = "മലയാളം (Malayalam)",
        scriptTag = "ML",
        region = "Kerala & Lakshadweep",
        isoCode = "ml",
        scriptName = "Malayalam Lipi"
    ),
    PUNJABI(
        code = "Punjabi",
        nativeLabel = "ਪੰਜਾਬੀ",
        fullDisplay = "ਪੰਜਾਬੀ (Punjabi)",
        scriptTag = "PA",
        region = "Punjab & Chandigarh",
        isoCode = "pa",
        scriptName = "Gurmukhi"
    ),
    ASSAMESE(
        code = "Assamese",
        nativeLabel = "অসমীয়া",
        fullDisplay = "অসমীয়া (Assamese)",
        scriptTag = "AS",
        region = "Assam & Northeast",
        isoCode = "as",
        scriptName = "Asamiya Lipi"
    ),
    MAITHILI(
        code = "Maithili",
        nativeLabel = "मैथिली",
        fullDisplay = "मैथिली (Maithili)",
        scriptTag = "MAI",
        region = "Bihar & Jharkhand",
        isoCode = "mai",
        scriptName = "Devanagari / Mithilakshar"
    ),
    SANTALI(
        code = "Santali",
        nativeLabel = "ᱥᱟᱱᱛᱟᱲᱤ",
        fullDisplay = "ᱥᱟᱱᱛᱟᱲᱤ (Santali)",
        scriptTag = "SAT",
        region = "Mayurbhanj, Balasore & Santhal Parganas",
        isoCode = "sat",
        scriptName = "Ol Chiki"
    ),
    SANSKRIT(
        code = "Sanskrit",
        nativeLabel = "संस्कृतम्",
        fullDisplay = "संस्कृतम् (Sanskrit)",
        scriptTag = "SA",
        region = "Classical / Pan-India",
        isoCode = "sa",
        scriptName = "Devanagari"
    ),
    URDU(
        code = "Urdu",
        nativeLabel = "اردو",
        fullDisplay = "اردو (Urdu)",
        scriptTag = "UR",
        region = "Pan-India Official",
        isoCode = "ur",
        scriptName = "Nastaliq / Perso-Arabic"
    ),
    NEPALI(
        code = "Nepali",
        nativeLabel = "नेपाली",
        fullDisplay = "नेपाली (Nepali)",
        scriptTag = "NE",
        region = "Sikkim, Darjeeling & Northern Hills",
        isoCode = "ne",
        scriptName = "Devanagari"
    ),
    KONKANI(
        code = "Konkani",
        nativeLabel = "कोंकणी",
        fullDisplay = "कोंकणी (Konkani)",
        scriptTag = "KOK",
        region = "Goa & Coastal Western India",
        isoCode = "kok",
        scriptName = "Devanagari"
    ),
    SINDHI(
        code = "Sindhi",
        nativeLabel = "सिन्धी",
        fullDisplay = "सिन्धी (Sindhi)",
        scriptTag = "SD",
        region = "Western India & Diaspora",
        isoCode = "sd",
        scriptName = "Devanagari / Arabic"
    ),
    DOGRI(
        code = "Dogri",
        nativeLabel = "डोगरी",
        fullDisplay = "डोगरी (Dogri)",
        scriptTag = "DOI",
        region = "Jammu Region & Himachal",
        isoCode = "doi",
        scriptName = "Devanagari"
    ),
    KASHMIRI(
        code = "Kashmiri",
        nativeLabel = "कॉशुर",
        fullDisplay = "कॉशुर (Kashmiri)",
        scriptTag = "KS",
        region = "Kashmir Valley",
        isoCode = "ks",
        scriptName = "Devanagari / Nastaliq"
    ),
    MANIPURI(
        code = "Manipuri",
        nativeLabel = "মৈতৈলোନ୍",
        fullDisplay = "মৈতৈলোନ୍ (Manipuri)",
        scriptTag = "MNI",
        region = "Manipur & Northeast",
        isoCode = "mni",
        scriptName = "Meitei Mayek"
    ),
    BODO(
        code = "Bodo",
        nativeLabel = "बड़ो",
        fullDisplay = "बड़ो (Bodo)",
        scriptTag = "BRX",
        region = "Bodoland & Assam",
        isoCode = "brx",
        scriptName = "Devanagari"
    );

    companion object {
        fun fromCode(code: String?): AppLanguage {
            return entries.firstOrNull {
                it.code.equals(code, ignoreCase = true) ||
                it.name.equals(code, ignoreCase = true) ||
                it.isoCode.equals(code, ignoreCase = true)
            } ?: ENGLISH
        }

        val allIndianLanguages: List<AppLanguage> get() = entries.toList()

        val popularRegionalLanguages: List<AppLanguage> get() = listOf(
            ODIA, ENGLISH, HINDI, BENGALI, SANTALI, TELUGU
        )
    }
}

data class Hotspot(
    val id: String,
    val name: String,
    val odiaName: String,
    val category: String,
    val description: String,
    val location: String,
    val rating: Double,
    val isFeatured: Boolean = false,
    val timing: String = "6:00 AM - 6:00 PM",
    val distanceKm: Double = 12.5,
    val detailedDescription: String = "",
    val historicalSignificance: String = "",
    val operatingHours: String = "",
    val reviewCount: Int = 1250,
    val latitude: Double = 21.4934,
    val longitude: Double = 86.9135
) {
    val effectiveDetailedDescription: String
        get() = detailedDescription.ifBlank { description }

    val effectiveOperatingHours: String
        get() = operatingHours.ifBlank { timing }
}

data class NewsArticle(
    val id: String,
    val title: String,
    val odiaTitle: String,
    val snippet: String,
    val odiaSnippet: String,
    val category: String,
    val timeAgo: String,
    val source: String = "Balasore Live Bureau",
    val isBookmarked: Boolean = false,
    val content: String = "",
    val odiaContent: String = ""
)

data class WeatherInfo(
    val tempCelsius: Int = 29,
    val condition: String = "Partly Cloudy",
    val highLow: String = "32° / 25°",
    val humidity: String = "74%",
    val windSpeedKmh: String = "18 km/h",
    val tideStatus: String = "Low Tide at 02:45 PM (Receding 5 km)",
    val seaCondition: String = "Safe for Beach Walkers",
    val cycloneAlert: String? = null,
    val feelsLikeCelsius: Int = 31,
    val windGustsKmh: String = "24 km/h",
    val uvIndex: Double = 6.0,
    val precipitationMm: Double = 0.0,
    val dataSource: String = "Open-Meteo Meteorological Service",
    val lastUpdatedTime: String = "Live • Just Now",
    val isLiveApi: Boolean = true,
    val weatherCode: Int = 2,
    val conditionIconUrl: String = "https://openweathermap.org/img/wn/02d@2x.png",
    val iconType: String = "partly_cloudy",
    val isDay: Boolean = true
)

data class BalasoreForecastHour(
    val timeLabel: String,
    val tempC: Int,
    val conditionEmoji: String,
    val popPercentage: Int,
    val windKmh: Int
)

data class BalasoreForecastDay(
    val id: String,
    val dayLabel: String,
    val dateFormatted: String,
    val odiaDayLabel: String,
    val highTempC: Int,
    val lowTempC: Int,
    val condition: String,
    val odiaCondition: String,
    val weatherIcon: String,
    val rainProbability: Int,
    val windSummary: String,
    val humidity: String,
    val uvIndex: String,
    val marineNotice: String,
    val hourlySlots: List<BalasoreForecastHour> = emptyList()
)

// 1. Chandipur Tidal Clock & Biodiversity
data class TidalClockData(
    val currentPhase: String = "Receding Sea (Low Tide)",
    val odiaPhase: String = "ଭଟ୍ଟା ସମୟ (ସମୁଦ୍ର ଅପସାରିତ)",
    val distanceRecededKm: Double = 4.6,
    val safeWalkMinutesRemaining: Int = 145, // Minutes until water starts rushing back
    val highTideTime: String = "08:15 PM",
    val lowTideTime: String = "02:30 PM",
    val tidalCoefficient: String = "Spring Tide (Extreme range)",
    val biodiversitySightings: List<String> = listOf(
        "Rare Horseshoe Crabs (Tachypleus gigas) active on outer sandbar",
        "Red Ghost Crabs near casuarina dunes",
        "Golden conch shells exposed at 3.5 km mark"
    )
)

// 2. DRDO ITR Coastal Advisories & Heritage
data class DrdoAdvisory(
    val id: String,
    val title: String,
    val odiaTitle: String,
    val dateOrTime: String,
    val status: String, // "CLEARED FOR FISHING", "COASTAL RESTRICTION", "UPCOMING TRIAL"
    val seaZone: String,
    val fishermenAdvisory: String,
    val sonicBoomWarning: Boolean = false,
    val heritageMilestone: String? = null
)

// 3. Remuna Khirachora Bhog & Darshan
data class TempleRitualInfo(
    val templeName: String = "Khirachora Gopinatha Temple, Remuna",
    val odiaTempleName: String = "କ୍ଷୀରଚୋରା ଗୋପୀନାଥ ମନ୍ଦିର, ରେମୁଣା",
    val nextBhogDistribution: String = "12:15 PM & 07:30 PM",
    val bhogName: String = "Sacred Amruta Keli (କ୍ଷୀର ଭୋଗ)",
    val bhogAvailabilityNote: String = "Fresh earthen pots (Kudua) prepared twice daily by temple servitors",
    val currentDarshanStatus: String = "Open for Devotees",
    val mangalaAlati: String = "05:30 AM",
    val sandhyaArati: String = "06:45 PM",
    val pahadaTime: String = "01:00 PM - 04:00 PM",
    val pilgrimageCircuit: List<String> = listOf(
        "Remuna Gopinatha (9 km)",
        "Panchalingeswar Spring (30 km)",
        "Emami Jagannath Temple (6 km)",
        "Bhusandeswar Shiva (82 km)"
    )
)

// 4. River Gauge & Flood Warning Telemetry
data class RiverGauge(
    val riverName: String,
    val odiaRiverName: String,
    val stationName: String,
    val currentLevelMeters: Double,
    val warningLevelMeters: Double,
    val dangerLevelMeters: Double,
    val status: String, // "NORMAL", "ALERT", "DANGER"
    val trend: String // "Rising", "Falling", "Steady"
)

data class CycloneShelter(
    val name: String,
    val block: String,
    val capacityPeople: Int,
    val nodalContact: String,
    val phone: String
)

// 5. Seafood Catch & Harbor Wholesale Rates
data class SeafoodCatch(
    val id: String,
    val name: String,
    val odiaName: String,
    val harbor: String,
    val priceRangeKg: String,
    val qualityGrade: String,
    val freshness: String = "Morning Harbor Landed",
    val peakSeason: String
)

// 6. Fakir Mohan Literary Trail & Daily Idiom
data class LiteraryTrailPoint(
    val id: String,
    val title: String,
    val odiaTitle: String,
    val location: String,
    val excerpt: String,
    val historicalContext: String
)

data class DailyBalasoreIdiom(
    val wordOrIdiom: String,
    val odiaScript: String,
    val meaning: String,
    val literaryWork: String,
    val funContext: String
)

data class EmergencyContact(
    val id: String,
    val name: String,
    val odiaName: String,
    val number: String,
    val category: String,
    val is24x7: Boolean = true,
    val description: String = ""
)

data class TransitSchedule(
    val id: String,
    val routeName: String,
    val origin: String,
    val destination: String,
    val time: String,
    val type: String, // Train or Bus
    val status: String = "On Time"
)
