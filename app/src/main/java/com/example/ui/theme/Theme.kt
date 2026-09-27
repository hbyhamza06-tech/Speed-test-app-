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

private val DarkColorScheme = darkColorScheme(
    primary = ElectricSkyBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF00366D),
    onPrimaryContainer = Color(0xFFCCE5FF),
    secondary = ElectricViolet,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF382C80),
    onSecondaryContainer = Color(0xFFE3DFFF),
    tertiary = PulseEmerald,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkCardBorder,
    error = PulseRose
)

private val LightColorScheme = lightColorScheme(
    primary = ElectricSkyBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCCEDFF),
    onPrimaryContainer = Color(0xFF001F3F),
    secondary = ElectricViolet,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE9E5FE),
    onSecondaryContainer = Color(0xFF281E60),
    tertiary = PulseEmerald,
    background = DarkBackground, // Preserve the sleek dark theme by default
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkCardBorder,
    error = PulseRose
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to true to match user's theme request
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
