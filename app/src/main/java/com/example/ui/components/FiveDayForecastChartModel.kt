package com.example.ui.components

import com.example.data.model.BalasoreForecastDay
import com.example.data.model.WeatherInfo
import com.example.data.remote.OpenWeatherDailyForecast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * Single day point for the 5-day Balasore temperature forecast chart widget.
 */
data class FiveDayForecastPoint(
    val dayIndex: Int,
    val dayShortEn: String,
    val dayShortOd: String,
    val dateLabel: String,
    val highTempC: Double,
    val lowTempC: Double,
    val avgTempC: Double,
    val condition: String,
    val rainChancePct: Int,
    val humidityPct: Int,
    val windKmh: Double
)

/**
 * Aggregated 5-day weather summary metrics shown alongside the temperature forecast chart.
 */
data class FiveDayForecastSummary(
    val points: List<FiveDayForecastPoint>,
    val peakHighC: Double,
    val lowestLowC: Double,
    val fiveDayAvgC: Double,
    val maxRainChancePct: Int,
    val peakDayLabel: String
)

/**
 * Pure domain utilities for preparing 5-day temperature forecast series, unit conversion,
 * Y-axis chart bounds, and summary statistics for the dashboard widget.
 */
object FiveDayForecastChartHelper {

    private val odiaWeekdays = mapOf(
        "sun" to "ରବି",
        "mon" to "ସୋମ",
        "tue" to "ମଙ୍ଗଳ",
        "wed" to "ବୁଧ",
        "thu" to "ଗୁରୁ",
        "fri" to "ଶୁକ୍ର",
        "sat" to "ଶନି"
    )

    fun convertTemp(celsius: Double, isFahrenheit: Boolean): Double {
        val raw = if (isFahrenheit) celsius * 9.0 / 5.0 + 32.0 else celsius
        return (raw * 10.0).roundToInt() / 10.0
    }

    fun formatTempWhole(celsius: Double, isFahrenheit: Boolean): String {
        val value = convertTemp(celsius, isFahrenheit).roundToInt()
        val unit = if (isFahrenheit) "°F" else "°C"
        return "$value$unit"
    }

    /**
     * Resolves exactly 5 chronological forecast days from live OpenWeatherMap data,
     * falling back to Open-Meteo daily forecast days or a Balasore baseline anchored to current weather.
     */
    fun buildFiveDayPoints(
        openWeatherForecast: List<OpenWeatherDailyForecast>,
        fallbackForecastDays: List<BalasoreForecastDay> = emptyList(),
        currentWeather: WeatherInfo? = null
    ): List<FiveDayForecastPoint> {
        if (openWeatherForecast.isNotEmpty()) {
            val mapped = openWeatherForecast.take(5).mapIndexed { idx, day ->
                val high = day.tempMax
                val low = day.tempMin.coerceAtMost(high)
                val avg = ((high + low) / 2.0 * 10.0).roundToInt() / 10.0
                val shortEn = if (idx == 0) "Today" else day.dayName.take(3).ifBlank { "Day ${idx + 1}" }
                val shortOd = if (idx == 0) "ଆଜି" else day.odiaDayName.ifBlank { shortEn }
                FiveDayForecastPoint(
                    dayIndex = idx,
                    dayShortEn = shortEn,
                    dayShortOd = shortOd,
                    dateLabel = day.dateLabel.ifBlank { shortEn },
                    highTempC = high,
                    lowTempC = low,
                    avgTempC = avg,
                    condition = day.conditionMain.ifBlank { day.conditionDesc.ifBlank { "Partly Cloudy" } },
                    rainChancePct = day.popPercent.coerceIn(0, 100),
                    humidityPct = day.avgHumidity.coerceIn(0, 100),
                    windKmh = day.windSpeedKmh.coerceAtLeast(0.0)
                )
            }
            if (mapped.size == 5) return mapped
            return padToFiveDays(mapped, currentWeather)
        }

        if (fallbackForecastDays.isNotEmpty()) {
            val mapped = fallbackForecastDays.take(5).mapIndexed { idx, day ->
                val high = day.highTempC.toDouble()
                val low = day.lowTempC.toDouble().coerceAtMost(high)
                val avg = ((high + low) / 2.0 * 10.0).roundToInt() / 10.0
                val shortEn = if (idx == 0) "Today" else day.dayLabel.take(3).ifBlank { "Day ${idx + 1}" }
                val shortOd = if (idx == 0) "ଆଜି" else day.odiaDayLabel.ifBlank {
                    odiaWeekdays[shortEn.lowercase(Locale.US)] ?: shortEn
                }
                val parsedHumidity = Regex("(\\d+)").find(day.humidity)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 72
                val parsedWind = Regex("(\\d+(?:\\.\\d+)?)").find(day.windSummary)?.groupValues?.getOrNull(1)?.toDoubleOrNull() ?: 12.0
                FiveDayForecastPoint(
                    dayIndex = idx,
                    dayShortEn = shortEn,
                    dayShortOd = shortOd,
                    dateLabel = day.dateFormatted.ifBlank { shortEn },
                    highTempC = high,
                    lowTempC = low,
                    avgTempC = avg,
                    condition = day.condition.ifBlank { "Coastal Breeze" },
                    rainChancePct = day.rainProbability.coerceIn(0, 100),
                    humidityPct = parsedHumidity.coerceIn(0, 100),
                    windKmh = parsedWind.coerceAtLeast(0.0)
                )
            }
            if (mapped.size == 5) return mapped
            return padToFiveDays(mapped, currentWeather)
        }

        return buildBaselineFiveDays(currentWeather)
    }

