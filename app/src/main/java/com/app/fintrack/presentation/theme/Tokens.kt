package com.app.fintrack.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class FintrackVisuals(
    val primaryGradient: List<Color>,
    val navGradient: List<Color>,
    val avatarGradient: List<Color>,
    val chartGradient: List<Color>,
    val categoryPalette: List<Color>,
    val success: Color,
    val successContainer: Color,
    val danger: Color,
    val dangerContainer: Color,
    val info: Color,
    val infoContainer: Color,
)

private val OceanLightVisuals = FintrackVisuals(
    primaryGradient = listOf(Color(0xFF2B7EFF), Color(0xFF56B2FF)),
    navGradient = listOf(Color(0xFF2B7EFF), Color(0xFF56B2FF)),
    avatarGradient = listOf(Color(0xFF1E273C), Color(0xFF4E76FF)),
    chartGradient = listOf(Color(0xFF2B7EFF), Color(0xFF56B2FF)),
    categoryPalette = listOf(
        Color(0xFF39D26E),
        Color(0xFF22A3FF),
        Color(0xFFFF8D4D),
        Color(0xFFFF5B94),
        Color(0xFFB245FF),
    ),
    success = Color(0xFF2CB55E),
    successContainer = Color(0xFFEAF7EF),
    danger = Color(0xFFEF5A56),
    dangerContainer = Color(0xFFFFE4E4),
    info = Color(0xFF3A7BFF),
    infoContainer = Color(0xFFEAF2FF),
)

private val OceanDarkVisuals = FintrackVisuals(
    primaryGradient = listOf(Color(0xFF3F84FF), Color(0xFF67C1E9)),
    navGradient = listOf(Color(0xFF3F84FF), Color(0xFF67C1E9)),
    avatarGradient = listOf(Color(0xFF18304C), Color(0xFF4D8EFF)),
    chartGradient = listOf(Color(0xFF59D29B), Color(0xFF4D8EFF)),
    categoryPalette = listOf(
        Color(0xFF4CE58A),
        Color(0xFF45B2FF),
        Color(0xFFFFA56C),
        Color(0xFFFF6BA4),
        Color(0xFFC065FF),
    ),
    success = Color(0xFF73DFA0),
    successContainer = Color(0xFF17392B),
    danger = Color(0xFFFF8D87),
    dangerContainer = Color(0xFF432021),
    info = Color(0xFF8FC5FF),
    infoContainer = Color(0xFF162B46),
)

private val NoirLightVisuals = FintrackVisuals(
    primaryGradient = listOf(Color(0xFF1F6A57), Color(0xFF0F5444)),
    navGradient = listOf(Color(0xFF2C7864), Color(0xFF0E5A4A)),
    avatarGradient = listOf(Color(0xFF2A7A67), Color(0xFF0E5A4A)),
    chartGradient = listOf(Color(0xFFC86D72), Color(0xFFAA2E3E)),
    categoryPalette = listOf(
        Color(0xFF2A7A67),
        Color(0xFF5C8F54),
        Color(0xFFC89A4A),
        Color(0xFFAA2E3E),
        Color(0xFF7A8D55),
    ),
    success = Color(0xFF1D7A4A),
    successContainer = Color(0xFFE0EEDC),
    danger = Color(0xFFAA2E3E),
    dangerContainer = Color(0xFFF3D8DB),
    info = Color(0xFF0E5A4A),
    infoContainer = Color(0xFFDDE9DC),
)

private val NoirDarkVisuals = FintrackVisuals(
    primaryGradient = listOf(Color(0xFF347E66), Color(0xFF214E40)),
    navGradient = listOf(Color(0xFF478F75), Color(0xFF2D6550)),
    avatarGradient = listOf(Color(0xFF5EAF8D), Color(0xFF2A5E4A)),
    chartGradient = listOf(Color(0xFF7DD5A2), Color(0xFF4F8FC2)),
    categoryPalette = listOf(
        Color(0xFF73C49E),
        Color(0xFFB9CF99),
        Color(0xFFDDB974),
        Color(0xFFE18D98),
        Color(0xFFA9BC85),
    ),
    success = Color(0xFF8ED7AB),
    successContainer = Color(0xFF224034),
    danger = Color(0xFFFF9FA7),
    dangerContainer = Color(0xFF47252C),
    info = Color(0xFF89CBE1),
    infoContainer = Color(0xFF213B40),
)

internal val LocalFintrackVisuals = staticCompositionLocalOf { NoirLightVisuals }

object FintrackTokens {
    val visuals: FintrackVisuals
        @Composable get() = LocalFintrackVisuals.current
}

internal fun visualsFor(
    isDark: Boolean,
    preset: FintrackColorPreset,
): FintrackVisuals = when (preset) {
    FintrackColorPreset.NOIR -> if (isDark) NoirDarkVisuals else NoirLightVisuals
    FintrackColorPreset.OCEAN -> if (isDark) OceanDarkVisuals else OceanLightVisuals
}
