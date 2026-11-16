package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.maps.BalasoreOsmDarkStyleHelper
import com.example.data.maps.BalasoreOsmOfflineTileProvider
import com.example.data.model.AppLanguage
import com.example.ui.theme.BentoSlate100
import com.example.ui.theme.BentoSlate600
import com.example.ui.theme.BentoSlate700
import com.example.ui.theme.BentoSlate800
import com.example.ui.theme.BentoSlate900
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.OceanBlue
import com.example.util.ShareHelper
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.util.Calendar
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class BalasoreMovieTheatreItem(
    val id: String,
    val nameEn: String,
    val nameOr: String,
    val addressEn: String,
    val addressOr: String,
    val soundAndScreenSpec: String,
    val rating: String,
    val nowShowingMovieEn: String,
    val nowShowingMovieOr: String,
    val languageCategory: String, // "Odia", "Hindi", "3D / Dolby"
    val showtimes: List<String>,
    val ticketFareRange: String,
    val amenitiesEn: String,
    val amenitiesOr: String,
    val boxOfficePhone: String,
    val latitude: Double,
    val longitude: Double
)

object BalasoreMovieTheatresRepository {
    const val BALASORE_CENTER_LAT = 21.4934
    const val BALASORE_CENTER_LON = 86.9135

    val theatres = listOf(
        BalasoreMovieTheatreItem(
            id = "odean_cinema",
            nameEn = "Odean Cinema",
            nameOr = "ଓଡ଼ିଅନ୍ ସିନେମା (Odean Cinema)",
            addressEn = "Sahadevkhunta, Balasore",
            addressOr = "ସହଦେବଖୁଣ୍ଟା, ବାଲେଶ୍ୱର",
            soundAndScreenSpec = "4K Digital • Dolby Atmos Surround",
            rating = "4.7",
            nowShowingMovieEn = "Now Showing: Baleswar Express (Odia Special) & Blockbuster Release",
            nowShowingMovieOr = "ଚାଲୁଥିବା ଚଳଚ୍ଚିତ୍ର: ବାଲେଶ୍ୱର ଏକ୍ସପ୍ରେସ୍ (ଓଡ଼ିଆ) ଓ ସୁପରହିଟ୍ ରିଲିଜ୍",
            languageCategory = "Odia",
            showtimes = listOf("11:00 AM", "02:15 PM", "05:45 PM", "08:45 PM"),
            ticketFareRange = "₹100 – ₹200 (Gold Balcony / Dress Circle)",
            amenitiesEn = "Near Sahadevkhunta Bus Stand & Station • Full AC • Parking & Snack Bar",
            amenitiesOr = "ସହଦେବଖୁଣ୍ଟା ବସ୍ ଷ୍ଟାଣ୍ଡ ନିକଟ • ସମ୍ପୂର୍ଣ୍ଣ AC • ପାର୍କିଂ ଓ ସ୍ନାକ୍ସ କାଉଣ୍ଟର",
            boxOfficePhone = "06782-262840",
            latitude = 21.5008,
            longitude = 86.9252
        ),
        BalasoreMovieTheatreItem(
            id = "moviex_ot_road",
            nameEn = "MovieX Multiplex",
            nameOr = "ମୁଭିଏକ୍ସ ମଲ୍ଟିପ୍ଲେକ୍ସ (MovieX)",
            addressEn = "O. T. Road, Balasore",
            addressOr = "ଓ.ଟି. ରୋଡ୍, ବାଲେଶ୍ୱର",
            soundAndScreenSpec = "4K Laser • RealD 3D • Dolby Atmos",
            rating = "4.8",
            nowShowingMovieEn = "Now Showing: Pan-India 3D Action, Hollywood & New Ollywood Premiere",
            nowShowingMovieOr = "ଚାଲୁଥିବା ଚଳଚ୍ଚିତ୍ର: ପ୍ୟାନ୍-ଇଣ୍ଡିଆ 3D ଆକ୍ସନ୍ ଓ ନୂଆ ଓଡ଼ିଆ ପ୍ରିମିୟର୍",
            languageCategory = "3D / Dolby",
            showtimes = listOf("10:45 AM", "02:00 PM", "05:30 PM", "09:00 PM"),
            ticketFareRange = "₹140 – ₹260 (Prime Recliner / Executive)",
            amenitiesEn = "Luxury Recliner Seats • Multi-Cuisine Food Court • O. T. Road Basement Parking",
            amenitiesOr = "ଲକ୍‌ଜୁରୀ ରିକ୍ଲାଇନର୍ ସିଟ୍ • ଫୁଡ୍ କୋର୍ଟ • ଓ.ଟି. ରୋଡ୍ ପାର୍କିଂ ସୁବିଧା",
            boxOfficePhone = "06782-265920",
            latitude = 21.4964,
            longitude = 86.9278
        ),
        BalasoreMovieTheatreItem(
            id = "baleswar_talkies",
            nameEn = "Baleswar Talkies (BT Multiplex)",
            nameOr = "ବାଲେଶ୍ୱର ଟକିଜ୍ (BT ମଲ୍ଟିପ୍ଲେକ୍ସ)",
            addressEn = "Cinema Chhak, Kacheri Road, Balasore",
            addressOr = "ସିନେମା ଛକ, କଚେରୀ ରୋଡ୍, ବାଲେଶ୍ୱର",
            soundAndScreenSpec = "4K Laser • Dolby Atmos",
            rating = "4.6",
            nowShowingMovieEn = "Now Showing: Baleswari Katha (Odia) & Pan-India Blockbuster",
            nowShowingMovieOr = "ଚାଲୁଥିବା ଚଳଚ୍ଚିତ୍ର: ବାଲେଶ୍ୱରୀ କଥା (ଓଡ଼ିଆ) ଓ ହିନ୍ଦୀ ବ୍ଲକବଷ୍ଟର",
            languageCategory = "Odia",
            showtimes = listOf("11:15 AM", "02:30 PM", "06:00 PM", "09:00 PM"),
            ticketFareRange = "₹120 – ₹220 (Recliner / Balcony)",
            amenitiesEn = "Central AC • EV & Car Parking • Popcorn & Sweet Corn Counter",
            amenitiesOr = "ସେଣ୍ଟ୍ରାଲ୍ AC • ପାର୍କିଂ ସୁବିଧା • ସ୍ନାକ୍ସ କାଉଣ୍ଟର",
            boxOfficePhone = "06782-262511",
            latitude = 21.4948,
            longitude = 86.9318
        ),
        BalasoreMovieTheatreItem(
            id = "subham_cineplex",
            nameEn = "Subham Cineplex (3D & Digital)",
            nameOr = "ଶୁଭମ୍ ସିନେପ୍ଲେକ୍ସ (3D ଓ ଡିଜିଟାଲ୍)",
            addressEn = "Near FM Circle, OT Road, Balasore",
            addressOr = "ଏଫ୍.ଏମ୍. ଗୋଲେଇ ନିକଟ, ଓ.ଟି. ରୋଡ୍, ବାଲେଶ୍ୱର",
            soundAndScreenSpec = "2K Digital • RealD 3D • Dolby 7.1",
            rating = "4.5",
            nowShowingMovieEn = "Now Showing: SamudraGarjna (3D Action) & Ollywood Family Drama",
            nowShowingMovieOr = "ଚାଲୁଥିବା ଚଳଚ୍ଚିତ୍ର: ସମୁଦ୍ର ଗର୍ଜନ (3D) ଓ ଓଡ଼ିଆ ପାରିବାରିକ ଚଳଚ୍ଚିତ୍ର",
            languageCategory = "3D / Dolby",
            showtimes = listOf("11:30 AM", "02:45 PM", "06:15 PM", "09:15 PM"),
            ticketFareRange = "₹110 – ₹200 (Executive / Silver)",
            amenitiesEn = "3D Glasses Sanitized • Two-Wheeler & Car Bay • Online QR Entry",
            amenitiesOr = "3D ଚଷମା • ବାଇକ୍ ଓ କାର୍ ପାର୍କିଂ • QR ଟିକେଟ୍ ଏଣ୍ଟ୍ରି",
            boxOfficePhone = "06782-264190",
            latitude = 21.4982,
            longitude = 86.9245
        ),
        BalasoreMovieTheatreItem(
            id = "samrat_cinema",
            nameEn = "Samrat Cinema Hall",
            nameOr = "ସମ୍ରାଟ ସିନେମା ହଲ୍",
            addressEn = "Station Road – Motiganj Link, Balasore",
            addressOr = "ଷ୍ଟେସନ ରୋଡ୍ – ମୋତିଗଞ୍ଜ ଲିଙ୍କ୍, ବାଲେଶ୍ୱର",
            soundAndScreenSpec = "Dolby Digital 7.1 • Wide Silver Screen",
            rating = "4.3",
            nowShowingMovieEn = "Now Showing: Chandipur Express (Hindi) & Superhit Odia Matinee",
            nowShowingMovieOr = "ଚାଲୁଥିବା ଚଳଚ୍ଚିତ୍ର: ଚାନ୍ଦିପୁର ଏକ୍ସପ୍ରେସ୍ (ହିନ୍ଦୀ) ଓ ଓଡ଼ିଆ ମାଟିନି ଶୋ",
            languageCategory = "Hindi",
            showtimes = listOf("11:00 AM", "02:15 PM", "05:45 PM", "08:45 PM"),
            ticketFareRange = "₹90 – ₹160 (Balcony / Dress Circle)",
            amenitiesEn = "500m from Balasore Railway Station • Family Balcony • Tea & Samosa Stall",
            amenitiesOr = "ବାଲେଶ୍ୱର ରେଳ ଷ୍ଟେସନ ନିକଟ • ଫ୍ୟାମିଲି ବାଲକୋନି • ଜଳଖିଆ ଷ୍ଟଲ୍",
            boxOfficePhone = "06782-263305",
            latitude = 21.5011,
            longitude = 86.9204
        ),
        BalasoreMovieTheatreItem(
            id = "soro_cine_palace",
            nameEn = "Soro Cine Palace (Digital 2K)",
            nameOr = "ସୋର ସିନେ ପ୍ୟାଲେସ୍ (ଡିଜିଟାଲ୍ 2K)",
            addressEn = "College Chhak, NH-16 Corridor, Soro",
            addressOr = "କଲେଜ ଛକ, NH-16 ପାଖ, ସୋର, ବାଲେଶ୍ୱର",
            soundAndScreenSpec = "UFO Digital 2K • Surround Sound",
            rating = "4.4",
            nowShowingMovieEn = "Now Showing: Panchalingeswar Mahima (Odia) & Evening Action Show",
            nowShowingMovieOr = "ଚାଲୁଥିବା ଚଳଚ୍ଚିତ୍ର: ପଞ୍ଚଲିଙ୍ଗେଶ୍ୱର ମହିମା (ଓଡ଼ିଆ) ଓ ସନ୍ଧ୍ୟା ଶୋ",
            languageCategory = "Odia",
            showtimes = listOf("11:30 AM", "02:30 PM", "06:00 PM", "08:45 PM"),
            ticketFareRange = "₹80 – ₹150 (AC Balcony / First Class)",
            amenitiesEn = "AC Balcony • Easy NH-16 Access • Famous Soro Ksheera Gaja Nearby",
            amenitiesOr = "AC ବାଲକୋନି • NH-16 ସୁବିଧା • ପ୍ରସିଦ୍ଧ ସୋର କ୍ଷୀର ଗଜା ନିକଟରେ",
            boxOfficePhone = "06788-221450",
            latitude = 21.2884,
            longitude = 86.6892
        ),
        BalasoreMovieTheatreItem(
            id = "jaleswar_talkies",
            nameEn = "Jaleswar Talkies & Multiplex Screen",
            nameOr = "ଜଳେଶ୍ୱର ଟକିଜ୍ ଓ ସିନେମା ସ୍କ୍ରିନ୍",
            addressEn = "Station Bazar, Jaleswar, Balasore",
            addressOr = "ଷ୍ଟେସନ ବଜାର, ଜଳେଶ୍ୱର, ବାଲେଶ୍ୱର",
            soundAndScreenSpec = "2K Projection • Dolby Surround",
            rating = "4.2",
            nowShowingMovieEn = "Now Showing: Subarnarekha (Odia / Bengali Bilingual) & Hindi Hit",
            nowShowingMovieOr = "ଚାଲୁଥିବା ଚଳଚ୍ଚିତ୍ର: ସୁବର୍ଣ୍ଣରେଖା (ଓଡ଼ିଆ) ଓ ହିନ୍ଦୀ ସୁପରହିଟ୍",
            languageCategory = "Hindi",
            showtimes = listOf("12:00 PM", "03:00 PM", "06:00 PM", "09:00 PM"),
            ticketFareRange = "₹80 – ₹140 (Balcony / Stall)",
            amenitiesEn = "Air Cooled & AC Balcony • Cycle & Bike Stand • Family Seating",
            amenitiesOr = "AC ବାଲକୋନି • ବାଇକ୍ ପାର୍କିଂ • ପାରିବାରିକ ସିଟ୍",
            boxOfficePhone = "06781-222318",
            latitude = 21.7975,
            longitude = 87.2225
        )
    )

