package com.marymar.mobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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

private val HighContrastColors = lightColorScheme(
    primary = Color(0xFF082B39),
    onPrimary = Color.White,
    secondary = Color(0xFF0B536A),
    onSecondary = Color.White,
    tertiary = Color(0xFFAA4700),
    onTertiary = Color.White,
    background = Color(0xFFF8F5EF),
    onBackground = Color(0xFF111111),
    surface = Color.White,
    onSurface = Color(0xFF111111),
    error = Color(0xFFB00020),
    onError = Color.White,
    outline = Color(0xFF4B5563)
)

private val DarkColors = darkColorScheme(
    primary = SecondaryBlue,
    onPrimary = SurfaceWhite,
    secondary = AccentOrange,
    onSecondary = SurfaceWhite,
    background = PrimaryBlue,
    onBackground = SurfaceWhite,
    surface = Color(0xFF133845),
    onSurface = SurfaceWhite,
    error = ErrorRed,
    onError = SurfaceWhite,
    outline = Color(0xFF2B5564)
)

@Composable
fun MarymarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    fontScale: Float = 1f,
    highContrast: Boolean = false,
    content: @Composable () -> Unit
) {
    val safeScale = fontScale.coerceIn(0.90f, 1.35f)

    val typography = MaterialTheme.typography.copy(
        headlineLarge = TextStyle(
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp * safeScale,
            lineHeight = 36.sp * safeScale
        ),
        headlineMedium = TextStyle(
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp * safeScale,
            lineHeight = 30.sp * safeScale
        ),
        headlineSmall = TextStyle(
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp * safeScale,
            lineHeight = 28.sp * safeScale
        ),
        titleLarge = TextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp * safeScale
        ),
        titleMedium = TextStyle(
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp * safeScale
        ),
        bodyLarge = TextStyle(
            fontSize = 16.sp * safeScale,
            lineHeight = 22.sp * safeScale
        ),
        bodyMedium = TextStyle(
            fontSize = 14.sp * safeScale,
            lineHeight = 20.sp * safeScale
        ),
        bodySmall = TextStyle(
            fontSize = 12.sp * safeScale,
            lineHeight = 17.sp * safeScale
        ),
        labelLarge = TextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp * safeScale
        )
    )

    val colors = when {
        highContrast -> HighContrastColors
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colors,
        typography = typography,
        content = content
    )
}