    private fun padToFiveDays(
        existing: List<FiveDayForecastPoint>,
        currentWeather: WeatherInfo?
    ): List<FiveDayForecastPoint> {
        val baseline = buildBaselineFiveDays(currentWeather)
        return List(5) { idx ->
            existing.getOrNull(idx) ?: baseline[idx]
        }
    }

    private fun buildBaselineFiveDays(currentWeather: WeatherInfo?): List<FiveDayForecastPoint> {
        val anchorHigh = (currentWeather?.tempCelsius ?: 31).toDouble().coerceIn(18.0, 44.0)
        val anchorLow = (anchorHigh - 7.5).coerceAtLeast(12.0)
        val anchorHumidity = Regex("(\\d+)").find(currentWeather?.humidity.orEmpty())
            ?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 74
        val highOffsets = listOf(0.0, 1.2, 0.5, -1.4, 0.8)
        val lowOffsets = listOf(0.0, 0.6, 0.2, -0.9, 0.4)
        val rainChances = listOf(15, 20, 35, 65, 25)
        val conditions = listOf("Partly Cloudy", "Warm & Breezy", "Scattered Clouds", "Coastal Showers", "Clear Skies")
        val winds = listOf(11.0, 13.5, 14.0, 18.5, 10.5)

        val dayFmt = SimpleDateFormat("EEE", Locale.US)
        val dateFmt = SimpleDateFormat("dd MMM", Locale.US)

        return List(5) { idx ->
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, idx) }
            val dayName = if (idx == 0) "Today" else dayFmt.format(cal.time)
            val odiaName = if (idx == 0) "ଆଜି" else odiaWeekdays[dayName.lowercase(Locale.US)] ?: dayName
            val high = ((anchorHigh + highOffsets[idx]) * 10.0).roundToInt() / 10.0
            val low = ((anchorLow + lowOffsets[idx]) * 10.0).roundToInt() / 10.0
            val avg = ((high + low) / 2.0 * 10.0).roundToInt() / 10.0
            FiveDayForecastPoint(
                dayIndex = idx,
                dayShortEn = dayName,
                dayShortOd = odiaName,
                dateLabel = dateFmt.format(cal.time),
                highTempC = high,
                lowTempC = low,
                avgTempC = avg,
                condition = conditions[idx],
                rainChancePct = rainChances[idx],
                humidityPct = (anchorHumidity + (idx - 1) * 2).coerceIn(45, 95),
                windKmh = winds[idx]
            )
        }
    }

    fun summarize(points: List<FiveDayForecastPoint>): FiveDayForecastSummary {
        if (points.isEmpty()) {
            return FiveDayForecastSummary(
                points = emptyList(),
                peakHighC = 0.0,
                lowestLowC = 0.0,
                fiveDayAvgC = 0.0,
                maxRainChancePct = 0,
                peakDayLabel = ""
            )
        }
        val peak = points.maxBy { it.highTempC }
        val lowest = points.minOf { it.lowTempC }
        val avg = (points.map { it.avgTempC }.average() * 10.0).roundToInt() / 10.0
        val maxRain = points.maxOf { it.rainChancePct }
        return FiveDayForecastSummary(
            points = points,
            peakHighC = peak.highTempC,
            lowestLowC = lowest,
            fiveDayAvgC = avg,
            maxRainChancePct = maxRain,
            peakDayLabel = peak.dayShortEn
        )
    }

    /**
     * Computes clean rounded Y-axis (min, max) bounds so High & Low temperature area series
     * have balanced breathing room and neat integer grid ticks.
     */
    fun computeYAxisBounds(
        points: List<FiveDayForecastPoint>,
        isFahrenheit: Boolean
    ): Pair<Float, Float> {
        if (points.isEmpty()) return if (isFahrenheit) 60f to 100f else 15f to 40f
        val highs = points.map { convertTemp(it.highTempC, isFahrenheit) }
        val lows = points.map { convertTemp(it.lowTempC, isFahrenheit) }
        val minVal = floor((lows.minOrNull() ?: 20.0) - 2.0).toFloat()
        val maxVal = ceil((highs.maxOrNull() ?: 35.0) + 2.0).toFloat()
        val adjustedMax = if (maxVal - minVal < 4f) minVal + 4f else maxVal
        return minVal to adjustedMax
    }
}
