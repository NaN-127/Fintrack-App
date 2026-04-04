package com.app.fintrack.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.app.fintrack.domain.model.ThemeMode

private val OceanLightScheme = lightColorScheme(
    primary = Color(0xFF1E5BFF),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDEE7FF),
    onPrimaryContainer = Color(0xFF0A1E5A),
    secondary = Color(0xFF3C8DFF),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDAE8FF),
    onSecondaryContainer = Color(0xFF0A254F),
    tertiary = Color(0xFF19B974),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFF3F4F8),
    onBackground = Color(0xFF141B2D),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF141B2D),
    surfaceVariant = Color(0xFFF0F2F8),
    onSurfaceVariant = Color(0xFF5B647A),
    outline = Color(0xFFCDD3E0),
    error = Color(0xFFCC2B2B),
    onError = Color(0xFFFFFFFF),
)

private val OceanDarkScheme = darkColorScheme(
    primary = Color(0xFF9CC0FF),
    onPrimary = Color(0xFF071B4D),
    primaryContainer = Color(0xFF173977),
    onPrimaryContainer = Color(0xFFE2EBFF),
    secondary = Color(0xFF82D0C4),
    onSecondary = Color(0xFF072923),
    secondaryContainer = Color(0xFF173F3A),
    onSecondaryContainer = Color(0xFFD8F5EF),
    tertiary = Color(0xFF79D9A1),
    onTertiary = Color(0xFF0A301E),
    background = Color(0xFF08111D),
    onBackground = Color(0xFFF1F5FB),
    surface = Color(0xFF101A28),
    onSurface = Color(0xFFF1F5FB),
    surfaceVariant = Color(0xFF182334),
    onSurfaceVariant = Color(0xFFD0D8E6),
    outline = Color(0xFF46546B),
    error = Color(0xFFFF8F8A),
    onError = Color(0xFF3B0D0D),
)

private val NoirLightScheme = lightColorScheme(
    primary = Color(0xFF0E5A4A),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDCE8D0),
    onPrimaryContainer = Color(0xFF0A3E34),
    secondary = Color(0xFF2A7A67),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE4EEE0),
    onSecondaryContainer = Color(0xFF143D33),
    tertiary = Color(0xFF5C8F54),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFF1F0DC),
    onBackground = Color(0xFF123B33),
    surface = Color(0xFFFAF8E8),
    onSurface = Color(0xFF123B33),
    surfaceVariant = Color(0xFFE7E6CF),
    onSurfaceVariant = Color(0xFF4A5E55),
    outline = Color(0xFFBCC7B6),
    error = Color(0xFFAA2E3E),
    onError = Color(0xFFFFFFFF),
)

private val NoirDarkScheme = darkColorScheme(
    primary = Color(0xFF6FBE9D),
    onPrimary = Color(0xFF05241A),
    primaryContainer = Color(0xFF1E4C3B),
    onPrimaryContainer = Color(0xFFD8F6E8),
    secondary = Color(0xFF9FC7AF),
    onSecondary = Color(0xFF10271D),
    secondaryContainer = Color(0xFF2B4338),
    onSecondaryContainer = Color(0xFFE0F0E7),
    tertiary = Color(0xFF87D7A8),
    onTertiary = Color(0xFF0A2C1A),
    background = Color(0xFF0B120F),
    onBackground = Color(0xFFF0F6F2),
    surface = Color(0xFF121B17),
    onSurface = Color(0xFFF0F6F2),
    surfaceVariant = Color(0xFF1C2823),
    onSurfaceVariant = Color(0xFFD0DCD5),
    outline = Color(0xFF50605A),
    error = Color(0xFFFFA3AF),
    onError = Color(0xFF3B0E16),
)

private fun colorsFor(
    isDark: Boolean,
    preset: FintrackColorPreset,
): ColorScheme = when (preset) {
    FintrackColorPreset.NOIR -> if (isDark) NoirDarkScheme else NoirLightScheme
    FintrackColorPreset.OCEAN -> if (isDark) OceanDarkScheme else OceanLightScheme
}

@Composable
fun FintrackTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    colorPreset: FintrackColorPreset = FintrackThemeConfig.activePreset,
    content: @Composable () -> Unit,
) {
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val colors = colorsFor(isDark = isDark, preset = colorPreset)
    CompositionLocalProvider(
        LocalFintrackVisuals provides visualsFor(isDark = isDark, preset = colorPreset),
        LocalFintrackSpacing provides FintrackSpacing(),
    ) {
        MaterialTheme(
            colorScheme = colors,
            typography = FintrackTypography,
            content = content,
        )
    }
}