    fun distanceKmBetween(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    fun distanceKmFromBalasoreCenter(lat: Double, lon: Double): Double =
        distanceKmBetween(BALASORE_CENTER_LAT, BALASORE_CENTER_LON, lat, lon)

    fun formatDistanceFromUser(distKm: Double, isOdia: Boolean): String {
        val formattedKm = if (distKm < 1.0) {
            "${(distKm * 1000).toInt()} m"
        } else {
            String.format(Locale.US, "%.1f km", distKm)
        }
        val driveMins = (distKm * 2.5).toInt().coerceAtLeast(2)
        return if (isOdia) {
            "ଆପଣଙ୍କଠାରୁ $formattedKm (~$driveMins ମିନିଟ୍)"
        } else {
            "$formattedKm away (~$driveMins min)"
        }
    }

    /**
     * Parses a 12-hour showtime string such as "10:45 AM" or "02:15 PM" into minutes from midnight.
     */
    fun parseShowtimeMinutesFromMidnight(showtime: String): Int {
        return try {
            val cleaned = showtime.trim().uppercase(Locale.US)
            val isPm = cleaned.endsWith("PM")
            val isAm = cleaned.endsWith("AM")
            val timePart = cleaned.removeSuffix("PM").removeSuffix("AM").trim()
            val parts = timePart.split(":")
            var hour = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: 12
            val minute = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: 0
            if (isPm && hour < 12) hour += 12
            if (isAm && hour == 12) hour = 0
            hour * 60 + minute
        } catch (_: Exception) {
            12 * 60
        }
    }

    fun currentLocalMinutesOfDay(): Int {
        val cal = Calendar.getInstance()
        return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
    }

    /**
     * Returns the next upcoming showtime string and the number of minutes until it starts.
     */
    fun minutesUntilNextShowtime(
        theatre: BalasoreMovieTheatreItem,
        nowMinutes: Int = currentLocalMinutesOfDay()
    ): Pair<String, Int> {
        if (theatre.showtimes.isEmpty()) return "" to Int.MAX_VALUE
        val parsed = theatre.showtimes.map { it to parseShowtimeMinutesFromMidnight(it) }
        val upcomingToday = parsed.firstOrNull { it.second >= nowMinutes }
        return if (upcomingToday != null) {
            upcomingToday.first to (upcomingToday.second - nowMinutes)
        } else {
            val firstTomorrow = parsed.minByOrNull { it.second } ?: parsed.first()
            firstTomorrow.first to ((24 * 60 - nowMinutes) + firstTomorrow.second)
        }
    }

    fun formatNextShowBadge(
        theatre: BalasoreMovieTheatreItem,
        isOdia: Boolean,
        nowMinutes: Int = currentLocalMinutesOfDay()
    ): String {
        val (nextShow, minsUntil) = minutesUntilNextShowtime(theatre, nowMinutes)
        if (nextShow.isEmpty()) return ""
        val timeDiffLabel = when {
            minsUntil <= 0 -> if (isOdia) "ଏବେ ଆରମ୍ଭ" else "Starting now"
            minsUntil < 60 -> if (isOdia) "${minsUntil} ମି.ରେ" else "in ${minsUntil}m"
            else -> {
                val hrs = minsUntil / 60
                val remMins = minsUntil % 60
                if (remMins == 0) {
                    if (isOdia) "${hrs} ଘଣ୍ଟାରେ" else "in ${hrs}h"
                } else {
                    if (isOdia) "${hrs}ଘ ${remMins}ମି" else "in ${hrs}h ${remMins}m"
                }
            }
        }
        return if (isOdia) {
            "ପରବର୍ତ୍ତୀ ଶୋ’: $nextShow ($timeDiffLabel)"
        } else {
            "Next: $nextShow ($timeDiffLabel)"
        }
    }

    fun resolveUserLocationOrDefault(context: Context): Pair<Double, Double> {
        return try {
            val hasFine = androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            val hasCoarse = androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (hasFine || hasCoarse) {
                val lm = context.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager
                val loc = lm?.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                    ?: lm?.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
                if (loc != null && loc.latitude in 20.5..22.5 && loc.longitude in 86.0..88.0) {
                    return loc.latitude to loc.longitude
                }
            }
            BALASORE_CENTER_LAT to BALASORE_CENTER_LON
        } catch (_: Exception) {
            BALASORE_CENTER_LAT to BALASORE_CENTER_LON
        }
    }
}

/**
 * Interactive local Balasore Map View with live OSMDroid markers pinpointing the exact GPS
 * locations of all cinemas (Odean Cinema Sahadevkhunta, MovieX O. T. Road, Baleswar Talkies,
 * Subham Cineplex, Samrat Cinema, Soro Cine Palace, and Jaleswar Talkies).
 */
@Composable
fun BalasoreMovieTheatresInteractiveMapSection(
    theatres: List<BalasoreMovieTheatreItem>,
    selectedTheatre: BalasoreMovieTheatreItem,
    onSelectTheatre: (BalasoreMovieTheatreItem) -> Unit,
    userLat: Double = BalasoreMovieTheatresRepository.BALASORE_CENTER_LAT,
    userLon: Double = BalasoreMovieTheatresRepository.BALASORE_CENTER_LON,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isOdia = language == AppLanguage.ODIA
    val appCtx = context.applicationContext ?: context

    val tileProvider = remember {
        BalasoreOsmOfflineTileProvider(appCtx)
    }
    var osmMapView by remember { mutableStateOf<MapView?>(null) }
    var currentZoom by remember { mutableDoubleStateOf(15.2) }
    var isDarkMapStyle by remember { mutableStateOf(true) }

    androidx.compose.runtime.LaunchedEffect(selectedTheatre.id) {
        osmMapView?.controller?.apply {
            if (currentZoom < 15.0) {
                currentZoom = 15.8
                setZoom(currentZoom)
            }
            animateTo(GeoPoint(selectedTheatre.latitude, selectedTheatre.longitude))
        }
        osmMapView?.invalidate()
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                osmMapView?.onPause()
                osmMapView?.onDetach()
                tileProvider.detach()
            } catch (_: Exception) {}
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("movie_theatres_interactive_map_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMapStyle) BentoSlate900 else Color.White
        ),
        border = BorderStroke(
            1.2.dp,
            if (isDarkMapStyle) OceanBlue.copy(alpha = 0.65f) else Color(0xFF9333EA).copy(alpha = 0.45f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Map Header with Live Pin Count, Dark Mode Style Switch & Reset Overview Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(OceanBlue, Color(0xFF7E22CE))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "Balasore Cinema Map",
                            tint = Color.White,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isOdia) {
                                "🗺️ ବାଲେଶ୍ୱର ସିନେମା ହଲ୍ ଇଣ୍ଟରାକ୍ଟିଭ୍ ମ୍ୟାପ୍"
                            } else {
                                "🗺️ Balasore Cinemas Interactive Map"
                            },
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = if (isDarkMapStyle) Color.White else BentoSlate900
                        )
                        Text(
                            text = if (isOdia) {
                                "${theatres.size}ଟି ସିନେମା ମାର୍କର • ${if (isDarkMapStyle) "ଡାର୍କ ସ୍ଲେଟ୍ ଥିମ୍ ସକ୍ରିୟ" else "ଷ୍ଟାଣ୍ଡାର୍ଡ ଥିମ୍"}"
                            } else {
                                "${theatres.size} Cinema Pins • ${if (isDarkMapStyle) "Custom Slate-Ocean Dark Style" else "Standard Day Style"}"
                            },
                            fontSize = 10.5.sp,
                            color = if (isDarkMapStyle) Color(0xFF94A3B8) else BentoSlate600
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Custom Dark Mode Map Style Toggle Button
                    Surface(
                        onClick = {
                            isDarkMapStyle = !isDarkMapStyle
                            osmMapView?.let { mv ->
                                BalasoreOsmDarkStyleHelper.applyMapColorStyle(mv, isDarkMapStyle)
                                mv.invalidate()
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isDarkMapStyle) BentoSlate800 else Color(0xFFF1F5F9),
                        border = BorderStroke(
                            1.dp,
                            if (isDarkMapStyle) Color(0xFF38BDF8).copy(alpha = 0.65f) else Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier.testTag("movie_map_style_toggle_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = if (isDarkMapStyle) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = "Toggle OSM Map Dark Style",
                                tint = if (isDarkMapStyle) Color(0xFF38BDF8) else Color(0xFFF59E0B),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isDarkMapStyle) {
                                    if (isOdia) "ଡାର୍କ" else "Dark"
                                } else {
                                    if (isOdia) "ଲାଇଟ୍" else "Light"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkMapStyle) Color(0xFFE0F2FE) else BentoSlate900
                            )
                        }
                    }

                    Surface(
                        onClick = {
                            currentZoom = 14.3
                            osmMapView?.controller?.apply {
                                setZoom(currentZoom)
                                animateTo(GeoPoint(21.4980, 86.9265))
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isDarkMapStyle) BentoSlate800 else Color(0xFFF3E8FF),
                        border = BorderStroke(
                            1.dp,
                            if (isDarkMapStyle) Color(0xFF7E22CE) else Color(0xFFD8B4FE)
                        ),
                        modifier = Modifier.testTag("movie_map_fit_all_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Center on Balasore Town Cinemas",
                                tint = if (isDarkMapStyle) Color(0xFFC084FC) else Color(0xFF7E22CE),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isOdia) "ସହର କେନ୍ଦ୍ର" else "Town View",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkMapStyle) Color.White else Color(0xFF6B21A8)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Cinema Marker Pin Selector Bar
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(vertical = 2.dp),
                modifier = Modifier.testTag("movie_map_pin_selector_row")
            ) {
                items(theatres, key = { it.id }) { cinema ->
                    val isSelected = cinema.id == selectedTheatre.id
                    Surface(
                        onClick = {
                            onSelectTheatre(cinema)
                            currentZoom = 16.2
                            osmMapView?.controller?.apply {
                                setZoom(currentZoom)
                                animateTo(GeoPoint(cinema.latitude, cinema.longitude))
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = when {
                            isSelected -> OceanBlue
                            isDarkMapStyle -> BentoSlate800
                            else -> Color(0xFFF8FAFC)
                        },
                        border = BorderStroke(
                            1.dp,
                            when {
                                isSelected -> Color(0xFF38BDF8)
                                isDarkMapStyle -> Color(0xFF334155)
                                else -> Color(0xFFD8B4FE)
                            }
                        ),
                        modifier = Modifier.testTag("movie_map_pin_chip_${cinema.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = when {
                                    isSelected -> Color.White
                                    isDarkMapStyle -> Color(0xFF38BDF8)
                                    else -> Color(0xFF7E22CE)
                                },
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            val chipDistKm = BalasoreMovieTheatresRepository.distanceKmBetween(
                                userLat,
                                userLon,
                                cinema.latitude,
                                cinema.longitude
                            )
                            Text(
                                text = "${if (isOdia) cinema.nameOr.substringBefore(" (") else cinema.nameEn.substringBefore(" (")} (${String.format(Locale.US, "%.1f km", chipDistKm)})",
                                fontSize = 10.5.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                color = if (isSelected || isDarkMapStyle) Color.White else BentoSlate900
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Interactive OSMDroid Map Container with Custom Dark Mode Palette, Cinema Markers & Floating Zoom Controls
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(235.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isDarkMapStyle) BentoSlate900 else Color(0xFFE2E8F0))
                    .testTag("movie_theatres_osm_map_box")
            ) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("movie_theatres_osm_map_view"),
                    factory = { ctx ->
                        Configuration.getInstance().userAgentValue = ctx.packageName
                        MapView(ctx, tileProvider).apply {
                            osmMapView = this
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                            BalasoreOsmDarkStyleHelper.applyMapColorStyle(this, isDarkMapStyle)
                            controller.setZoom(currentZoom)
                            controller.setCenter(GeoPoint(selectedTheatre.latitude, selectedTheatre.longitude))
                        }
                    },
                    update = { mapView ->
                        BalasoreOsmDarkStyleHelper.applyMapColorStyle(mapView, isDarkMapStyle)
                        mapView.overlays.clear()

                        // Add a reference marker for User's Location in Balasore
                        val centerMarker = Marker(mapView).apply {
                            position = GeoPoint(userLat, userLon)
                            title = if (isOdia) "ଆପଣଙ୍କ ଅବସ୍ଥାନ (Your Location)" else "Your Location (Balasore)"
                            snippet = String.format(Locale.US, "GPS: %.4f° N, %.4f° E", userLat, userLon)
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        }
                        mapView.overlays.add(centerMarker)

                        // Add interactive markers for all Balasore movie theatres
                        theatres.forEach { cinema ->
                            val distKm = BalasoreMovieTheatresRepository.distanceKmBetween(
                                userLat,
                                userLon,
                                cinema.latitude,
                                cinema.longitude
                            )
                            val marker = Marker(mapView).apply {
                                position = GeoPoint(cinema.latitude, cinema.longitude)
                                title = "🎬 ${cinema.nameEn} (${cinema.rating}★)"
                                snippet = "${cinema.addressEn} • ${String.format(Locale.US, "%.1f km from you", distKm)} • ☎️ ${cinema.boxOfficePhone}"
                                subDescription = "${cinema.nowShowingMovieEn} | Showtimes: ${cinema.showtimes.joinToString(", ")}"
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                setOnMarkerClickListener { m, mv ->
                                    onSelectTheatre(cinema)
                                    mv.controller.animateTo(m.position)
                                    m.showInfoWindow()
                                    true
                                }
                            }
                            mapView.overlays.add(marker)
                            if (cinema.id == selectedTheatre.id) {
                                marker.showInfoWindow()
                            }
                        }
                        mapView.invalidate()
                    }
                )

                // Top-left Live GPS Coordinate HUD Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BentoSlate900.copy(alpha = 0.88f),
                    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.45f)),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "📍 ${selectedTheatre.nameEn.substringBefore(" (")} • ${
                            String.format(
                                Locale.US,
                                "%.4f° N, %.4f° E",
                                selectedTheatre.latitude,
                                selectedTheatre.longitude
                            )
                        }",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE0F2FE),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Bottom-end Zoom In / Zoom Out Controls
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        onClick = {
                            currentZoom = (currentZoom + 1.0).coerceAtMost(18.0)
                            osmMapView?.controller?.setZoom(currentZoom)
                        },
                        shape = CircleShape,
                        color = if (isDarkMapStyle) BentoSlate800.copy(alpha = 0.94f) else Color.White.copy(alpha = 0.94f),
                        border = BorderStroke(
                            1.dp,
                            if (isDarkMapStyle) Color(0xFF38BDF8).copy(alpha = 0.5f) else Color.Transparent
                        ),
                        shadowElevation = 3.dp,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("movie_map_zoom_in_btn")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Zoom In Cinema Map",
                                tint = if (isDarkMapStyle) Color.White else BentoSlate900,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Surface(
                        onClick = {
                            currentZoom = (currentZoom - 1.0).coerceAtLeast(10.0)
                            osmMapView?.controller?.setZoom(currentZoom)
                        },
                        shape = CircleShape,
                        color = if (isDarkMapStyle) BentoSlate800.copy(alpha = 0.94f) else Color.White.copy(alpha = 0.94f),
                        border = BorderStroke(
                            1.dp,
                            if (isDarkMapStyle) Color(0xFF38BDF8).copy(alpha = 0.5f) else Color.Transparent
                        ),
                        shadowElevation = 3.dp,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("movie_map_zoom_out_btn")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Zoom Out Cinema Map",
                                tint = if (isDarkMapStyle) Color.White else BentoSlate900,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Selected Cinema Pin Details Card with Distance-from-User & Direct Call Button
            val distFromUser = BalasoreMovieTheatresRepository.distanceKmBetween(
                userLat,
                userLon,
                selectedTheatre.latitude,
                selectedTheatre.longitude
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFAF5FF),
                border = BorderStroke(1.dp, Color(0xFFD8B4FE)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("movie_map_selected_cinema_card")
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🎬 ${if (isOdia) selectedTheatre.nameOr else selectedTheatre.nameEn}",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF581C87)
                            )
                            Text(
                                text = "📍 ${if (isOdia) selectedTheatre.addressOr else selectedTheatre.addressEn}",
                                fontSize = 10.5.sp,
                                color = BentoSlate700
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFE0F2FE),
                                border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f)),
                                modifier = Modifier.testTag("movie_map_selected_distance_badge")
                            ) {
                                Text(
                                    text = "🧭 ${BalasoreMovieTheatresRepository.formatDistanceFromUser(distFromUser, isOdia)}",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF0369A1),
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Direct Call Inquiry Button in Selected Pin Details
                            Surface(
                                onClick = {
                                    Toast.makeText(
                                        context,
                                        "Calling ${selectedTheatre.nameEn} Box Office (${selectedTheatre.boxOfficePhone})...",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    runCatching {
                                        context.startActivity(
                                            Intent(Intent.ACTION_DIAL, Uri.parse("tel:${selectedTheatre.boxOfficePhone}")).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = EmeraldGreen,
                                modifier = Modifier.testTag("movie_map_selected_call_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = "Call Selected Cinema Box Office",
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isOdia) "କଲ୍" else "Call",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            // Directions Button
                            Surface(
                                onClick = {
                                    runCatching {
                                        val geoUri = Uri.parse(
                                            "geo:${selectedTheatre.latitude},${selectedTheatre.longitude}?q=${selectedTheatre.latitude},${selectedTheatre.longitude}(${Uri.encode(selectedTheatre.nameEn)})"
                                        )
                                        context.startActivity(
                                            Intent(Intent.ACTION_VIEW, geoUri).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = OceanBlue,
                                modifier = Modifier.testTag("movie_map_selected_directions_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Directions,
                                        contentDescription = "Get Directions to Selected Cinema",
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isOdia) "ଦିଗନିର୍ଦ୍ଦେଶ" else "Directions",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "🕒 ${if (isOdia) "ଶୋ’ ସମୟ:" else "Showtimes:"} ${selectedTheatre.showtimes.joinToString(" • ")} | ☎️ ${if (isOdia) "ବକ୍ସ ଅଫିସ୍:" else "Box Office:"} ${selectedTheatre.boxOfficePhone}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF6B21A8)
                    )
                }
            }
        }
    }
}

@Composable
fun BalasoreMovieTheatresCard(
    language: AppLanguage,
    onOpenFullSheet: (() -> Unit)? = null,
    showInteractiveMapInitially: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isOdia = language == AppLanguage.ODIA
    val prefs = remember {
        context.getSharedPreferences("balasore_movie_theatres_prefs", Context.MODE_PRIVATE)
    }

    val userCoords = remember {
        BalasoreMovieTheatresRepository.resolveUserLocationOrDefault(context)
    }
    val userLat = userCoords.first
    val userLon = userCoords.second

    var selectedFilter by remember { mutableStateOf("All") }
    var selectedSortOption by remember { mutableStateOf("Nearest") } // "Nearest", "Highest Rated", "Showing Soon"
    var isMapExpanded by remember { mutableStateOf(showInteractiveMapInitially) }
    var selectedMapTheatre by remember {
        mutableStateOf(BalasoreMovieTheatresRepository.theatres.first())
    }
    var selectedShowtimesByTheatre by remember {
        mutableStateOf(
            BalasoreMovieTheatresRepository.theatres.associate { it.id to it.showtimes[2] }
        )
    }
    var reservedAlertIds by remember {
        val initial = BalasoreMovieTheatresRepository.theatres
            .map { it.id }
            .filter { prefs.getBoolean("reserved_${it}", false) }
            .toSet()
        mutableStateOf(initial)
    }

    val sortOptions = listOf("Nearest", "Highest Rated", "Showing Soon")
    val filterOptions = listOf("All", "Nearest", "Highest Rated", "Showing Soon", "Odia", "Hindi", "3D / Dolby")
    val nowMinutes = remember { BalasoreMovieTheatresRepository.currentLocalMinutesOfDay() }

    val filteredTheatres = remember(selectedFilter, selectedSortOption, userLat, userLon, nowMinutes) {
        val base = when (selectedFilter) {
            "All", "Nearest", "Highest Rated", "Showing Soon" -> BalasoreMovieTheatresRepository.theatres
            else -> BalasoreMovieTheatresRepository.theatres.filter {
                it.languageCategory.equals(selectedFilter, ignoreCase = true) ||
                    it.soundAndScreenSpec.contains(selectedFilter.substringBefore(" "), ignoreCase = true)
            }
        }
        when (selectedSortOption) {
            "Nearest" -> base.sortedBy {
                BalasoreMovieTheatresRepository.distanceKmBetween(userLat, userLon, it.latitude, it.longitude)
            }
            "Highest Rated" -> base.sortedByDescending {
                it.rating.toDoubleOrNull() ?: 0.0
            }
            "Showing Soon" -> base.sortedBy {
                BalasoreMovieTheatresRepository.minutesUntilNextShowtime(it, nowMinutes).second
            }
            else -> base
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("balasore_movie_theatres_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF5FF)),
        border = BorderStroke(1.dp, Color(0xFFC084FC).copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(15.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF7E22CE),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Movie,
                                contentDescription = "Movie Theatres",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isOdia) {
                                "🎬 ବାଲେଶ୍ୱର ସିନେମା ହଲ୍ ଓ ଶୋ’ ସମୟ (Movie Theatres)"
                            } else {
                                "🎬 Balasore Movie Theatres & Showtimes"
                            },
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = BentoSlate900
                        )
                        Text(
                            text = if (isOdia) {
                                "ଓଡ଼ିଅନ୍ ସିନେମା (ସହଦେବଖୁଣ୍ଟା), ମୁଭିଏକ୍ସ (ଓ.ଟି. ରୋଡ୍), ବାଲେଶ୍ୱର ଟକିଜ୍ ଓ ଶୁଭମ୍"
                            } else {
                                "Odean Cinema (Sahadevkhunta), MovieX (O. T. Road), Baleswar Talkies & Subham"
                            },
                            fontSize = 11.sp,
                            color = Color(0xFF6B21A8)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        onClick = { isMapExpanded = !isMapExpanded },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isMapExpanded) Color(0xFF0284C7) else Color.White,
                        border = BorderStroke(1.dp, Color(0xFF0284C7)),
                        modifier = Modifier.testTag("toggle_movie_theatres_map_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = "Toggle Cinema Map View",
                                tint = if (isMapExpanded) Color.White else Color(0xFF0284C7),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isOdia) "ମ୍ୟାପ୍" else "Map",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isMapExpanded) Color.White else Color(0xFF0284C7)
                            )
                        }
                    }

                    if (onOpenFullSheet != null) {
                        Surface(
                            onClick = onOpenFullSheet,
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF7E22CE),
                            modifier = Modifier.testTag("open_movie_theatres_sheet_btn")
                        ) {
                            Text(
                                text = if (isOdia) "ସବୁ ଦେଖନ୍ତୁ" else "Full Hub",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Category & Quick Filter Chips Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 2.dp),
                modifier = Modifier.testTag("movie_theatres_filter_row")
            ) {
                items(filterOptions) { option ->
                    val isSortChipInRow = option in sortOptions
                    val isSelected = if (isSortChipInRow) {
                        selectedFilter == option || (selectedFilter == "All" && selectedSortOption == option)
                    } else {
                        selectedFilter == option
                    }
                    val label = if (isOdia) {
                        when (option) {
                            "All" -> "ସମସ୍ତ ହଲ୍ (${BalasoreMovieTheatresRepository.theatres.size})"
                            "Nearest" -> "🧭 ନିକଟତମ (Nearest)"
                            "Highest Rated" -> "⭐ ସର୍ବୋଚ୍ଚ ରେଟିଂ (Highest Rated)"
                            "Showing Soon" -> "🕒 ଶୀଘ୍ର ଆରମ୍ଭ (Showing Soon)"
                            "Odia" -> "ଓଡ଼ିଆ ଚଳଚ୍ଚିତ୍ର"
                            "Hindi" -> "ହିନ୍ଦୀ / ପ୍ୟାନ୍-ଇଣ୍ଡିଆ"
                            "3D / Dolby" -> "3D / ଡଲବି ଆଟମସ୍"
                            else -> option
                        }
                    } else {
                        when (option) {
                            "All" -> "All (${BalasoreMovieTheatresRepository.theatres.size})"
                            "Nearest" -> "🧭 Nearest"
                            "Highest Rated" -> "⭐ Highest Rated"
                            "Showing Soon" -> "🕒 Showing Soon"
                            "Odia" -> "Odia (Ollywood)"
                            "Hindi" -> "Hindi / Pan-India"
                            else -> option
                        }
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (option in sortOptions) {
                                selectedSortOption = option
                                selectedFilter = option
                            } else {
                                selectedFilter = option
                            }
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF7E22CE),
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = Color(0xFF6B21A8)
                        ),
                        modifier = Modifier.testTag("movie_filter_chip_${option.lowercase().replace(" ", "_")}")
                    )
                }
            }

            // Interactive Local Balasore Cinema Map View with Markers
            AnimatedVisibility(
                visible = isMapExpanded,
                enter = fadeIn(animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)) +
                    expandVertically(animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)),
                exit = fadeOut(animationSpec = tween(durationMillis = 250)) +
                    shrinkVertically(animationSpec = tween(durationMillis = 250))
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    BalasoreMovieTheatresInteractiveMapSection(
                        theatres = filteredTheatres.ifEmpty { BalasoreMovieTheatresRepository.theatres },
                        selectedTheatre = selectedMapTheatre,
                        onSelectTheatre = { selectedMapTheatre = it },
                        userLat = userLat,
                        userLon = userLon,
                        language = language
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dedicated Sort Filter/Chip Bar at the Top of the Movie Theatre List
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF3E8FF).copy(alpha = 0.75f),
                border = BorderStroke(1.dp, Color(0xFFD8B4FE)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("movie_theatres_sort_chip_bar")
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Sort Movie Theatres",
                                tint = Color(0xFF6B21A8),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (isOdia) "ସିନେମା ତାଲିକା ସଜାନ୍ତୁ (Sort By):" else "Sort Cinemas By:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF581C87)
                            )
                        }
                        Text(
                            text = when (selectedSortOption) {
                                "Nearest" -> if (isOdia) "ଦୂରତା ଅନୁସାରେ" else "Sorted by Distance"
                                "Highest Rated" -> if (isOdia) "ରେଟିଂ ଅନୁସାରେ" else "Sorted by Rating ★"
                                "Showing Soon" -> if (isOdia) "ପରବର୍ତ୍ତୀ ଶୋ’ ଅନୁସାରେ" else "Sorted by Next Showtime"
                                else -> ""
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF7E22CE),
                            modifier = Modifier.testTag("movie_theatres_active_sort_label")
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("movie_theatres_sort_chips_row")
                    ) {
                        items(sortOptions) { sortOption ->
                            val isSortSelected = selectedSortOption == sortOption
                            val chipIcon = when (sortOption) {
                                "Nearest" -> Icons.Default.LocationOn
                                "Highest Rated" -> Icons.Default.Star
                                else -> Icons.Default.AccessTime
                            }
                            val chipTitle = if (isOdia) {
                                when (sortOption) {
                                    "Nearest" -> "ନିକଟତମ (Nearest)"
                                    "Highest Rated" -> "ସର୍ବୋଚ୍ଚ ରେଟିଂ (Highest Rated)"
                                    "Showing Soon" -> "ଶୀଘ୍ର ଆରମ୍ଭ (Showing Soon)"
                                    else -> sortOption
                                }
                            } else {
                                sortOption
                            }

                            FilterChip(
                                selected = isSortSelected,
                                onClick = {
                                    selectedSortOption = sortOption
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = chipIcon,
                                        contentDescription = null,
                                        tint = if (isSortSelected) Color.White else Color(0xFF7E22CE),
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = chipTitle,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSortSelected) FontWeight.ExtraBold else FontWeight.SemiBold
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF0284C7),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White,
                                    labelColor = Color(0xFF581C87)
                                ),
                                modifier = Modifier.testTag(
                                    "movie_sort_chip_${sortOption.lowercase().replace(" ", "_")}"
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val listFadeAnim = remember { Animatable(0f) }
            LaunchedEffect(selectedFilter, selectedSortOption) {
                listFadeAnim.snapTo(0f)
                listFadeAnim.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = 420,
                        easing = FastOutSlowInEasing
                    )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = listFadeAnim.value
                    }
                    .testTag("movie_theatres_list_container")
            ) {
                filteredTheatres.forEachIndexed { index, theatre ->
                    key(theatre.id) {
                        val itemAlphaAnim = remember(selectedFilter, selectedSortOption) { Animatable(0f) }
                        val itemSlideAnim = remember(selectedFilter, selectedSortOption) { Animatable(24f) }
                        LaunchedEffect(theatre.id, selectedFilter, selectedSortOption) {
                            val staggerDelay = (index * 65).coerceAtMost(360)
                            kotlinx.coroutines.coroutineScope {
                                launch {
                                    itemAlphaAnim.animateTo(
                                        targetValue = 1f,
                                        animationSpec = tween(
                                            durationMillis = 380,
                                            delayMillis = staggerDelay,
                                            easing = FastOutSlowInEasing
                                        )
                                    )
                                }
                                launch {
                                    itemSlideAnim.animateTo(
                                        targetValue = 0f,
                                        animationSpec = tween(
                                            durationMillis = 380,
                                            delayMillis = staggerDelay,
                                            easing = FastOutSlowInEasing
                                        )
                                    )
                                }
                            }
                        }

                        val activeShowtime = selectedShowtimesByTheatre[theatre.id] ?: theatre.showtimes.first()
                        val isReserved = reservedAlertIds.contains(theatre.id)
                        val isPinSelectedOnMap = isMapExpanded && selectedMapTheatre.id == theatre.id
                        val distanceFromUserKm = BalasoreMovieTheatresRepository.distanceKmBetween(
                            userLat,
                            userLon,
                            theatre.latitude,
                            theatre.longitude
                        )
                        val distanceLabel = BalasoreMovieTheatresRepository.formatDistanceFromUser(
                            distanceFromUserKm,
                            isOdia
                        )
                        val nextShowLabel = BalasoreMovieTheatresRepository.formatNextShowBadge(
                            theatre,
                            isOdia,
                            nowMinutes
                        )

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White,
                            border = BorderStroke(
                                if (isPinSelectedOnMap) 1.6.dp else 1.dp,
                                when {
                                    isPinSelectedOnMap -> Color(0xFF0284C7)
                                    isReserved -> EmeraldGreen.copy(alpha = 0.6f)
                                    else -> Color(0xFFE9D5FF)
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .graphicsLayer {
                                    alpha = itemAlphaAnim.value
                                    translationY = itemSlideAnim.value
                                }
                                .clickable {
                                    selectedMapTheatre = theatre
                                    isMapExpanded = true
                                }
                                .testTag("movie_theatre_item_${theatre.id}")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                        // Top Row: Theatre Name + Address + Distance-from-User Indicator + Next Show + Rating & Tech Spec
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isOdia) theatre.nameOr else theatre.nameEn,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BentoSlate900
                                )
                                Text(
                                    text = "📍 ${if (isOdia) theatre.addressOr else theatre.addressEn}",
                                    fontSize = 10.5.sp,
                                    color = BentoSlate600
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                // Distance-from-User Indicator Badge + Direct Box Office Phone Pill
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFE0F2FE),
                                        border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.35f)),
                                        modifier = Modifier.testTag("movie_theatre_distance_badge_${theatre.id}")
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = "Distance from user",
                                                tint = Color(0xFF0369A1),
                                                modifier = Modifier.size(11.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = distanceLabel,
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF0369A1)
                                            )
                                        }
                                    }

