package com.justmusic.app.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class AppThemeColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val cardBg: Color,
    val cardSubtle: Color,
    val primary: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val navBg: Color,
    val divider: Color,
    val heroGradientStart: Color,
    val heroGradientEnd: Color,
    val isDynamicMusicTheme: Boolean = false
)

val LocalAppThemeColors = staticCompositionLocalOf {
    AppThemeColors(
        isDark = true,
        background = DarkBackground,
        surface = DarkSurface,
        cardBg = DarkCardBg,
        cardSubtle = DarkCardSubtle,
        primary = ExpressiveBlue,
        textPrimary = DarkTextPrimary,
        textSecondary = DarkTextSecondary,
        navBg = DarkNavBg,
        divider = DarkDivider,
        heroGradientStart = Color(0xFF1E293B),
        heroGradientEnd = ExpressiveBlue,
        isDynamicMusicTheme = false
    )
}

@Composable
fun JustMusicTheme(
    themeMode: String = "SYSTEM", // "SYSTEM", "LIGHT", "DARK"
    dynamicColors: DynamicMusicColors? = null,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode.uppercase()) {
        "LIGHT" -> false
        "DARK" -> true
        else -> systemDark
    }

    val targetPrimary = dynamicColors?.primary ?: if (isDark) ExpressiveBlueLight else ExpressiveBlue
    val targetGradientStart = dynamicColors?.gradientTop ?: if (isDark) Color(0xFF182234) else Color(0xFF1E40AF)
    val targetGradientEnd = dynamicColors?.gradientBottom ?: if (isDark) ExpressiveBlue else Color(0xFF3B82F6)

    val targetBg = if (isDark) {
        dynamicColors?.darkBackground ?: DarkBackground
    } else {
        dynamicColors?.lightBackground ?: LightBackground
    }

    val targetCard = if (isDark) {
        dynamicColors?.darkCard ?: DarkCardBg
    } else {
        LightCardBg
    }

    val targetCardSubtle = if (dynamicColors != null) {
        targetPrimary.copy(alpha = if (isDark) 0.16f else 0.12f)
    } else {
        if (isDark) DarkCardSubtle else LightCardSubtle
    }

    val targetNavBg = if (isDark) {
        dynamicColors?.darkBackground ?: DarkNavBg
    } else {
        LightNavBg
    }

    // Animate color transitions smoothly so theme morphs gracefully when playing/changing tracks
    val animBg by animateColorAsState(targetBg, animationSpec = tween(450), label = "bg")
    val animSurface by animateColorAsState(targetCard, animationSpec = tween(450), label = "surface")
    val animCardBg by animateColorAsState(targetCard, animationSpec = tween(450), label = "card")
    val animCardSubtle by animateColorAsState(targetCardSubtle, animationSpec = tween(450), label = "cardSubtle")
    val animPrimary by animateColorAsState(targetPrimary, animationSpec = tween(450), label = "primary")
    val animNavBg by animateColorAsState(targetNavBg, animationSpec = tween(450), label = "navBg")
    val animGradStart by animateColorAsState(targetGradientStart, animationSpec = tween(450), label = "gradStart")
    val animGradEnd by animateColorAsState(targetGradientEnd, animationSpec = tween(450), label = "gradEnd")

    val customColors = AppThemeColors(
        isDark = isDark,
        background = animBg,
        surface = animSurface,
        cardBg = animCardBg,
        cardSubtle = animCardSubtle,
        primary = animPrimary,
        textPrimary = if (isDark) DarkTextPrimary else LightTextPrimary,
        textSecondary = if (isDark) DarkTextSecondary else LightTextSecondary,
        navBg = animNavBg,
        divider = if (isDark) DarkDivider else LightDivider,
        heroGradientStart = animGradStart,
        heroGradientEnd = animGradEnd,
        isDynamicMusicTheme = dynamicColors != null
    )

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = customColors.primary,
            secondary = PinkAccent,
            background = customColors.background,
            surface = customColors.surface,
            onPrimary = Color.White,
            onBackground = customColors.textPrimary,
            onSurface = customColors.textPrimary
        )
    } else {
        lightColorScheme(
            primary = customColors.primary,
            secondary = PinkAccent,
            background = customColors.background,
            surface = customColors.surface,
            onPrimary = Color.White,
            onBackground = customColors.textPrimary,
            onSurface = customColors.textPrimary
        )
    }

    CompositionLocalProvider(LocalAppThemeColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = CustomTypography,
            content = content
        )
    }
}
