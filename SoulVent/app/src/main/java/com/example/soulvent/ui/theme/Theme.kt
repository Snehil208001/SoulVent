package com.example.soulvent.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Define your color schemes
private val DefaultDarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val DefaultLightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

private val ForestColorScheme = lightColorScheme(
    primary = ForestGreen,
    secondary = ForestLightGreen,
    tertiary = ForestGreen
)

private val OceanColorScheme = lightColorScheme(
    primary = OceanBlue,
    secondary = OceanLightBlue,
    tertiary = OceanBlue
)

private val SunsetColorScheme = lightColorScheme(
    primary = SunsetOrange,
    secondary = SunsetLightOrange,
    tertiary = SunsetOrange
)

// A map to easily access your themes
val AppThemes = mapOf(
    "Default" to DefaultLightColorScheme,
    "Forest" to ForestColorScheme,
    "Ocean" to OceanColorScheme,
    "Sunset" to SunsetColorScheme
)

@Composable
fun SoulVentTheme(
    themeName: String = "Default", // Add a parameter to select the theme
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Select the color scheme based on the themeName
    val colorScheme = AppThemes[themeName] ?: DefaultLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}