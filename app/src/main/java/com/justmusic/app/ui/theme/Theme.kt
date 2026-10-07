package com.justmusic.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = PinkAccent,
    secondary = CyanAccent,
    background = DeepIndigoBg,
    surface = WarmPurpleBg,
    onPrimary = LightText,
    onBackground = LightText,
    onSurface = LightText
)

@Composable
fun JustMusicTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = CustomTypography,
        content = content
    )
}
