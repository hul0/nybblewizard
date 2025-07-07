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

// Dark Theme - Deeper background, slightly more vibrant primary for contrast
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF7B7EFF), // Slightly brighter violet for better pop on dark bg
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4A4AFF), // For subtle indicators or backgrounds
    onPrimaryContainer = Color(0xFFE0E0FF),

    secondary = Color(0xFFFF8A80), // iOS red, slightly softer
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFD9665F),
    onSecondaryContainer = Color.White,

    tertiary = Color(0xFF64FFDA), // Teal, brighter for accent
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF00BFA5),
    onTertiaryContainer = Color.Black,

    error = Color(0xFFFF5252), // Standard Material error red
    onError = Color.White,
    errorContainer = Color(0xFFD32F2F),
    onErrorContainer = Color.White,

    background = Color(0xFF0F0F14), // Very dark, almost black for depth
    onBackground = Color(0xFFEAEAEA), // Light gray for text

    surface = Color(0xFF1A1A20), // Dark gray, distinct from background but still dark
    onSurface = Color(0xFFEAEAEA), // Consistent light text color

    surfaceVariant = Color(0xFF2C2C33), // For cards and slightly elevated flat surfaces
    onSurfaceVariant = Color(0xFFC5C5C9), // Softer text/icon color for variants

    outline = Color(0xFF404045), // Subtle outlines if needed
    outlineVariant = Color(0xFF303035) // Even more subtle
)

// Light Theme - Clean, bright, with good contrast
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF5D5FEF), // Your original primary, good for light theme
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDADAFF), // Light container for primary accents
    onPrimaryContainer = Color(0xFF000056),

    secondary = Color(0xFFEF5350), // iOS red
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFCDD2),
    onSecondaryContainer = Color(0xFFB71C1C),

    tertiary = Color(0xFF26A69A), // Teal accent
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFB2DFDB),
    onTertiaryContainer = Color(0xFF004D40),

    error = Color(0xFFE53935), // Standard Material error red
    onError = Color.White,
    errorContainer = Color(0xFFFFCDD2),
    onErrorContainer = Color(0xFFB71C1C),

    background = Color(0xFFFCFCFF), // Very light, clean background
    onBackground = Color(0xFF1A1A1C), // Dark text for readability

    surface = Color(0xFFFFFFFF), // Pure white for main surfaces (like nav bar)
    onSurface = Color(0xFF1A1A1C), // Dark text on surface

    surfaceVariant = Color(0xFFF0F0F5), // Very light gray for cards (FlatCard)
    onSurfaceVariant = Color(0xFF454549), // Medium gray text/icons on variants

    outline = Color(0xFFD0D0D5), // Light outlines
    outlineVariant = Color(0xFFE0E0E5) // Very light outlines
)

// --- Global Theme Preference (Consider DataStore for persistence) ---
object ThemePreferences {
    var isDarkMode by mutableStateOf(false) // This will be used by the toggle
}

// --- Typography (Adjusted for a modern, clean look) ---
// Consider adding a custom font if you have one.
// For now, using system defaults with adjusted weights and sizes.
val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default, // Or your custom font
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
        fontWeight = FontWeight.SemiBold, // Slightly less heavy than display for better balance
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle( // Used in FAQScreen Title
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
    titleMedium = TextStyle( // Used in AboutScreen InfoSection title & FAQCard question
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold, // Good for section titles
        fontSize = 18.sp,                // Slightly larger for clarity
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle( // Could be used for smaller subheadings
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
    bodyMedium = TextStyle( // Used for general content text (AboutScreen InfoSection, FAQ answer)
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp, // Increased line height for readability
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
        fontSize = 12.sp, // Used in NavBar
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
    // Allow external control for dark mode, defaulting to system setting
    // and then overridden by ThemePreferences if the user has made a choice.
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val isEffectivelyDark = if (ThemePreferences.isDarkMode) true else useDarkTheme
    val colors = if (isEffectivelyDark) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography, // Use the more detailed AppTypography
        content = content
    )
}
