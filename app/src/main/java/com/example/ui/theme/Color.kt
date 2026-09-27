package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Brand Colors inspired by the reference dark indigo/blue design
val ElectricSkyBlue = Color(0xFF00B4FF)
val ElectricCyan = Color(0xFF00D2FF)
val ElectricViolet = Color(0xFF7058FF)
val ElectricBlueDark = Color(0xFF0052D4)

val PulseEmerald = Color(0xFF10B981)
val PulseAmber = Color(0xFFF59E0B)
val PulseRose = Color(0xFFF43F5E)

// Aliases mapped to the new theme
val PulseCyan = ElectricSkyBlue
val PulsePurple = ElectricViolet

// Reference Theme Dark Palette
val DarkBackground = Color(0xFF0C1024)
val DarkBackgroundSecondary = Color(0xFF080B18)
val DarkSurface = Color(0xFF14192F)
val DarkSurfaceVariant = Color(0xFF1B213D)
val DarkCardBorder = Color(0xFF222B4C)
val DarkTextPrimary = Color(0xFFFFFFFF)
val DarkTextSecondary = Color(0xFF8E9BB5)

// Light Palette (fallback)
val LightBackground = Color(0xFFF1F5F9)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFE2E8F0)
val LightCardBorder = Color(0xFFCBD5E1)
val LightTextPrimary = Color(0xFF0F172A)
val LightTextSecondary = Color(0xFF64748B)

// Gauges & Gradients matching the reference image
val GaugeTrackInactive = Color(0xFF171D36)
val GaugeInnerRingColor = Color(0xFF1E2644)

val SpeedometerArcGradient = Brush.sweepGradient(
    listOf(
        Color(0xFF0052D4),
        Color(0xFF0091FF),
        Color(0xFF00D2FF),
        Color(0xFF00E5FF),
        Color(0xFF0052D4)
    )
)

val ButtonBorderBrush = Brush.horizontalGradient(
    listOf(
        Color(0xFF1E88E5),
        Color(0xFF00D2FF)
    )
)

val DownloadGradient = Brush.horizontalGradient(
    listOf(Color(0xFF0091FF), Color(0xFF00D2FF))
)
val UploadGradient = Brush.horizontalGradient(
    listOf(Color(0xFF7058FF), Color(0xFF9D8CFF))
)
