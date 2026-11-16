package com.example

import com.example.data.model.AppLanguage
import com.example.ui.navigation.HashRoute
import com.example.ui.navigation.HashRouter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HashRouterTest {

    @Test
    fun testBottomNavRoutesContainRequestedViews() {
        val routes = HashRoute.bottomNavRoutes
        assertEquals(4, routes.size)
        assertEquals(HashRoute.News, routes[0])
        assertEquals(HashRoute.Weather, routes[1])
        assertEquals(HashRoute.Tourism, routes[2])
        assertEquals(HashRoute.Profile, routes[3])
    }

    @Test
    fun testHashRouteProperties() {
        assertEquals("#news", HashRoute.News.hash)
        assertEquals(2, HashRoute.News.tabIndex)
        assertEquals("News", HashRoute.News.titleEnglish)

        assertEquals("#weather", HashRoute.Weather.hash)
        assertEquals(3, HashRoute.Weather.tabIndex)
        assertEquals("Weather", HashRoute.Weather.titleEnglish)

        assertEquals("#tourism", HashRoute.Tourism.hash)
        assertEquals(0, HashRoute.Tourism.tabIndex)
        assertEquals("Tourism", HashRoute.Tourism.titleEnglish)

        assertEquals("#profile", HashRoute.Profile.hash)
        assertEquals(5, HashRoute.Profile.tabIndex)
        assertEquals("Profile", HashRoute.Profile.titleEnglish)
    }

    @Test
    fun testFromHashParsing() {
        assertEquals(HashRoute.News, HashRoute.fromHash("#news"))
        assertEquals(HashRoute.News, HashRoute.fromHash("news"))
        assertEquals(HashRoute.Weather, HashRoute.fromHash("#weather"))
        assertEquals(HashRoute.Weather, HashRoute.fromHash("weather"))
        assertEquals(HashRoute.Tourism, HashRoute.fromHash("#tourism"))
        assertEquals(HashRoute.Tourism, HashRoute.fromHash("#hotspots"))
        assertEquals(HashRoute.Profile, HashRoute.fromHash("#profile"))
        assertEquals(HashRoute.Profile, HashRoute.fromHash("account"))
    }

    @Test
    fun testFromTabIndex() {
        assertEquals(HashRoute.News, HashRoute.fromTabIndex(2))
        assertEquals(HashRoute.Weather, HashRoute.fromTabIndex(3))
        assertEquals(HashRoute.Tourism, HashRoute.fromTabIndex(0))
        assertEquals(HashRoute.Profile, HashRoute.fromTabIndex(5))
    }

    @Test
    fun testLocalizedTitles() {
        assertEquals("ଖବର", HashRoute.News.localizedTitle(AppLanguage.ODIA))
        assertEquals("News", HashRoute.News.localizedTitle(AppLanguage.ENGLISH))
        assertEquals("ପାଣିପାଗ", HashRoute.Weather.localizedTitle(AppLanguage.ODIA))
        assertEquals("ପର୍ଯ୍ୟଟନ", HashRoute.Tourism.localizedTitle(AppLanguage.ODIA))
        assertEquals("ପ୍ରୋଫାଇଲ୍", HashRoute.Profile.localizedTitle(AppLanguage.ODIA))
    }

    @Test
    fun testHashRouterNavigationAndHistory() {
        var notifiedRoute: HashRoute? = null
        val router = HashRouter(initialRoute = HashRoute.Tourism) { route ->
            notifiedRoute = route
        }

        assertEquals(HashRoute.Tourism, router.currentRoute)
        assertEquals("#tourism", router.currentHash)
        assertFalse(router.canGoBack())

        // Navigate to #news
        router.navigate(HashRoute.News)
        assertEquals(HashRoute.News, router.currentRoute)
        assertEquals("#news", router.currentHash)
        assertEquals(HashRoute.News, notifiedRoute)
        assertTrue(router.canGoBack())
        assertEquals(2, router.history.size)

        // Navigate via hash string to #profile
        router.navigate("#profile")
        assertEquals(HashRoute.Profile, router.currentRoute)
        assertEquals("#profile", router.currentHash)
        assertEquals(HashRoute.Profile, notifiedRoute)
        assertEquals(3, router.history.size)

        // Pop back to #news
        val popped = router.pop()
        assertTrue(popped)
        assertEquals(HashRoute.News, router.currentRoute)
        assertEquals(HashRoute.News, notifiedRoute)
        assertEquals(2, router.history.size)

        // Pop back to #tourism
        val popped2 = router.pop()
        assertTrue(popped2)
        assertEquals(HashRoute.Tourism, router.currentRoute)
        assertEquals(1, router.history.size)
        assertFalse(router.canGoBack())

        // Popping at root returns false
        val poppedAtRoot = router.pop()
        assertFalse(poppedAtRoot)
        assertEquals(HashRoute.Tourism, router.currentRoute)
    }
}
