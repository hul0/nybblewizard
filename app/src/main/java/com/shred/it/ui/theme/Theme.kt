package com.shred.it.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// --- Updated Color Palette for a Flatter, Modern Look ---

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF00FFC2),              // Neon Mint (pops on dark, fun/cyber)
    onPrimary = Color(0xFF1B1B1F),            // Near-black for text/icons on neon
    primaryContainer = Color(0xFF00554B),     // Deep teal for contrast
    onPrimaryContainer = Color(0xFF9FFFF3),   // Soft mint text/icons

    secondary = Color(0xFFFF00B8),            // Neon pink/magenta (fun!)
    onSecondary = Color(0xFF1B1B1F),
    secondaryContainer = Color(0xFF37003C),   // Deep purple magenta
    onSecondaryContainer = Color(0xFFFFB3E6),

    tertiary = Color(0xFF00BFFF),             // Electric blue
    onTertiary = Color(0xFF1B1B1F),
    tertiaryContainer = Color(0xFF00374C),    // Deep blue
    onTertiaryContainer = Color(0xFF99E7FF),

    error = Color(0xFFFF6E6E),                // Bright error red
    onError = Color(0xFF1B1B1F),
    errorContainer = Color(0xFF470000),
    onErrorContainer = Color(0xFFFFB4A9),

    background = Color(0xFF161622),           // Deep purple-black (better than #0F0F14 for contrast)
    onBackground = Color(0xFFE9F8FA),         // Soft teal-white for text

    surface = Color(0xFF232334),              // Muted indigo
    onSurface = Color(0xFFE9F8FA),

    surfaceVariant = Color(0xFF2C2C40),       // Slightly lighter for cards
    onSurfaceVariant = Color(0xFFA0A3B1),     // Muted blue-gray

    outline = Color(0xFF00FFC2),              // Neon mint as accent outline
    outlineVariant = Color(0xFF00554B)        // Deep teal
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF7A8B99),            // Gentle blue-grey
    onPrimary = Color(0xFFF4F6F8),          // Very light for text on primary
    primaryContainer = Color(0xFFD1D8DD),   // Softer blue-grey
    onPrimaryContainer = Color(0xFF23272B),

    secondary = Color(0xFFB6A7D6),          // Lavender/muted purple
    onSecondary = Color(0xFF23272B),
    secondaryContainer = Color(0xFFECE7F6),
    onSecondaryContainer = Color(0xFF3E3C47),

    tertiary = Color(0xFFA8C3B6),           // Muted mint green
    onTertiary = Color(0xFF23272B),
    tertiaryContainer = Color(0xFFE6F4F0),
    onTertiaryContainer = Color(0xFF3E4B47),

    error = Color(0xFFDB6B6B),              // Muted brick red
    onError = Color(0xFFF4F6F8),
    errorContainer = Color(0xFFF9DEDE),
    onErrorContainer = Color(0xFF6E2727),

    background = Color(0xFFF5F6F8),         // Very light grey (not white!)
    onBackground = Color(0xFF26282A),       // Almost black, but not fully

    surface = Color(0xFFF8F9FA),            // Slightly lighter grey for cards/surfaces
    onSurface = Color(0xFF23272B),

    surfaceVariant = Color(0xFFE6E8EB),     // Card backgrounds, etc.
    onSurfaceVariant = Color(0xFF676A6D),   // Muted medium grey for text/icons

    outline = Color(0xFFD3D6D9),            // Subtle, soft border
    outlineVariant = Color(0xFFCFD3D6)
)

// --- Global Theme Preference (Consider DataStore for persistence) ---
object ThemePreferences {
    // Use only this value for global theme switching
    var isDarkMode by mutableStateOf(true)
}

// --- Typography ---
val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 52.sp,
        lineHeight = 60.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 44.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

@Composable
fun ShredItTheme(
    // If you want system default, pass useDarkTheme = null.
    // If you want to force a mode (e.g. for previews), provide true/false.
    content: @Composable () -> Unit
) {
    val isDark = ThemePreferences.isDarkMode
    val colors =   if (isDark) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        content = content
    )
}

// Helpers for usage in Composables (not required, but convenient)
object ShredderTheme {
    val isDark: Boolean
        @Composable
        get() = ThemePreferences.isDarkMode

    val colors: ColorScheme
        @Composable
        get() = MaterialTheme.colorScheme

    val typography: Typography
        @Composable
        get() = MaterialTheme.typography
}