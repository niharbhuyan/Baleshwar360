package com.example

import com.example.data.model.TrainMovement
import com.example.data.model.TrainNotificationPreference
import com.example.data.model.TrainStatusCategory
import com.example.data.model.TrainType
import com.example.data.repository.BalasoreTrainRepository
import com.example.ui.viewmodel.BalasoreViewModel
import com.example.ui.viewmodel.UniqueFeatureSheetType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class BalasoreTrainRunningStatusTest {

    @Test
    fun testBalasoreTrainRepositoryDataset() {
        val trains = BalasoreTrainRepository.INITIAL_TRAIN_DATASET
        assertTrue("Train list should contain trains for Balasore Junction", trains.isNotEmpty())
        assertTrue("Should have at least 10 major trains", trains.size >= 10)

        // Verify Vande Bharat Express
        val vandeBharat = trains.find { it.trainNumber == "20831" }
        assertNotNull("Howrah - Puri Vande Bharat should be present", vandeBharat)
        assertEquals(TrainType.VANDE_BHARAT, vandeBharat?.type)
        assertEquals("Platform 2", vandeBharat?.platform)
        assertEquals("HWH", vandeBharat?.originCode)
        assertEquals("PURI", vandeBharat?.destinationCode)

        // Verify Dhauli Superfast Express
        val dhauli = trains.find { it.trainNumber == "12821" }
        assertNotNull("Dhauli Superfast Express should be present", dhauli)
        assertEquals(TrainType.SUPERFAST, dhauli?.type)
        assertTrue("Dhauli should have a non-empty coach composition", dhauli?.coachComposition?.isNotBlank() == true)

        // Verify Coromandel Express
        val coromandel = trains.find { it.trainNumber == "12841" }
        assertNotNull("Coromandel Express should be present", coromandel)
        assertEquals("MAS", coromandel?.destinationCode)

        // Verify Local MEMU Passenger
        val memu = trains.find { it.trainNumber == "08063" }
        assertNotNull("MEMU Passenger should be present", memu)
        assertEquals(TrainType.MEMU_PASSENGER, memu?.type)
    }

    @Test
    fun testLiveTelemetrySimulationTick() = runBlocking {
        val repository = BalasoreTrainRepository()
        val trainBefore = repository.getTrainByNumber("20831")
        assertNotNull(trainBefore)
        val initialDist = trainBefore!!.distanceToBalasoreKm

        val refreshed = repository.refreshLiveStatuses()
        val trainAfter = refreshed.find { it.trainNumber == "20831" }
        assertNotNull(trainAfter)

        // Distance should be updated or maintained >= 0
        assertTrue("Distance should remain non-negative", trainAfter!!.distanceToBalasoreKm >= 0.0)
        assertNotNull(trainAfter.statusCategory)
    }

    @Test
    fun testViewModelBalasoreLiveTrainStatusSheet() {
        val viewModel = BalasoreViewModel()
        assertNull(viewModel.uiState.value.activeFeatureSheet)

        viewModel.openFeatureSheet(UniqueFeatureSheetType.BALASORE_LIVE_TRAIN_STATUS)
        assertEquals(
            UniqueFeatureSheetType.BALASORE_LIVE_TRAIN_STATUS,
            viewModel.uiState.value.activeFeatureSheet
        )

        viewModel.closeFeatureSheet()
        assertNull(viewModel.uiState.value.activeFeatureSheet)
    }

    @Test
    fun testTrainNotificationPreferenceDefaults() {
        val pref = TrainNotificationPreference(trainNumber = "20831")
        assertTrue(pref.isTrackingEnabled)
        assertTrue(pref.notifyOnApproaching)
        assertTrue(pref.notifyOnDelay)
        assertTrue(pref.notifyOnPlatformChange)
        assertTrue(pref.notifyOnArrival)
        assertEquals(15, pref.advanceMinutes)
    }

    @Test
    fun testTrainMovementEnumBilingualLabels() {
        assertEquals("Arrivals", TrainMovement.ARRIVALS.labelEn)
        assertEquals("ଆଗମନ", TrainMovement.ARRIVALS.labelOr)
        assertEquals("Departures", TrainMovement.DEPARTURES.labelEn)
        assertEquals("ପ୍ରସ୍ଥାନ", TrainMovement.DEPARTURES.labelOr)
    }
}
