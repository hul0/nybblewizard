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
import androidx.compose.ui.text.font.FontFamily // Keep this import
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// --- Black and White Color Palette (with Red for Errors) ---

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFFFFF), // White
    onPrimary = Color(0xFF000000), // Black
    primaryContainer = Color(0xFF333333), // Dark Gray
    onPrimaryContainer = Color(0xFFFFFFFF), // White

    secondary = Color(0xFFCCCCCC), // Light Gray
    onSecondary = Color(0xFF000000), // Black
    secondaryContainer = Color(0xFF666666), // Medium Gray
    onSecondaryContainer = Color(0xFFFFFFFF), // White

    tertiary = Color(0xFF999999), // Medium-light Gray
    onTertiary = Color(0xFF000000), // Black
    tertiaryContainer = Color(0xFF444444), // Darker Gray
    onTertiaryContainer = Color(0xFFFFFFFF), // White

    error = Color(0xFFFF4444), // Bright, clear Red (retained)
    onError = Color(0xFFFFFFFF), // White
    errorContainer = Color(0xFF660000), // Dark Red
    onErrorContainer = Color(0xFFFFABAB), // Light Pinkish Red

    background = Color(0xFF000000), // Pure Black
    onBackground = Color(0xFFFFFFFF), // Pure White

    surface = Color(0xFF1A1A1A), // Very Dark Gray for elevated surfaces
    onSurface = Color(0xFFFFFFFF), // Pure White

    surfaceVariant = Color(0xFF333333), // Slightly Lighter Dark Gray for elevation
    onSurfaceVariant = Color(0xFFCCCCCC), // Light Gray

    outline = Color(0xFF666666), // Medium Gray for outlines
    outlineVariant = Color(0xFF333333) // Dark Gray for subtle outlines
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF000000), // Black
    onPrimary = Color(0xFFFFFFFF), // White
    primaryContainer = Color(0xFFCCCCCC), // Light Gray
    onPrimaryContainer = Color(0xFF000000), // Black

    secondary = Color(0xFF333333), // Dark Gray
    onSecondary = Color(0xFFFFFFFF), // White
    secondaryContainer = Color(0xFF999999), // Medium Gray
    onSecondaryContainer = Color(0xFF000000), // Black

    tertiary = Color(0xFF666666), // Medium Gray
    onTertiary = Color(0xFFFFFFFF), // White
    tertiaryContainer = Color(0xFFBBBBBB), // Lighter Gray
    onTertiaryContainer = Color(0xFF000000), // Black

    error = Color(0xFFCC0000), // Strong, Clear Red (retained)
    onError = Color(0xFFFFFFFF), // White
    errorContainer = Color(0xFFFFB0B0), // Light Pinkish Red
    onErrorContainer = Color(0xFF4A0000), // Dark Red text

    background = Color(0xFFFFFFFF), // Pure White
    onBackground = Color(0xFF000000), // Pure Black

    surface = Color(0xFFF0F0F0), // Off-white for elevated surfaces
    onSurface = Color(0xFF000000), // Pure Black

    surfaceVariant = Color(0xFFE0E0E0), // Light Gray for elevation
    onSurfaceVariant = Color(0xFF444444), // Dark Gray

    outline = Color(0xFF999999), // Medium Gray for outlines
    outlineVariant = Color(0xFFBBBBBB) // Lighter Gray for subtle outlines
)

// --- Global Theme Preference (Consider DataStore for persistence) ---
object ThemePreferences {
    var isDarkMode by mutableStateOf(false)
}

// --- Typography with alternative generic font families ---
val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif, // Changed from Default
        fontWeight = FontWeight.Bold,
        fontSize = 52.sp,
        lineHeight = 60.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.SansSerif, // Changed from Default
        fontWeight = FontWeight.Bold,
        fontSize = 44.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif, // Changed from Default
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Serif, // Example: Using Serif for headlines
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif, // Example: Using Serif for headlines
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Serif, // Example: Using Serif for headlines
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif, // Changed from Default
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif, // Changed from Default
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif, // Changed from Default
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default, // Stick to Default for main body text usually
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default, // Stick to Default for main body text usually
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default, // Stick to Default for main body text usually
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Monospace, // Example: Using Monospace for labels/buttons
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Monospace, // Example: Using Monospace for labels/buttons
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Monospace, // Example: Using Monospace for labels/buttons
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
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
        typography = AppTypography, // Now using generic system font families
        content = content
    )
}