                                    Text(
                                        text = "☎️ ${theatre.boxOfficePhone}",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = EmeraldGreen
                                    )
                                }

                                if (nextShowLabel.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFF3E8FF),
                                        border = BorderStroke(1.dp, Color(0xFF9333EA).copy(alpha = 0.35f)),
                                        modifier = Modifier.testTag("movie_theatre_next_show_badge_${theatre.id}")
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AccessTime,
                                                contentDescription = "Showing soon indicator",
                                                tint = Color(0xFF6B21A8),
                                                modifier = Modifier.size(11.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = nextShowLabel,
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF6B21A8)
                                            )
                                        }
                                    }
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFEF3C7),
                                border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "${theatre.rating} • ${theatre.soundAndScreenSpec.substringBefore(" •")}",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF92400E)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Now Showing Film Banner
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF3E8FF),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)) {
                                Text(
                                    text = "🍿 ${if (isOdia) theatre.nowShowingMovieOr else theatre.nowShowingMovieEn}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF581C87)
                                )
                                Text(
                                    text = "🎟️ ${theatre.ticketFareRange} • ${if (isOdia) theatre.amenitiesOr else theatre.amenitiesEn}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF6B21A8)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Interactive Showtime Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            theatre.showtimes.forEach { time ->
                                val isTimeSelected = time == activeShowtime
                                Surface(
                                    onClick = {
                                        selectedShowtimesByTheatre =
                                            selectedShowtimesByTheatre + (theatre.id to time)
                                    },
                                    shape = RoundedCornerShape(7.dp),
                                    color = if (isTimeSelected) Color(0xFF7E22CE) else BentoSlate100,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isTimeSelected) Color(0xFF7E22CE) else Color(0xFFD8B4FE)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("movie_showtime_chip_${theatre.id}_${time.replace(" ", "_")}")
                                ) {
                                    Text(
                                        text = time,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isTimeSelected) Color.White else Color(0xFF581C87),
                                        modifier = Modifier.padding(vertical = 5.dp, horizontal = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Action Row: Seat Alert/Book, Pinpoint on Map, Call Box Office, Share Deep Link
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. Reserve / Seat Alert Button
                            Surface(
                                onClick = {
                                    val updated = if (isReserved) {
                                        reservedAlertIds - theatre.id
                                    } else {
                                        reservedAlertIds + theatre.id
                                    }
                                    reservedAlertIds = updated
                                    prefs.edit().putBoolean("reserved_${theatre.id}", !isReserved).apply()
                                    Toast.makeText(
                                        context,
                                        if (!isReserved) {
                                            "Seat Alert saved for ${theatre.nameEn} ($activeShowtime show)!"
                                        } else {
                                            "Seat Alert removed for ${theatre.nameEn}"
                                        },
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isReserved) Color(0xFFDCFCE7) else Color(0xFF7E22CE),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .testTag("movie_theatre_book_btn_${theatre.id}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ConfirmationNumber,
                                        contentDescription = null,
                                        tint = if (isReserved) Color(0xFF166534) else Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isReserved) {
                                            if (isOdia) "ବୁକ୍ ସତର୍କତା ✓" else "Saved $activeShowtime ✓"
                                        } else {
                                            if (isOdia) "ସିଟ୍ ବୁକ୍ ($activeShowtime)" else "Book $activeShowtime"
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isReserved) Color(0xFF166534) else Color.White
                                    )
                                }
                            }

                            // 2. Pinpoint on Local Map Button
                            Surface(
                                onClick = {
                                    selectedMapTheatre = theatre
                                    isMapExpanded = true
                                    Toast.makeText(
                                        context,
                                        if (isOdia) {
                                            "ମ୍ୟାପ୍‌ରେ ${theatre.nameOr} ପିନ୍ ଦେଖାଯାଉଛି"
                                        } else {
                                            "Pinpointing ${theatre.nameEn} (${theatre.addressEn}) on Balasore map"
                                        },
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isPinSelectedOnMap) OceanBlue else OceanBlue.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.35f)),
                                modifier = Modifier.testTag("movie_theatre_map_btn_${theatre.id}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Pinpoint Cinema on Map",
                                        tint = if (isPinSelectedOnMap) Color.White else OceanBlue,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (isOdia) "ମ୍ୟାପ୍ ପିନ୍" else "Pin Map",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPinSelectedOnMap) Color.White else OceanBlue
                                    )
                                }
                            }

                            // 3. Call Box Office Button for Direct Inquiries
                            Surface(
                                onClick = {
                                    Toast.makeText(
                                        context,
                                        if (isOdia) {
                                            "${theatre.nameOr} ବକ୍ସ ଅଫିସ୍ (${theatre.boxOfficePhone}) କୁ କଲ୍ କରାଯାଉଛି..."
                                        } else {
                                            "Calling ${theatre.nameEn} Box Office (${theatre.boxOfficePhone}) for direct inquiry..."
                                        },
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    runCatching {
                                        context.startActivity(
                                            Intent(Intent.ACTION_DIAL, Uri.parse("tel:${theatre.boxOfficePhone}")).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = EmeraldGreen,
                                modifier = Modifier.testTag("movie_theatre_call_btn_${theatre.id}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = "Call Box Office for Direct Inquiry",
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isOdia) "କଲ୍ (Call)" else "Call",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            // 4. Share Showtimes Deep Link Button
                            Surface(
                                onClick = {
                                    ShareHelper.shareFeatureBoxDeepLink(
                                        context = context,
                                        featureKey = "movie_theatre_${theatre.id}",
                                        title = "${theatre.nameEn} (${theatre.soundAndScreenSpec})",
                                        odiaTitle = theatre.nameOr,
                                        description = "${theatre.nowShowingMovieEn} • Showtimes: ${theatre.showtimes.joinToString(", ")} • Location: ${theatre.addressEn}",
                                        category = "Balasore Movie Theatres",
                                        isOdia = isOdia
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = OceanBlue.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.3f)),
                                modifier = Modifier.testTag("movie_theatre_share_btn_${theatre.id}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share Theatre Showtimes",
                                        tint = OceanBlue,
                                        modifier = Modifier.size(13.dp)
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
    }
}

@Composable
fun BalasoreMovieTheatresSheet(
    language: AppLanguage,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOdia = language == AppLanguage.ODIA
    val sheetFadeAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        sheetFadeAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing)
        )
    }
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .graphicsLayer { alpha = sheetFadeAnim.value }
            .testTag("balasore_movie_theatres_sheet"),
        contentPadding = PaddingValues(bottom = 36.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isOdia) {
                            "🎬 ବାଲେଶ୍ୱର ସିନେମା ହଲ୍ ଓ ମଲ୍ଟିପ୍ଲେକ୍ସ ଗାଇଡ୍"
                        } else {
                            "🎬 Balasore Movie Theatres & Multiplex Guide"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = BentoSlate900
                        )
                    )
                    Text(
                        text = if (isOdia) {
                            "ଇଣ୍ଟରାକ୍ଟିଭ୍ ମ୍ୟାପ୍ ମାର୍କର, ଶୋ’ ସମୟ, ଟିକେଟ୍ ଦର ଓ ବକ୍ସ ଅଫିସ୍ ନମ୍ବର"
                        } else {
                            "Interactive cinema location map markers, showtimes, ticket fares & box office"
                        },
                        fontSize = 12.sp,
                        color = BentoSlate600
                    )
                }
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("close_movie_theatres_sheet_btn")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close Movie Theatres Sheet")
                }
            }
        }

        item {
            BalasoreMovieTheatresCard(
                language = language,
                onOpenFullSheet = null,
                showInteractiveMapInitially = true
            )
        }
    }
}
