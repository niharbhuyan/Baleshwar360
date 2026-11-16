package com.example

import android.content.Context
import android.content.SharedPreferences
import com.example.data.firebase.FirestoreService
import com.example.data.model.UserWeatherPreferences
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class FirestoreWeatherPreferencesRuleTest {

    private fun createMockContextWithPrefs(): Context {
        val store = mutableMapOf<String, Any>()
        val editor = mock(SharedPreferences.Editor::class.java)
        val prefs = mock(SharedPreferences::class.java)
        val context = mock(Context::class.java)

        `when`(editor.putBoolean(anyString(), anyBoolean())).thenAnswer { inv ->
            store[inv.getArgument(0)] = inv.getArgument<Boolean>(1)
            editor
        }
        `when`(editor.putInt(anyString(), anyInt())).thenAnswer { inv ->
            store[inv.getArgument(0)] = inv.getArgument<Int>(1)
            editor
        }
        `when`(editor.putLong(anyString(), anyLong())).thenAnswer { inv ->
            store[inv.getArgument(0)] = inv.getArgument<Long>(1)
            editor
        }
        `when`(prefs.edit()).thenReturn(editor)
        `when`(prefs.getBoolean(anyString(), anyBoolean())).thenAnswer { inv ->
            (store[inv.getArgument(0)] as? Boolean) ?: inv.getArgument(1)
        }
        `when`(prefs.getInt(anyString(), anyInt())).thenAnswer { inv ->
            (store[inv.getArgument(0)] as? Int) ?: inv.getArgument(1)
        }
        `when`(prefs.getLong(anyString(), anyLong())).thenAnswer { inv ->
            (store[inv.getArgument(0)] as? Long) ?: inv.getArgument(1)
        }
        `when`(context.getSharedPreferences(anyString(), anyInt())).thenReturn(prefs)
        `when`(context.applicationContext).thenReturn(context)
        return context
    }

    @Test
    fun fetchWeatherPreferences_unauthenticatedOrLocalSession_returnsLocalWithoutPermissionDenied() = runBlocking {
        val context = createMockContextWithPrefs()
        val service = FirestoreService(context)

        val saveRes = service.saveWeatherPreferences(
            userId = "local_weather_user",
            prefs = UserWeatherPreferences(
                userId = "local_weather_user",
                notifyCyclone = false,
                notifyHighTide = true,
                notifySevereHeat = false,
                notifyRainfall = true
            )
        )
        assertTrue(saveRes.isSuccess)

        val fetchRes = service.fetchWeatherPreferences("local_weather_user")
        assertTrue(fetchRes.isSuccess)
        val loaded = fetchRes.getOrNull()
        assertNotNull(loaded)
        assertEquals(false, loaded?.notifyCyclone)
        assertEquals(true, loaded?.notifyHighTide)
        assertEquals(false, loaded?.notifySevereHeat)
        assertEquals(true, loaded?.notifyRainfall)
    }

    @Test
    fun userWeatherPreferences_firestoreMap_containsAllRequiredRuleFields() {
        val prefs = UserWeatherPreferences(
            userId = "alice_123",
            notifyCyclone = true,
            notifyHighTide = false,
            notifySevereHeat = true,
            notifyRainfall = true,
            highTideAlertHoursBefore = 3,
            minTempThreshold = 14,
            maxTempThreshold = 40
        )
        val map = prefs.toFirestoreMap()
        assertEquals("alice_123", map["userId"])
        assertEquals(true, map["notifyCyclone"])
        assertEquals(false, map["notifyHighTide"])
        assertEquals(true, map["notifySevereHeat"])
        assertEquals(true, map["notifyRainfall"])
        assertEquals(3, map["highTideAlertHoursBefore"])
        assertTrue(map.containsKey("updatedAt"))
    }
}
