package com.example

import com.example.data.model.BalasoreForecastDay
import com.example.data.model.WeatherInfo
import com.example.data.remote.OpenWeatherDailyForecast
import com.example.ui.components.FiveDayForecastChartHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FiveDayForecastChartTest {

    @Test
    fun buildFiveDayPoints_takesFirstFiveOpenWeatherDays() {
        val sevenDays = (1..7).map { idx ->
            OpenWeatherDailyForecast(
                dayIndex = idx,
                dateLabel = "2${idx} Nov",
                dayName = listOf("Thu", "Fri", "Sat", "Sun", "Mon", "Tue", "Wed")[idx - 1],
                odiaDayName = listOf("ଗୁରୁ", "ଶୁକ୍ର", "ଶନି", "ରବି", "ସୋମ", "ମଙ୍ଗଳ", "ବୁଧ")[idx - 1],
                tempMax = 30.0 + idx,
                tempMin = 22.0 + idx * 0.5,
                avgHumidity = 70 + idx,
                windSpeedKmh = 12.0 + idx,
                conditionMain = if (idx == 3) "Rain" else "Clouds",
                conditionDesc = "Scattered clouds",
                iconCode = "03d",
                popPercent = idx * 10
            )
        }

        val points = FiveDayForecastChartHelper.buildFiveDayPoints(openWeatherForecast = sevenDays)
        assertEquals(5, points.size)
        assertEquals("Today", points[0].dayShortEn)
        assertEquals("Fri", points[1].dayShortEn)
        assertEquals(35.0, points[4].highTempC, 0.01)

        val summary = FiveDayForecastChartHelper.summarize(points)
        assertEquals(35.0, summary.peakHighC, 0.01)
        assertEquals(22.5, summary.lowestLowC, 0.01)
        assertEquals(50, summary.maxRainChancePct)
        assertEquals("Mon", summary.peakDayLabel)
    }

    @Test
    fun buildFiveDayPoints_fallsBackToForecastDaysAndBaseline() {
        val fallbackDays = listOf(
            BalasoreForecastDay(
                id = "d1",
                dayLabel = "Thu",
                dateFormatted = "26 Nov",
                odiaDayLabel = "ଗୁରୁ",
                highTempC = 32,
                lowTempC = 24,
                condition = "Clear",
                odiaCondition = "ସଫା",
                weatherIcon = "☀️",
                rainProbability = 10,
                windSummary = "10 km/h NE",
                humidity = "70%",
                uvIndex = "6",
                marineNotice = "Calm"
            ),
            BalasoreForecastDay(
                id = "d2",
                dayLabel = "Fri",
                dateFormatted = "27 Nov",
                odiaDayLabel = "ଶୁକ୍ର",
                highTempC = 33,
                lowTempC = 25,
                condition = "Cloudy",
                odiaCondition = "ମେଘୁଆ",
                weatherIcon = "⛅",
                rainProbability = 25,
                windSummary = "12 km/h E",
                humidity = "74%",
                uvIndex = "5",
                marineNotice = "Moderate"
            )
        )
        val padded = FiveDayForecastChartHelper.buildFiveDayPoints(
            openWeatherForecast = emptyList(),
            fallbackForecastDays = fallbackDays,
            currentWeather = WeatherInfo(
                tempCelsius = 31,
                condition = "Sunny",
                humidity = "72%",
                windSpeedKmh = "12 km/h",
                tideStatus = "Low Tide"
            )
        )
        assertEquals(5, padded.size)
        assertEquals(32.0, padded[0].highTempC, 0.01)
        assertEquals(33.0, padded[1].highTempC, 0.01)
    }

    @Test
    fun unitConversionAndYAxisBounds_handleCelsiusAndFahrenheit() {
        assertEquals(86.0, FiveDayForecastChartHelper.convertTemp(30.0, isFahrenheit = true), 0.01)
        assertEquals("86°F", FiveDayForecastChartHelper.formatTempWhole(30.0, isFahrenheit = true))
        assertEquals("30°C", FiveDayForecastChartHelper.formatTempWhole(30.0, isFahrenheit = false))

        val baseline = FiveDayForecastChartHelper.buildFiveDayPoints(emptyList())
        val (minC, maxC) = FiveDayForecastChartHelper.computeYAxisBounds(baseline, isFahrenheit = false)
        assertTrue("Expected maxC > minC + 4", maxC >= minC + 4f)

        val (minF, maxF) = FiveDayForecastChartHelper.computeYAxisBounds(baseline, isFahrenheit = true)
        assertTrue("Expected Fahrenheit bounds higher than Celsius", minF > minC && maxF > maxC)
    }
}
