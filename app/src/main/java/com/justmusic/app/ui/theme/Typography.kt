package com.justmusic.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val SerifHeaderFontFamily = FontFamily.Serif

val CustomTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = SerifHeaderFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        color = LightText
    ),
    titleLarge = TextStyle(
        fontFamily = SerifHeaderFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        color = LightText
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        color = LightText
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        color = LightText
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = MutedText
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        color = MutedText
    )
)
