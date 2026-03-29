package com.adrian.queuenote.ui.theme

import androidx.compose.ui.graphics.Color

// Premium Tech Palette - No logic changes
val PrimaryBlue = Color(0xFF007AFF)
val PrimaryIndigo = Color(0xFF5856D6)
val SuccessGreen = Color(0xFF34C759)
val WarningOrange = Color(0xFFFF9500)
val ErrorRed = Color(0xFFFF3B30)

// Dark Theme - True OLED
val DarkBackground = Color(0xFF000000)
val DarkSurface = Color(0xFF1C1C1E)
val DarkSurfaceVariant = Color(0xFF2C2C2E)
val DarkOnSurface = Color(0xFFFFFFFF)
val DarkOnSurfaceVariant = Color(0xFFEBEBF5).copy(alpha = 0.6f)

// Light Theme
val LightBackground = Color(0xFFF2F2F7)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFE5E5EA)
val LightOnSurface = Color(0xFF000000)
val LightOnSurfaceVariant = Color(0xFF3C3C43).copy(alpha = 0.6f)

// Legacy compatibility (only colors, no logic)
val Purple80 = PrimaryBlue
val PurpleGrey80 = PrimaryIndigo
val Pink80 = SuccessGreen
val Purple40 = PrimaryBlue
val PurpleGrey40 = PrimaryIndigo
val Pink40 = ErrorRed
