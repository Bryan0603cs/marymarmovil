package com.marymar.mobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val MarymarBaseColors = lightColorScheme(
    primary = Color(0xFF082B39),
    onPrimary = Color.White,
    secondary = Color(0xFF0F3B4C),
    onSecondary = Color.White,
    tertiary = Color(0xFFE9722A),
    onTertiary = Color.White,
    background = Color(0xFFF8F5EF),
    onBackground = Color(0xFF111111),
    surface = Color.White,
    onSurface = Color(0xFF111111),
    surfaceVariant = Color(0xFFF1EEE6),
    onSurfaceVariant = Color(0xFF1F2933),
    error = Color(0xFFB3261E),
    onError = Color.White,
    outline = Color(0xFF8A8F98)
)

private val MarymarHighContrastDark = darkColorScheme(
    primary = Color(0xFF89D7EE),
    onPrimary = Color(0xFF03161F),
    secondary = Color(0xFFFFCC99),
    onSecondary = Color(0xFF221003),
    tertiary = Color(0xFFFF9D57),
    onTertiary = Color(0xFF291100),
    background = Color(0xFF05080B),
    onBackground = Color(0xFFF5F7FA),
    surface = Color(0xFF10161C),
    onSurface = Color(0xFFF5F7FA),
    surfaceVariant = Color(0xFF18212A),
    onSurfaceVariant = Color(0xFFD8E0E8),
    error = Color(0xFFFF8A80),
    onError = Color(0xFF2B0000),
    outline = Color(0xFF93A1AF)
)

@Composable
fun MarymarTheme(
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
            fontSize = 25.sp * safeScale,
            lineHeight = 31.sp * safeScale
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

    val colors = if (highContrast) {
        MarymarHighContrastDark
    } else {
        MarymarBaseColors
    }

    MaterialTheme(
        colorScheme = colors,
        typography = typography,
        content = content
    )
}