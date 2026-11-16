package com.example

import com.example.data.model.AppLanguage
import com.example.ui.components.AppLanguagePreferencesManager
import com.example.ui.components.AppLanguageState
import com.example.ui.navigation.HashRoute
import com.example.ui.util.AppStrings
import com.example.ui.viewmodel.BalasoreViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests verifying the persistent language selection component (English / ଓଡ଼ିଆ)
 * and state-managed language provider (`AppLanguageState`, `AppLanguagePreferencesManager`,
 * `BalasoreViewModel.setLanguage` / `toggleLanguage`) that dynamically update application text
 * without needing a page/activity restart.
 */
class LanguageProviderAndSelectionUnitTest {

    @Before
    fun setUp() {
        AppLanguagePreferencesManager.saveLanguage(null, AppLanguage.ENGLISH)
    }

    @Test
    fun testAppLanguageStateDynamicTranslationHelper() {
        var capturedLang = AppLanguage.ENGLISH
        val englishState = AppLanguageState(
            currentLanguage = AppLanguage.ENGLISH,
            onLanguageChange = { capturedLang = it },
            onToggleLanguage = {
                capturedLang = if (capturedLang == AppLanguage.ODIA) AppLanguage.ENGLISH else AppLanguage.ODIA
            }
        )
        assertFalse(englishState.isOdia)
        assertEquals("Explore Baleshwar 360 Hotspots", englishState.tr("Explore Baleshwar 360 Hotspots", "ବାଲେଶ୍ୱର ୩୬୦ ପର୍ଯ୍ୟଟନ ଓ ଐତିହ୍ୟ"))

        englishState.onToggleLanguage()
        assertEquals(AppLanguage.ODIA, capturedLang)

        val odiaState = AppLanguageState(
            currentLanguage = capturedLang,
            onLanguageChange = { capturedLang = it }
        )
        assertTrue(odiaState.isOdia)
        assertEquals("ବାଲେଶ୍ୱର ୩୬୦ ପର୍ଯ୍ୟଟନ ଓ ଐତିହ୍ୟ", odiaState.tr("Explore Baleshwar 360 Hotspots", "ବାଲେଶ୍ୱର ୩୬୦ ପର୍ଯ୍ୟଟନ ଓ ଐତିହ୍ୟ"))
    }

    @Test
    fun testAppLanguagePreferencesManagerStateFlowPersistence() {
        assertEquals(AppLanguage.ENGLISH, AppLanguagePreferencesManager.currentLanguage)
        assertEquals(AppLanguage.ENGLISH, AppLanguagePreferencesManager.languageFlow.value)

        // Select Odia (ଓଡ଼ିଆ)
        AppLanguagePreferencesManager.saveLanguage(null, AppLanguage.ODIA)
        assertEquals(AppLanguage.ODIA, AppLanguagePreferencesManager.currentLanguage)
        assertEquals(AppLanguage.ODIA, AppLanguagePreferencesManager.getSavedLanguage(null))

        // Toggle back to English
        val toggled = AppLanguagePreferencesManager.toggleLanguage(null)
        assertEquals(AppLanguage.ENGLISH, toggled)
        assertEquals(AppLanguage.ENGLISH, AppLanguagePreferencesManager.currentLanguage)
    }

    @Test
    fun testViewModelDynamicLanguageSwitchingAndPersistenceSync() {
        val viewModel = BalasoreViewModel()
        assertEquals(AppLanguage.ENGLISH, viewModel.uiState.value.language)

        // Dynamically switch to Odia without restart
        viewModel.setLanguage(AppLanguage.ODIA)
        assertEquals(AppLanguage.ODIA, viewModel.uiState.value.language)
        assertEquals(AppLanguage.ODIA, AppLanguagePreferencesManager.currentLanguage)

        // Toggle back to English
        viewModel.toggleLanguage()
        assertEquals(AppLanguage.ENGLISH, viewModel.uiState.value.language)
        assertEquals(AppLanguage.ENGLISH, AppLanguagePreferencesManager.currentLanguage)
    }

    @Test
    fun testDynamicTopAndBottomBarLocalizationStrings() {
        // English state
        assertEquals("Baleshwar 360", AppStrings.appName(AppLanguage.ENGLISH))
        assertEquals("Tourism", HashRoute.Tourism.localizedTitle(AppLanguage.ENGLISH))
        assertEquals("News", HashRoute.News.localizedTitle(AppLanguage.ENGLISH))
        assertEquals("Weather", HashRoute.Weather.localizedTitle(AppLanguage.ENGLISH))
        assertEquals("Profile", HashRoute.Profile.localizedTitle(AppLanguage.ENGLISH))

        // Odia (ଓଡ଼ିଆ) state
        assertEquals("ବାଲେଶ୍ୱର ୩୬୦", AppStrings.appName(AppLanguage.ODIA))
        assertEquals("ପର୍ଯ୍ୟଟନ", HashRoute.Tourism.localizedTitle(AppLanguage.ODIA))
        assertEquals("ଖବର", HashRoute.News.localizedTitle(AppLanguage.ODIA))
        assertEquals("ପାଣିପାଗ", HashRoute.Weather.localizedTitle(AppLanguage.ODIA))
        assertEquals("ପ୍ରୋଫାଇଲ୍", HashRoute.Profile.localizedTitle(AppLanguage.ODIA))
    }
}
