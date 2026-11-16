package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.data.model.AppLanguage

/**
 * Hash-based routing destination keys for switching views without reloading or changing pages.
 * Supports '#news', '#weather', '#tourism', and '#profile'.
 */
sealed class HashRoute(
    val hash: String,
    val titleEnglish: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tabIndex: Int
) {
    object News : HashRoute(
        hash = "#news",
        titleEnglish = "News",
        selectedIcon = Icons.AutoMirrored.Filled.Article,
        unselectedIcon = Icons.AutoMirrored.Outlined.Article,
        tabIndex = 2
    )

    object Weather : HashRoute(
        hash = "#weather",
        titleEnglish = "Weather",
        selectedIcon = Icons.Default.Cloud,
        unselectedIcon = Icons.Outlined.Cloud,
        tabIndex = 3
    )

    object Tourism : HashRoute(
        hash = "#tourism",
        titleEnglish = "Tourism",
        selectedIcon = Icons.Default.Explore,
        unselectedIcon = Icons.Outlined.Explore,
        tabIndex = 0
    )

    object Profile : HashRoute(
        hash = "#profile",
        titleEnglish = "Profile",
        selectedIcon = Icons.Default.AccountCircle,
        unselectedIcon = Icons.Outlined.AccountCircle,
        tabIndex = 5
    )

    fun localizedTitle(language: AppLanguage): String {
        return when (this) {
            News -> when (language) {
                AppLanguage.ODIA -> "ଖବର"
                AppLanguage.HINDI -> "समाचार"
                AppLanguage.BENGALI -> "সংবাদ"
                AppLanguage.TELUGU -> "వార్తలు"
                AppLanguage.TAMIL -> "செய்திகள்"
                AppLanguage.MARATHI -> "बातम्या"
                AppLanguage.GUJARATI -> "સમાચાર"
                AppLanguage.KANNADA -> "ಸುದ್ದಿ"
                AppLanguage.MALAYALAM -> "വാർത്തകൾ"
                AppLanguage.PUNJABI -> "ਖ਼ਬਰਾਂ"
                AppLanguage.ASSAMESE -> "বাতৰি"
                AppLanguage.SANTALI -> "ᱠᱷᱚᱵᱚᱨ"
                AppLanguage.URDU -> "خبریں"
                else -> "News"
            }
            Weather -> when (language) {
                AppLanguage.ODIA -> "ପାଣିପାଗ"
                AppLanguage.HINDI -> "मौसम"
                AppLanguage.BENGALI -> "আবহাওয়া"
                AppLanguage.TELUGU -> "వాతావరణం"
                AppLanguage.TAMIL -> "வானிலை"
                AppLanguage.MARATHI -> "हवामान"
                AppLanguage.GUJARATI -> "હવામાન"
                AppLanguage.KANNADA -> "ಹವಾಮಾನ"
                AppLanguage.MALAYALAM -> "കാലാവസ്ഥ"
                AppLanguage.PUNJABI -> "ਮੌਸਮ"
                AppLanguage.ASSAMESE -> "বতৰ"
                AppLanguage.SANTALI -> "ᱦᱚᱭ-ᱦᱤᱥᱤᱫ"
                AppLanguage.URDU -> "موسم"
                else -> "Weather"
            }
            Tourism -> when (language) {
                AppLanguage.ODIA -> "ପର୍ଯ୍ୟଟନ"
                AppLanguage.HINDI -> "पर्यटन"
                AppLanguage.BENGALI -> "পর্যটন"
                AppLanguage.TELUGU -> "పర్యాటకం"
                AppLanguage.TAMIL -> "சுற்றுலா"
                AppLanguage.MARATHI -> "पर्यटन"
                AppLanguage.GUJARATI -> "પ્રવાસન"
                AppLanguage.KANNADA -> "ಪ್ರವಾಸೋದ್ಯಮ"
                AppLanguage.MALAYALAM -> "ടൂറിസം"
                AppLanguage.PUNJABI -> "ਸੈਰ-ਸਪਾਟਾ"
                AppLanguage.ASSAMESE -> "ভ্ৰমণ"
                AppLanguage.SANTALI -> "ᱫᱟᱬᱟᱸ"
                AppLanguage.URDU -> "سیاحت"
                else -> "Tourism"
            }
            Profile -> when (language) {
                AppLanguage.ODIA -> "ପ୍ରୋଫାଇଲ୍"
                AppLanguage.HINDI -> "प्रोफ़ाइल"
                AppLanguage.BENGALI -> "প্রোফাইল"
                AppLanguage.TELUGU -> "ప్రొఫైల్"
                AppLanguage.TAMIL -> "சுயவிவரம்"
                AppLanguage.MARATHI -> "प्रोफाइल"
                AppLanguage.GUJARATI -> "પ્રોફાઇલ"
                AppLanguage.KANNADA -> "ಪ್ರೊಫೈಲ್"
                AppLanguage.MALAYALAM -> "പ്രൊഫൈൽ"
                AppLanguage.PUNJABI -> "ਪ੍ਰੋਫਾਈਲ"
                AppLanguage.ASSAMESE -> "প্রোফাইল"
                AppLanguage.SANTALI -> "ᱯᱨᱚᱯᱷᱟᱭᱤᱞ"
                AppLanguage.URDU -> "پروفائل"
                else -> "Profile"
            }
        }
    }

    companion object {
        val bottomNavRoutes: List<HashRoute>
            get() = listOf(News, Weather, Tourism, Profile)

        fun fromHash(hash: String?): HashRoute {
            val normalized = hash?.lowercase()?.trim()?.removePrefix("#") ?: return Tourism
            return when (normalized) {
                "news", "articles" -> News
                "weather", "alerts" -> Weather
                "tourism", "hotspots", "explore" -> Tourism
                "profile", "user", "account" -> Profile
                else -> Tourism
            }
        }

        fun fromTabIndex(tabIndex: Int): HashRoute {
            return when (tabIndex) {
                2 -> News
                3 -> Weather
                0 -> Tourism
                5 -> Profile
                else -> Tourism
            }
        }
    }
}

/**
 * Client-side hash routing manager for switching views without reloading or changing pages.
 * Handles backstack history and allows navigating via route object or hash string (e.g. '#news', '#weather').
 */
@Stable
class HashRouter(
    initialRoute: HashRoute = HashRoute.Tourism,
    private val onRouteChanged: (HashRoute) -> Unit = {}
) {
    var currentRoute by mutableStateOf(initialRoute)
        private set

    val history = mutableStateListOf(initialRoute)

    val currentHash: String
        get() = currentRoute.hash

    fun navigate(route: HashRoute) {
        if (currentRoute != route) {
            currentRoute = route
            history.add(route)
            onRouteChanged(route)
        }
    }

    fun navigate(hash: String) {
        val route = HashRoute.fromHash(hash)
        navigate(route)
    }

    fun pop(): Boolean {
        if (history.size > 1) {
            history.removeAt(history.lastIndex)
            val previous = history.last()
            currentRoute = previous
            onRouteChanged(previous)
            return true
        }
        return false
    }

    fun canGoBack(): Boolean = history.size > 1
}

@Composable
fun rememberHashRouter(
    initialRoute: HashRoute = HashRoute.Tourism,
    onRouteChanged: (HashRoute) -> Unit = {}
): HashRouter {
    return remember {
        HashRouter(initialRoute = initialRoute, onRouteChanged = onRouteChanged)
    }
}
