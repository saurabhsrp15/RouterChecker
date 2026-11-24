package com.example.routerchecker.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// High-Tech Cyber NOC Color Palette
val TechBackground = Color(0xFF090D16)
val TechSurface = Color(0xFF101726)
val TechSurfaceVariant = Color(0xFF172033)
val TechBorder = Color(0xFF1E2D4A)

val TechCyan = Color(0xFF00E5FF)
val TechCyanMuted = Color(0xFF0097A7)
val TechGreen = Color(0xFF00E676)
val TechGreenMuted = Color(0xFF1B5E20)
val TechAmber = Color(0xFFFFD600)
val TechRed = Color(0xFFFF1744)
val TechPurple = Color(0xFF7C4DFF)

val TechTextPrimary = Color(0xFFF0F4F8)
val TechTextSecondary = Color(0xFF94A3B8)
val TechTextMuted = Color(0xFF64748B)

private val TechDarkColorScheme = darkColorScheme(
    primary = TechCyan,
    onPrimary = Color(0xFF002029),
    primaryContainer = TechSurfaceVariant,
    onPrimaryContainer = TechCyan,
    secondary = TechPurple,
    onSecondary = Color.White,
    background = TechBackground,
    onBackground = TechTextPrimary,
    surface = TechSurface,
    onSurface = TechTextPrimary,
    surfaceVariant = TechSurfaceVariant,
    onSurfaceVariant = TechTextSecondary,
    outline = TechBorder,
    error = TechRed,
    onError = Color.White
)

@Composable
fun TechnicalRouterTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TechDarkColorScheme,
        content = content
    )
}
