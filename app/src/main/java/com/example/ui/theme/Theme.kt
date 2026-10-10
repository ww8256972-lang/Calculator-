package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.calculator.data.AppThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = DarkEqualsKey,
    onPrimary = Color.White,
    primaryContainer = DarkOperatorKey,
    onPrimaryContainer = Color.White,
    secondary = DarkAccentCyan,
    onSecondary = Color.Black,
    tertiary = DarkScientificText,
    background = DarkBg,
    onBackground = Color(0xFFF8FAFC),
    surface = DarkSurface,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF334155)
)

private val LightColorScheme = lightColorScheme(
    primary = LightEqualsKey,
    onPrimary = Color.White,
    primaryContainer = LightOperatorKey,
    onPrimaryContainer = Color.White,
    secondary = LightAccentCyan,
    onSecondary = Color.White,
    tertiary = LightScientificText,
    background = LightBg,
    onBackground = Color(0xFF0F172A),
    surface = LightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1)
)

@Composable
fun WaqarCalculatorTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
