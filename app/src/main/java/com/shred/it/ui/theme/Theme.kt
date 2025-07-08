package com.shred.it.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// --- Black and White Color Palette (with Red for Errors) ---

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFFFFF), // Pure White
    onPrimary = Color(0xFF000000), // Pure Black
    primaryContainer = Color(0xFF222222), // Dark Gray for elevated primary elements (darker for contrast)
    onPrimaryContainer = Color(0xFFFFFFFF), // White text on primary container

    secondary = Color(0xFFCCCCCC), // Light Gray
    onSecondary = Color(0xFF000000), // Black
    secondaryContainer = Color(0xFF555555), // Medium Gray for elevated secondary elements
    onSecondaryContainer = Color(0xFFFFFFFF), // White text on secondary container

    tertiary = Color(0xFF999999), // Medium-light Gray
    onTertiary = Color(0xFF000000), // Black
    tertiaryContainer = Color(0xFF3B3B3B), // Darker Gray for elevated tertiary elements
    onTertiaryContainer = Color(0xFFFFFFFF), // White text on tertiary container

    error = Color(0xFFFF4444), // Bright, clear Red
    onError = Color(0xFF000000), // Black text on error
    errorContainer = Color(0xFF660000), // Dark Red container
    onErrorContainer = Color(0xFFFFABAB), // Light pinkish-red text on error container

    background = Color(0xFF000000), // Pure Black background
    onBackground = Color(0xFFFFFFFF), // Pure White text on background

    surface = Color(0xFF121212), // Very Dark Gray for elevated surfaces (slightly darker than 1A1A1A for more depth)
    onSurface = Color(0xFFFFFFFF), // Pure White text on surface

    surfaceVariant = Color(0xFF2D2D2D), // Slightly Lighter Dark Gray for subtle elevation/variants
    onSurfaceVariant = Color(0xFFCCCCCC), // Light Gray text on surface variant

    outline = Color(0xFF666666), // Medium Gray for outlines
    outlineVariant = Color(0xFF333333) // Dark Gray for subtle outlines
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0A0A0A), // Near Black (slightly off-black for depth)
    onPrimary = Color(0xFFFFFFFF), // Pure White
    primaryContainer = Color(0xFFEFEFEF), // Very light gray, almost white, for a clean look
    onPrimaryContainer = Color(0xFF0A0A0A), // Near Black text on primary container

    secondary = Color(0xFF333333), // Dark Gray (good contrast with black/white)
    onSecondary = Color(0xFFFFFFFF), // White
    secondaryContainer = Color(0xFFAAAAAA), // Medium Gray, slightly desaturated for balance
    onSecondaryContainer = Color(0xFF0A0A0A), // Near Black text on secondary container

    tertiary = Color(0xFF666666), // Medium Gray for accents
    onTertiary = Color(0xFFFFFFFF), // White
    tertiaryContainer = Color(0xFFDDDDDD), // Lighter gray for tertiary, providing subtle contrast
    onTertiaryContainer = Color(0xFF0A0A0A), // Near Black text on tertiary container

    error = Color(0xFFD32F2F), // Stronger, more vibrant Red
    onError = Color(0xFFFFFFFF), // White text on error
    errorContainer = Color(0xFFFFCDD2), // Lighter, more saturated pinkish red container
    onErrorContainer = Color(0xFFB71C1C), // Deeper red text on error container

    background = Color(0xFFFFFFFF), // Pure White background
    onBackground = Color(0xFF0A0A0A), // Near Black text on background

    surface = Color(0xFFF9F9F9), // Very light off-white for elevated surfaces, almost white
    onSurface = Color(0xFF0A0A0A), // Near Black text on surface

    surfaceVariant = Color(0xFFEAEAEA), // Light Gray for elevation, providing subtle lift
    onSurfaceVariant = Color(0xFF333333), // Dark Gray text on surface variant

    outline = Color(0xFF616161), // Slightly darker, more prominent gray for outlines
    outlineVariant = Color(0xFFBDBDBD) // Medium light gray, clearer for subtle outlines
)
// --- Global Theme Preference (Consider DataStore for persistence) ---
object ThemePreferences {
    var isDarkMode by mutableStateOf(true)
}

// --- Typography with alternative generic font families ---
val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 52.sp,
        lineHeight = 60.sp,
        letterSpacing = (-0.75).sp // Slightly more aggressive negative letter spacing for large titles
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 44.sp,
        lineHeight = 52.sp,
        letterSpacing = (-0.5).sp // Adjusted letter spacing
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.25).sp // Adjusted letter spacing
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp // Slightly adjusted letter spacing
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.15.sp // Slightly adjusted letter spacing
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif, // Explicitly set SansSerif
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif, // Explicitly set SansSerif
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif, // Explicitly set SansSerif
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Monospace, // Monospace for labels/buttons
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.2.sp // Slightly adjusted for monospace readability
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Monospace, // Monospace for labels/buttons
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.6.sp // Slightly adjusted for monospace readability
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Monospace, // Monospace for labels/buttons
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.6.sp // Slightly adjusted for monospace readability
    )
)

@Composable
fun ShredItTheme(
    content: @Composable () -> Unit
) {
    val isDark = ThemePreferences.isDarkMode
    val colors = if (isDark) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        content = content
    )
}