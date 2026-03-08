package com.marymar.mobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = SurfaceWhite,
    secondary = SecondaryBlue,
    onSecondary = SurfaceWhite,
    tertiary = AccentOrange,
    onTertiary = SurfaceWhite,
    background = SoftBeige,
    onBackground = TextDark,
    surface = SurfaceWhite,
    onSurface = TextDark,
    error = ErrorRed,
    onError = SurfaceWhite,
    outline = BorderGray
)

private val DarkColors = darkColorScheme(
    primary = SecondaryBlue,
    onPrimary = SurfaceWhite,
    secondary = AccentOrange,
    onSecondary = SurfaceWhite,
    background = PrimaryBlue,
    onBackground = SurfaceWhite,
    surface = ColorTokens.cardDark,
    onSurface = SurfaceWhite,
    error = ErrorRed,
    onError = SurfaceWhite,
    outline = ColorTokens.outlineDark
)

private object ColorTokens {
    val cardDark = androidx.compose.ui.graphics.Color(0xFF133845)
    val outlineDark = androidx.compose.ui.graphics.Color(0xFF2B5564)
}

@Composable
fun MarymarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val typography = MaterialTheme.typography.copy(
        headlineLarge = TextStyle(
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            lineHeight = 36.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            lineHeight = 30.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            lineHeight = 28.sp
        ),
        titleLarge = TextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        ),
        titleMedium = TextStyle(
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp
        ),
        bodyLarge = TextStyle(
            fontSize = 16.sp,
            lineHeight = 22.sp
        ),
        bodyMedium = TextStyle(
            fontSize = 14.sp,
            lineHeight = 20.sp
        ),
        bodySmall = TextStyle(
            fontSize = 12.sp,
            lineHeight = 17.sp
        ),
        labelLarge = TextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    )

    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = typography,
        content = content
    )
}
