package com.example

import com.example.widget.BalasoreWeatherWidgetProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BalasoreWeatherWidgetTest {

    @Test
    fun `verify widget refresh action constant`() {
        assertEquals(
            "com.example.widget.ACTION_REFRESH_BALASORE_WEATHER",
            BalasoreWeatherWidgetProvider.ACTION_REFRESH_WEATHER
        )
    }

    @Test
    fun `verify widget data model defaults and properties`() {
        val testData = BalasoreWeatherWidgetProvider.Companion.WidgetData(
            tempC = 31,
            conditionDesc = "Scattered Clouds • Balasore",
            windInfo = "💨 Wind: 14 km/h S • 72% Hum",
            alertText = "⚠️ Heavy Rain Alert: Convective squall approaching Balasore",
            hasAlert = true,
            civicStatusText = "Civic Eye: Active monitoring",
            lastUpdatedMillis = System.currentTimeMillis()
        )

        assertEquals(31, testData.tempC)
        assertEquals("Scattered Clouds • Balasore", testData.conditionDesc)
        assertTrue(testData.windInfo.contains("14 km/h"))
        assertTrue(testData.hasAlert)
        assertTrue(testData.alertText.contains("Heavy Rain Alert"))
    }

    @Test
    fun `verify all clear alert text format`() {
        val normalData = BalasoreWeatherWidgetProvider.Companion.WidgetData(
            tempC = 29,
            conditionDesc = "Clear Sky • Balasore Coast",
            windInfo = "💨 Wind: 12 km/h SW • 65% Hum",
            alertText = "✅ Normal Coastal Weather: Sea calm at Chandipur. All clear in Balasore.",
            hasAlert = false,
            civicStatusText = "Civic Eye: All clear",
            lastUpdatedMillis = System.currentTimeMillis()
        )

        assertFalse(normalData.hasAlert)
        assertTrue(normalData.alertText.startsWith("✅"))
        assertTrue(normalData.alertText.contains("All clear in Balasore"))
    }
}
