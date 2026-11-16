package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

import com.example.ui.viewmodel.ThemeMode
import androidx.compose.material3.ColorScheme

private val LightColorScheme = lightColorScheme(
    primary = OceanBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = OceanBlueDark,
    secondary = CoralOrange,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFEDD5),
    onSecondaryContainer = Color(0xFF9A3412),
    tertiary = EmeraldGreen,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD1FAE5),
    onTertiaryContainer = Color(0xFF065F46),
    error = Color(0xFFDC2626),
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B),
    background = BentoSlate50,
    onBackground = BentoSlate900,
    surface = Color.White,
    onSurface = BentoSlate900,
    surfaceVariant = BentoSlate100,
    onSurfaceVariant = BentoSlate700,
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF38BDF8),
    onPrimary = BentoSlate900,
    primaryContainer = Color(0xFF075985),
    onPrimaryContainer = Color(0xFFE0F2FE),
    secondary = Color(0xFFFB923C),
    onSecondary = BentoSlate900,
    secondaryContainer = Color(0xFF7C2D12),
    onSecondaryContainer = Color(0xFFFFEDD5),
    tertiary = Color(0xFF34D399),
    onTertiary = BentoSlate900,
    tertiaryContainer = Color(0xFF064E3B),
    onTertiaryContainer = Color(0xFFD1FAE5),
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2),
    background = BentoSlate900,
    onBackground = BentoSlate50,
    surface = BentoSlate800,
    onSurface = BentoSlate50,
    surfaceVariant = BentoSlate700,
    onSurfaceVariant = BentoSlate200,
    surfaceContainerLowest = Color(0xFF090D16),
    surfaceContainerLow = Color(0xFF0F172A),
    surfaceContainer = Color(0xFF1E293B),
    surfaceContainerHigh = Color(0xFF273549),
    surfaceContainerHighest = Color(0xFF334155),
    outline = Color(0xFF64748B),
    outlineVariant = Color(0xFF334155)
)

/**
 * High-Contrast OLED Dark Color Scheme for night-time reading and emergency alert visibility.
 * Uses pure pitch black (#000000) for zero screen glow and maximum OLED power efficiency,
 * ultra-vibrant emergency alert indicators (#EF4444 red, #F59E0B amber, #38BDF8 cyan)
 * ensuring life-saving alerts, cyclone sirens, and evacuation warnings remain prominently readable
 * in complete darkness without waking other family members.
 */
private val HighContrastDarkColorScheme = darkColorScheme(
    primary = Color(0xFF38BDF8),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF075985),
    onPrimaryContainer = Color(0xFFE0F2FE),
    secondary = Color(0xFFFBBF24), // Soothing warm amber for zero blue-light fatigue
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF78350F),
    onSecondaryContainer = Color(0xFFFEF3C7),
    tertiary = Color(0xFF34D399),
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF064E3B),
    onTertiaryContainer = Color(0xFFD1FAE5),
    error = Color(0xFFEF4444), // Vivid emergency alarm red
    onError = Color.Black,
    errorContainer = Color(0xFF991B1B),
    onErrorContainer = Color(0xFFFEF2F2),
    background = Color(0xFF000000), // AMOLED Pitch Black
    onBackground = Color(0xFFFFFFFF), // 21:1 Contrast
    surface = Color(0xFF0B1120), // Deep contrasting surface
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFFF1F5F9),
    surfaceContainerLowest = Color(0xFF000000),
    surfaceContainerLow = Color(0xFF070B14),
    surfaceContainer = Color(0xFF0F172A),
    surfaceContainerHigh = Color(0xFF1E293B),
    surfaceContainerHighest = Color(0xFF334155),
    outline = Color(0xFF64748B),
    outlineVariant = Color(0xFF334155)
)

data class BentoPalette(
    val isDark: Boolean = false,
    val isHighContrast: Boolean = false,
    val gold: Color = BentoGold,
    val tideIncoming: Color = TideIncoming,
    val tideReceding: Color = TideReceding,
    val cardBackground: Color = if (isDark) {
        if (isHighContrast) Color(0xFF0F172A) else BentoSlate800
    } else Color.White,
    val cardBorder: Color = if (isDark) {
        if (isHighContrast) Color(0xFF38BDF8).copy(alpha = 0.4f) else BentoSlate700
    } else BentoBorder,
    val textPrimary: Color = if (isDark) Color(0xFFFFFFFF) else BentoSlate900,
    val textSecondary: Color = if (isDark) Color(0xFFCBD5E1) else BentoSlate600,
    val textMuted: Color = if (isDark) Color(0xFF94A3B8) else BentoSlate500,
    val nightHighlightBg: Color = if (isHighContrast) Color(0xFF172554) else if (isDark) Color(0xFF1E293B) else Color(0xFFE0F2FE)
)

val LocalBentoPalette = androidx.compose.runtime.staticCompositionLocalOf { BentoPalette() }

data class AppThemeState(
    val themeMode: ThemeMode,
    val isDark: Boolean,
    val isHighContrast: Boolean,
    val colorScheme: ColorScheme,
    val onThemeModeChange: (ThemeMode) -> Unit,
    val onToggleDarkMode: () -> Unit = {}
)

val LocalAppTheme = androidx.compose.runtime.staticCompositionLocalOf {
    AppThemeState(
        themeMode = ThemeMode.SYSTEM,
        isDark = false,
        isHighContrast = false,
        colorScheme = LightColorScheme,
        onThemeModeChange = {},
        onToggleDarkMode = {}
    )
}

/**
 * Modern Material 3 Theme Provider for Balasore 360.
 * Dynamically adapts the entire UI tree between Light, Dark, High-Contrast OLED Dark,
 * and System-Default using semantic Material 3 design tokens.
 */
@Composable
fun AppThemeProvider(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.HIGH_CONTRAST_DARK -> true
    }
    val isHighContrast = themeMode == ThemeMode.HIGH_CONTRAST_DARK
    val colorScheme = when {
        isHighContrast -> HighContrastDarkColorScheme
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }
    val bentoPalette = BentoPalette(
        isDark = isDark,
        isHighContrast = isHighContrast
    )
    val toggleDarkAction = androidx.compose.runtime.remember(isDark, onThemeModeChange) {
        {
            val targetMode = if (isDark) ThemeMode.LIGHT else ThemeMode.DARK
            onThemeModeChange(targetMode)
        }
    }
    val appThemeState = androidx.compose.runtime.remember(themeMode, isDark, isHighContrast, colorScheme, toggleDarkAction) {
        AppThemeState(
            themeMode = themeMode,
            isDark = isDark,
            isHighContrast = isHighContrast,
            colorScheme = colorScheme,
            onThemeModeChange = onThemeModeChange,
            onToggleDarkMode = toggleDarkAction
        )
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalAppTheme provides appThemeState,
        LocalBentoPalette provides bentoPalette
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

@Composable
fun Balasore360Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    isHighContrast: Boolean = false,
    content: @Composable () -> Unit
) {
    val themeMode = when {
        isHighContrast -> ThemeMode.HIGH_CONTRAST_DARK
        darkTheme -> ThemeMode.DARK
        else -> ThemeMode.LIGHT
    }
    AppThemeProvider(
        themeMode = themeMode,
        onThemeModeChange = {},
        content = content
    )
}
