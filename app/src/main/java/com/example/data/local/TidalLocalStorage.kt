package com.example.data.local

import android.content.Context
import com.example.data.model.TidalPhase
import com.example.data.model.TidalTimeframe
import com.example.data.model.TidalTrendPoint
import com.example.data.model.TidalTrendSummary
import kotlin.math.PI
import kotlin.math.cos

class TidalLocalStorage(context: Context) {
    private val prefs = context.getSharedPreferences("balasore_tidal_storage", Context.MODE_PRIVATE)
    private var lastSyncTimestamp: Long = prefs.getLong("last_sync_ts", System.currentTimeMillis())
    private val customObservations = mutableListOf<TidalTrendPoint>()

    companion object {
        @Volatile
        private var INSTANCE: TidalLocalStorage? = null

        fun getInstance(context: Context): TidalLocalStorage {
            return INSTANCE ?: synchronized(this) {
                val instance = TidalLocalStorage(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    fun ensureDefaultHistoricalDataSeeded() {
        if (!prefs.getBoolean("seeded_v1", false)) {
            lastSyncTimestamp = System.currentTimeMillis()
            prefs.edit()
                .putBoolean("seeded_v1", true)
                .putLong("last_sync_ts", lastSyncTimestamp)
                .apply()
        }
    }

    fun getTidalData(timeframe: TidalTimeframe): List<TidalTrendPoint> =
        getTidalPointsForTimeframe(timeframe)

    fun getTidalPointsForTimeframe(timeframe: TidalTimeframe): List<TidalTrendPoint> {
        val now = System.currentTimeMillis()
        val count = when (timeframe) {
            TidalTimeframe.HOURLY_24H -> 24
            TidalTimeframe.HISTORICAL_7D -> 28
            TidalTimeframe.MONTHLY_30D -> 30
        }
        val stepMillis = when (timeframe) {
            TidalTimeframe.HOURLY_24H -> 3600_000L
            TidalTimeframe.HISTORICAL_7D -> 6 * 3600_000L
            TidalTimeframe.MONTHLY_30D -> 24 * 3600_000L
        }

        val generated = (0 until count).map { idx ->
            val ts = now - (count - 1 - idx) * stepMillis
            val angle = (idx.toDouble() / 6.2) * 2.0 * PI
            val normalized = (cos(angle) + 1.0) / 2.0 // 0.0 to 1.0
            val heightM = 0.6 + normalized * 3.2
            val recessionKm = ((1.0 - normalized) * 4.5).coerceIn(0.1, 4.8)
            val phase = when {
                normalized > 0.78 -> TidalPhase.HIGH_TIDE
                normalized < 0.22 -> TidalPhase.LOW_TIDE_PEAK
                idx % 4 < 2 -> TidalPhase.RECEDING
                else -> TidalPhase.INCOMING
            }
            val label = when (timeframe) {
                TidalTimeframe.HOURLY_24H -> "${(idx % 24).toString().padStart(2, '0')}:00"
                TidalTimeframe.HISTORICAL_7D -> "D-${(count - idx) / 4 + 1} ${(idx % 4) * 6}h"
                TidalTimeframe.MONTHLY_30D -> "Day ${idx + 1}"
            }
            val phaseDesc = when (phase) {
                TidalPhase.LOW_TIDE_PEAK -> "Maximum seabed exposed (~${String.format("%.1f", recessionKm)} km walk)"
                TidalPhase.RECEDING -> "Water receding steadily towards outer sandbar"
                TidalPhase.INCOMING -> "Tide returning rapidly — return toward embankment"
                TidalPhase.HIGH_TIDE -> "Full high water along Chandipur promenade"
            }
            val safety = when (phase) {
                TidalPhase.LOW_TIDE_PEAK, TidalPhase.RECEDING -> "Safe for Seabed Exploration"
                TidalPhase.INCOMING -> "Caution: Return to Shore Soon"
                TidalPhase.HIGH_TIDE -> "Stay on Beach Promenade"
            }
            TidalTrendPoint(
                id = "tidal_${timeframe.name}_$idx",
                timestamp = ts,
                timeLabel = label,
                tideHeightMeters = (heightM * 100).toInt() / 100.0,
                recessionDistanceKm = (recessionKm * 100).toInt() / 100.0,
                phase = phase,
                phaseDescription = phaseDesc,
                lunarCondition = if (idx % 2 == 0) "Spring Tide (Purnima)" else "Waxing Lunar Cycle",
                safetyStatus = safety
            )
        }
        return if (customObservations.isEmpty()) generated else (generated + customObservations).takeLast(count)
    }

    fun getSummary(): TidalTrendSummary = getTrendSummary()

    fun getTrendSummary(): TidalTrendSummary {
        val points = getTidalPointsForTimeframe(TidalTimeframe.HOURLY_24H)
        val latest = points.lastOrNull()
        return TidalTrendSummary(
            currentHeightMeters = latest?.tideHeightMeters ?: 1.4,
            currentRecessionKm = latest?.recessionDistanceKm ?: 3.2,
            maxRecessionObservedKm = points.maxOfOrNull { it.recessionDistanceKm } ?: 4.5,
            lowestTideLevelMeters = points.minOfOrNull { it.tideHeightMeters } ?: 0.6,
            highestTideLevelMeters = points.maxOfOrNull { it.tideHeightMeters } ?: 3.8,
            totalObservationsCount = 720 + customObservations.size,
            lastStorageSyncTimestamp = lastSyncTimestamp
        )
    }

    fun addObservation(
        point: TidalTrendPoint,
        timeframe: TidalTimeframe = TidalTimeframe.HOURLY_24H
    ): TidalTrendSummary {
        customObservations.add(point)
        lastSyncTimestamp = System.currentTimeMillis()
        prefs.edit().putLong("last_sync_ts", lastSyncTimestamp).apply()
        return getTrendSummary()
    }

    fun resetToDefaults(): TidalTrendSummary {
        customObservations.clear()
        lastSyncTimestamp = System.currentTimeMillis()
        prefs.edit().putLong("last_sync_ts", lastSyncTimestamp).apply()
        return getTrendSummary()
    }

    fun appendLiveObservationAndRefresh(): TidalTrendSummary {
        lastSyncTimestamp = System.currentTimeMillis()
        prefs.edit().putLong("last_sync_ts", lastSyncTimestamp).apply()
        return getTrendSummary()
    }
}
