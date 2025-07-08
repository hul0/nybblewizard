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

// --- Color Palette System ---
enum class ColorPalette(val displayName: String) {
    ELECTRIC_BLUE("Electric Blue"),
    NEON_GREEN("Neon Green"),
    SUNSET_ORANGE("Sunset Orange"),
    HOT_PINK("Hot Pink"),
    DEEP_PURPLE("Deep Purple"),
    CRIMSON_RED("Crimson Red"),
    TURQUOISE("Turquoise"),
    GOLDEN_YELLOW("Golden Yellow"),
    PASTEL_DREAM("Pastel Dream"),
    MONOCHROME("Monochrome");

    data class PaletteColors(
        val primary: Color,
        val secondary: Color,
        val tertiary: Color,
        val error: Color,
        val background: Color,
        val surface: Color,
        val onPrimary: Color,
        val onSecondary: Color,
        val onTertiary: Color,
        val onError: Color,
        val onBackground: Color,
        val onSurface: Color,
        val primaryContainer: Color,
        val secondaryContainer: Color,
        val tertiaryContainer: Color,
        val errorContainer: Color,
        val onPrimaryContainer: Color,
        val onSecondaryContainer: Color,
        val onTertiaryContainer: Color,
        val onErrorContainer: Color,
        val surfaceVariant: Color,
        val onSurfaceVariant: Color,
        val outline: Color,
        val outlineVariant: Color,
        val surfaceTint: Color,
        val inverseSurface: Color,
        val inverseOnSurface: Color,
        val inversePrimary: Color,
        val surfaceDim: Color,
        val surfaceBright: Color,
        val surfaceContainerLowest: Color,
        val surfaceContainerLow: Color,
        val surfaceContainer: Color,
        val surfaceContainerHigh: Color,
        val surfaceContainerHighest: Color
    )

    fun getColors(isDark: Boolean): PaletteColors {
        return when (this) {
            ELECTRIC_BLUE -> if (isDark) electricBlueDark else electricBlueLight
            NEON_GREEN -> if (isDark) neonGreenDark else neonGreenLight
            SUNSET_ORANGE -> if (isDark) sunsetOrangeDark else sunsetOrangeLight
            HOT_PINK -> if (isDark) hotPinkDark else hotPinkLight
            DEEP_PURPLE -> if (isDark) deepPurpleDark else deepPurpleLight
            CRIMSON_RED -> if (isDark) crimsonRedDark else crimsonRedLight
            TURQUOISE -> if (isDark) turquoiseDark else turquoiseLight
            GOLDEN_YELLOW -> if (isDark) goldenYellowDark else goldenYellowLight
            PASTEL_DREAM -> if (isDark) pastelDreamDark else pastelDreamLight
            MONOCHROME -> if (isDark) monochromeDark else monochromeLight
        }
    }

    companion object {
        // Electric Blue Palette
        private val electricBlueDark = PaletteColors(
            primary = Color(0xFF00D4FF),
            secondary = Color(0xFF40E0D0),
            tertiary = Color(0xFF87CEEB),
            error = Color(0xFFFF6B6B),
            background = Color(0xFF0B1426),
            surface = Color(0xFF1A1A2E),
            onPrimary = Color(0xFF0B1426),
            onSecondary = Color(0xFF0B1426),
            onTertiary = Color(0xFF0B1426),
            onError = Color(0xFFFFFFFF),
            onBackground = Color(0xFFF8F8FF),
            onSurface = Color(0xFFF8F8FF),
            primaryContainer = Color(0xFF003A5C),
            secondaryContainer = Color(0xFF1A4A47),
            tertiaryContainer = Color(0xFF2A4A5C),
            errorContainer = Color(0xFF5C1A1A),
            onPrimaryContainer = Color(0xFFB8EAFF),
            onSecondaryContainer = Color(0xFFB8F5F0),
            onTertiaryContainer = Color(0xFFE8F4F8),
            onErrorContainer = Color(0xFFFFB8B8),
            surfaceVariant = Color(0xFF36454F),
            onSurfaceVariant = Color(0xFFC0C0C0),
            outline = Color(0xFF708090),
            outlineVariant = Color(0xFF41424C),
            surfaceTint = Color(0xFF00D4FF),
            inverseSurface = Color(0xFFFFFAFA),
            inverseOnSurface = Color(0xFF0B1426),
            inversePrimary = Color(0xFF003A5C),
            surfaceDim = Color(0xFF131324),
            surfaceBright = Color(0xFF252540),
            surfaceContainerLowest = Color(0xFF0A0A1A),
            surfaceContainerLow = Color(0xFF161628),
            surfaceContainer = Color(0xFF1F1F35),
            surfaceContainerHigh = Color(0xFF2A2A42),
            surfaceContainerHighest = Color(0xFF35354F)
        )

        private val electricBlueLight = PaletteColors(
            primary = Color(0xFF0066CC),
            secondary = Color(0xFF006666),
            tertiary = Color(0xFF4A90E2),
            error = Color(0xFFD32F2F),
            background = Color(0xFFFFFAFA),
            surface = Color(0xFFFFFBFE),
            onPrimary = Color(0xFFFFFFFF),
            onSecondary = Color(0xFFFFFFFF),
            onTertiary = Color(0xFFFFFFFF),
            onError = Color(0xFFFFFFFF),
            onBackground = Color(0xFF1C1B1F),
            onSurface = Color(0xFF1C1B1F),
            primaryContainer = Color(0xFFE3F2FD),
            secondaryContainer = Color(0xFFE0F2F1),
            tertiaryContainer = Color(0xFFE8F4FD),
            errorContainer = Color(0xFFFFEBEE),
            onPrimaryContainer = Color(0xFF001F33),
            onSecondaryContainer = Color(0xFF001F1F),
            onTertiaryContainer = Color(0xFF001F33),
            onErrorContainer = Color(0xFF5C1A1A),
            surfaceVariant = Color(0xFFF4F3F7),
            onSurfaceVariant = Color(0xFF49454F),
            outline = Color(0xFF79747E),
            outlineVariant = Color(0xFFCAC4D0),
            surfaceTint = Color(0xFF0066CC),
            inverseSurface = Color(0xFF313033),
            inverseOnSurface = Color(0xFFF4EFF4),
            inversePrimary = Color(0xFF90CAF9),
            surfaceDim = Color(0xFFDDD8E1),
            surfaceBright = Color(0xFFFFFBFE),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFF7F2FA),
            surfaceContainer = Color(0xFFF1ECF4),
            surfaceContainerHigh = Color(0xFFECE6F0),
            surfaceContainerHighest = Color(0xFFE6E0E9)
        )

        // Neon Green Palette
        private val neonGreenDark = PaletteColors(
            primary = Color(0xFF00FF7F),
            secondary = Color(0xFF39FF14),
            tertiary = Color(0xFF32CD32),
            error = Color(0xFFFF4444),
            background = Color(0xFF0A1A0A),
            surface = Color(0xFF1A2E1A),
            onPrimary = Color(0xFF0A1A0A),
            onSecondary = Color(0xFF0A1A0A),
            onTertiary = Color(0xFF0A1A0A),
            onError = Color(0xFFFFFFFF),
            onBackground = Color(0xFFF0FFF0),
            onSurface = Color(0xFFF0FFF0),
            primaryContainer = Color(0xFF003A20),
            secondaryContainer = Color(0xFF1A5C10),
            tertiaryContainer = Color(0xFF2A5C20),
            errorContainer = Color(0xFF5C1A1A),
            onPrimaryContainer = Color(0xFFB8FFD4),
            onSecondaryContainer = Color(0xFFB8FF88),
            onTertiaryContainer = Color(0xFFB8FFB8),
            onErrorContainer = Color(0xFFFFB8B8),
            surfaceVariant = Color(0xFF2E4F2E),
            onSurfaceVariant = Color(0xFFC0E0C0),
            outline = Color(0xFF70A070),
            outlineVariant = Color(0xFF404A40),
            surfaceTint = Color(0xFF00FF7F),
            inverseSurface = Color(0xFFF0FFF0),
            inverseOnSurface = Color(0xFF0A1A0A),
            inversePrimary = Color(0xFF003A20),
            surfaceDim = Color(0xFF131A13),
            surfaceBright = Color(0xFF254025),
            surfaceContainerLowest = Color(0xFF0A120A),
            surfaceContainerLow = Color(0xFF16241A),
            surfaceContainer = Color(0xFF1F2E1F),
            surfaceContainerHigh = Color(0xFF2A382A),
            surfaceContainerHighest = Color(0xFF354235)
        )

        private val neonGreenLight = PaletteColors(
            primary = Color(0xFF2E7D32),
            secondary = Color(0xFF4CAF50),
            tertiary = Color(0xFF66BB6A),
            error = Color(0xFFD32F2F),
            background = Color(0xFFF8FFF8),
            surface = Color(0xFFFFFBFE),
            onPrimary = Color(0xFFFFFFFF),
            onSecondary = Color(0xFFFFFFFF),
            onTertiary = Color(0xFFFFFFFF),
            onError = Color(0xFFFFFFFF),
            onBackground = Color(0xFF1C1B1F),
            onSurface = Color(0xFF1C1B1F),
            primaryContainer = Color(0xFFE8F5E8),
            secondaryContainer = Color(0xFFE8F5E8),
            tertiaryContainer = Color(0xFFE8F5E8),
            errorContainer = Color(0xFFFFEBEE),
            onPrimaryContainer = Color(0xFF1B5E20),
            onSecondaryContainer = Color(0xFF2E7D32),
            onTertiaryContainer = Color(0xFF2E7D32),
            onErrorContainer = Color(0xFFB71C1C),
            surfaceVariant = Color(0xFFF0F7F0),
            onSurfaceVariant = Color(0xFF404A40),
            outline = Color(0xFF707A70),
            outlineVariant = Color(0xFFC0D0C0),
            surfaceTint = Color(0xFF2E7D32),
            inverseSurface = Color(0xFF313033),
            inverseOnSurface = Color(0xFFF4EFF4),
            inversePrimary = Color(0xFF81C784),
            surfaceDim = Color(0xFFD8E1D8),
            surfaceBright = Color(0xFFFFFBFE),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFF7FAF7),
            surfaceContainer = Color(0xFFF1F4F1),
            surfaceContainerHigh = Color(0xFFECEFEC),
            surfaceContainerHighest = Color(0xFFE6E9E6)
        )

        // Sunset Orange Palette
        private val sunsetOrangeDark = PaletteColors(
            primary = Color(0xFFFF6B35),
            secondary = Color(0xFFFF8C00),
            tertiary = Color(0xFFFFB347),
            error = Color(0xFFFF4444),
            background = Color(0xFF1A0A0A),
            surface = Color(0xFF2E1A1A),
            onPrimary = Color(0xFF1A0A0A),
            onSecondary = Color(0xFF1A0A0A),
            onTertiary = Color(0xFF1A0A0A),
            onError = Color(0xFFFFFFFF),
            onBackground = Color(0xFFFFF0F0),
            onSurface = Color(0xFFFFF0F0),
            primaryContainer = Color(0xFF5C2A10),
            secondaryContainer = Color(0xFF5C3A00),
            tertiaryContainer = Color(0xFF5C4A20),
            errorContainer = Color(0xFF5C1A1A),
            onPrimaryContainer = Color(0xFFFFD4B8),
            onSecondaryContainer = Color(0xFFFFE4B8),
            onTertiaryContainer = Color(0xFFFFF4D4),
            onErrorContainer = Color(0xFFFFB8B8),
            surfaceVariant = Color(0xFF4F3E2E),
            onSurfaceVariant = Color(0xFFE0D0C0),
            outline = Color(0xFFA08070),
            outlineVariant = Color(0xFF4A3A2A),
            surfaceTint = Color(0xFFFF6B35),
            inverseSurface = Color(0xFFFFF0F0),
            inverseOnSurface = Color(0xFF1A0A0A),
            inversePrimary = Color(0xFF5C2A10),
            surfaceDim = Color(0xFF1A1313),
            surfaceBright = Color(0xFF402525),
            surfaceContainerLowest = Color(0xFF120A0A),
            surfaceContainerLow = Color(0xFF241616),
            surfaceContainer = Color(0xFF2E1F1F),
            surfaceContainerHigh = Color(0xFF382A2A),
            surfaceContainerHighest = Color(0xFF423535)
        )

        private val sunsetOrangeLight = PaletteColors(
            primary = Color(0xFFE65100),
            secondary = Color(0xFFFF8F00),
            tertiary = Color(0xFFFFB74D),
            error = Color(0xFFD32F2F),
            background = Color(0xFFFFF8F0),
            surface = Color(0xFFFFFBFE),
            onPrimary = Color(0xFFFFFFFF),
            onSecondary = Color(0xFFFFFFFF),
            onTertiary = Color(0xFF000000),
            onError = Color(0xFFFFFFFF),
            onBackground = Color(0xFF1C1B1F),
            onSurface = Color(0xFF1C1B1F),
            primaryContainer = Color(0xFFFFF3E0),
            secondaryContainer = Color(0xFFFFF3E0),
            tertiaryContainer = Color(0xFFFFF8E1),
            errorContainer = Color(0xFFFFEBEE),
            onPrimaryContainer = Color(0xFFBF360C),
            onSecondaryContainer = Color(0xFFE65100),
            onTertiaryContainer = Color(0xFFE65100),
            onErrorContainer = Color(0xFFB71C1C),
            surfaceVariant = Color(0xFFF7F3F0),
            onSurfaceVariant = Color(0xFF4A3A30),
            outline = Color(0xFF7A6A60),
            outlineVariant = Color(0xFFD0C0B0),
            surfaceTint = Color(0xFFE65100),
            inverseSurface = Color(0xFF313033),
            inverseOnSurface = Color(0xFFF4EFF4),
            inversePrimary = Color(0xFFFFB74D),
            surfaceDim = Color(0xFFE1D8D0),
            surfaceBright = Color(0xFFFFFBFE),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFFAF7F2),
            surfaceContainer = Color(0xFFF4F1EC),
            surfaceContainerHigh = Color(0xFFEFECE7),
            surfaceContainerHighest = Color(0xFFE9E6E1)
        )

        // Hot Pink Palette
        private val hotPinkDark = PaletteColors(
            primary = Color(0xFFFF1B8D),
            secondary = Color(0xFFFF69B4),
            tertiary = Color(0xFFDA70D6),
            error = Color(0xFFFF4444),
            background = Color(0xFF1A0A1A),
            surface = Color(0xFF2E1A2E),
            onPrimary = Color(0xFF1A0A1A),
            onSecondary = Color(0xFF1A0A1A),
            onTertiary = Color(0xFF1A0A1A),
            onError = Color(0xFFFFFFFF),
            onBackground = Color(0xFFFFF0FF),
            onSurface = Color(0xFFFFF0FF),
            primaryContainer = Color(0xFF5C1A40),
            secondaryContainer = Color(0xFF5C2A50),
            tertiaryContainer = Color(0xFF5C3A60),
            errorContainer = Color(0xFF5C1A1A),
            onPrimaryContainer = Color(0xFFFFB8E8),
            onSecondaryContainer = Color(0xFFFFD4F0),
            onTertiaryContainer = Color(0xFFFFE4F8),
            onErrorContainer = Color(0xFFFFB8B8),
            surfaceVariant = Color(0xFF4F2E4F),
            onSurfaceVariant = Color(0xFFE0C0E0),
            outline = Color(0xFFA070A0),
            outlineVariant = Color(0xFF4A2A4A),
            surfaceTint = Color(0xFFFF1B8D),
            inverseSurface = Color(0xFFFFF0FF),
            inverseOnSurface = Color(0xFF1A0A1A),
            inversePrimary = Color(0xFF5C1A40),
            surfaceDim = Color(0xFF1A131A),
            surfaceBright = Color(0xFF402540),
            surfaceContainerLowest = Color(0xFF120A12),
            surfaceContainerLow = Color(0xFF241624),
            surfaceContainer = Color(0xFF2E1F2E),
            surfaceContainerHigh = Color(0xFF382A38),
            surfaceContainerHighest = Color(0xFF423542)
        )

        private val hotPinkLight = PaletteColors(
            primary = Color(0xFFC2185B),
            secondary = Color(0xFFE91E63),
            tertiary = Color(0xFFAD1457),
            error = Color(0xFFD32F2F),
            background = Color(0xFFFFF8FC),
            surface = Color(0xFFFFFBFE),
            onPrimary = Color(0xFFFFFFFF),
            onSecondary = Color(0xFFFFFFFF),
            onTertiary = Color(0xFFFFFFFF),
            onError = Color(0xFFFFFFFF),
            onBackground = Color(0xFF1C1B1F),
            onSurface = Color(0xFF1C1B1F),
            primaryContainer = Color(0xFFFCE4EC),
            secondaryContainer = Color(0xFFFCE4EC),
            tertiaryContainer = Color(0xFFF8BBD9),
            errorContainer = Color(0xFFFFEBEE),
            onPrimaryContainer = Color(0xFF880E4F),
            onSecondaryContainer = Color(0xFFAD1457),
            onTertiaryContainer = Color(0xFF880E4F),
            onErrorContainer = Color(0xFFB71C1C),
            surfaceVariant = Color(0xFFF7F0F7),
            onSurfaceVariant = Color(0xFF4A304A),
            outline = Color(0xFF7A607A),
            outlineVariant = Color(0xFFD0B0D0),
            surfaceTint = Color(0xFFC2185B),
            inverseSurface = Color(0xFF313033),
            inverseOnSurface = Color(0xFFF4EFF4),
            inversePrimary = Color(0xFFF48FB1),
            surfaceDim = Color(0xFFE1D0E1),
            surfaceBright = Color(0xFFFFFBFE),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFFAF2FA),
            surfaceContainer = Color(0xFFF4ECF4),
            surfaceContainerHigh = Color(0xFFEFE7EF),
            surfaceContainerHighest = Color(0xFFE9E1E9)
        )

        // Deep Purple Palette
        private val deepPurpleDark = PaletteColors(
            primary = Color(0xFF6A4C93),
            secondary = Color(0xFF9B59B6),
            tertiary = Color(0xFF8E44AD),
            error = Color(0xFFFF4444),
            background = Color(0xFF0A0A1A),
            surface = Color(0xFF1A1A2E),
            onPrimary = Color(0xFF0A0A1A),
            onSecondary = Color(0xFF0A0A1A),
            onTertiary = Color(0xFF0A0A1A),
            onError = Color(0xFFFFFFFF),
            onBackground = Color(0xFFF0F0FF),
            // Continuing from Deep Purple Dark palette...
            onSurface = Color(0xFFF0F0FF),
            primaryContainer = Color(0xFF3A2C5C),
            secondaryContainer = Color(0xFF4A3A70),
            tertiaryContainer = Color(0xFF4A3A60),
            errorContainer = Color(0xFF5C1A1A),
            onPrimaryContainer = Color(0xFFD4B8FF),
            onSecondaryContainer = Color(0xFFE4D4FF),
            onTertiaryContainer = Color(0xFFE4D4F0),
            onErrorContainer = Color(0xFFFFB8B8),
            surfaceVariant = Color(0xFF4F3E5F),
            onSurfaceVariant = Color(0xFFE0C0E0),
            outline = Color(0xFFA070B0),
            outlineVariant = Color(0xFF4A3A5A),
            surfaceTint = Color(0xFF6A4C93),
            inverseSurface = Color(0xFFF0F0FF),
            inverseOnSurface = Color(0xFF0A0A1A),
            inversePrimary = Color(0xFF3A2C5C),
            surfaceDim = Color(0xFF131324),
            surfaceBright = Color(0xFF252540),
            surfaceContainerLowest = Color(0xFF0A0A1A),
            surfaceContainerLow = Color(0xFF161628),
            surfaceContainer = Color(0xFF1F1F35),
            surfaceContainerHigh = Color(0xFF2A2A42),
            surfaceContainerHighest = Color(0xFF35354F)
        )

        private val deepPurpleLight = PaletteColors(
            primary = Color(0xFF512DA8),
            secondary = Color(0xFF7E57C2),
            tertiary = Color(0xFF673AB7),
            error = Color(0xFFD32F2F),
            background = Color(0xFFFFF8FF),
            surface = Color(0xFFFFFBFE),
            onPrimary = Color(0xFFFFFFFF),
            onSecondary = Color(0xFFFFFFFF),
            onTertiary = Color(0xFFFFFFFF),
            onError = Color(0xFFFFFFFF),
            onBackground = Color(0xFF1C1B1F),
            onSurface = Color(0xFF1C1B1F),
            primaryContainer = Color(0xFFEDE7F6),
            secondaryContainer = Color(0xFFE1BEE7),
            tertiaryContainer = Color(0xFFD1C4E9),
            errorContainer = Color(0xFFFFEBEE),
            onPrimaryContainer = Color(0xFF311B92),
            onSecondaryContainer = Color(0xFF4A148C),
            onTertiaryContainer = Color(0xFF4A148C),
            onErrorContainer = Color(0xFFB71C1C),
            surfaceVariant = Color(0xFFF3F0F7),
            onSurfaceVariant = Color(0xFF4A3A5A),
            outline = Color(0xFF7A6A8A),
            outlineVariant = Color(0xFFC0B0D0),
            surfaceTint = Color(0xFF512DA8),
            inverseSurface = Color(0xFF313033),
            inverseOnSurface = Color(0xFFF4EFF4),
            inversePrimary = Color(0xFF9C88FF),
            surfaceDim = Color(0xFFDDD8E1),
            surfaceBright = Color(0xFFFFFBFE),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFF7F2FA),
            surfaceContainer = Color(0xFFF1ECF4),
            surfaceContainerHigh = Color(0xFFECE6F0),
            surfaceContainerHighest = Color(0xFFE6E0E9)
        )

        // Crimson Red Palette
        private val crimsonRedDark = PaletteColors(
            primary = Color(0xFFDC143C),
            secondary = Color(0xFFFF4500),
            tertiary = Color(0xFFB22222),
            error = Color(0xFFFF6B6B),
            background = Color(0xFF1A0A0A),
            surface = Color(0xFF2E1A1A),
            onPrimary = Color(0xFFFFFFFF),
            onSecondary = Color(0xFFFFFFFF),
            onTertiary = Color(0xFFFFFFFF),
            onError = Color(0xFFFFFFFF),
            onBackground = Color(0xFFFFF0F0),
            onSurface = Color(0xFFFFF0F0),
            primaryContainer = Color(0xFF5C1A1A),
            secondaryContainer = Color(0xFF5C2A10),
            tertiaryContainer = Color(0xFF5C2020),
            errorContainer = Color(0xFF5C1A1A),
            onPrimaryContainer = Color(0xFFFFB8B8),
            onSecondaryContainer = Color(0xFFFFD4B8),
            onTertiaryContainer = Color(0xFFFFB8B8),
            onErrorContainer = Color(0xFFFFB8B8),
            surfaceVariant = Color(0xFF4F2E2E),
            onSurfaceVariant = Color(0xFFE0C0C0),
            outline = Color(0xFFA07070),
            outlineVariant = Color(0xFF4A2A2A),
            surfaceTint = Color(0xFFDC143C),
            inverseSurface = Color(0xFFFFF0F0),
            inverseOnSurface = Color(0xFF1A0A0A),
            inversePrimary = Color(0xFF5C1A1A),
            surfaceDim = Color(0xFF1A1313),
            surfaceBright = Color(0xFF402525),
            surfaceContainerLowest = Color(0xFF120A0A),
            surfaceContainerLow = Color(0xFF241616),
            surfaceContainer = Color(0xFF2E1F1F),
            surfaceContainerHigh = Color(0xFF382A2A),
            surfaceContainerHighest = Color(0xFF423535)
        )

        private val crimsonRedLight = PaletteColors(
            primary = Color(0xFFB71C1C),
            secondary = Color(0xFFD32F2F),
            tertiary = Color(0xFFE53935),
            error = Color(0xFFD32F2F),
            background = Color(0xFFFFF8F8),
            surface = Color(0xFFFFFBFE),
            onPrimary = Color(0xFFFFFFFF),
            onSecondary = Color(0xFFFFFFFF),
            onTertiary = Color(0xFFFFFFFF),
            onError = Color(0xFFFFFFFF),
            onBackground = Color(0xFF1C1B1F),
            onSurface = Color(0xFF1C1B1F),
            primaryContainer = Color(0xFFFFEBEE),
            secondaryContainer = Color(0xFFFFCDD2),
            tertiaryContainer = Color(0xFFFFCDD2),
            errorContainer = Color(0xFFFFEBEE),
            onPrimaryContainer = Color(0xFF8B0000),
            onSecondaryContainer = Color(0xFFB71C1C),
            onTertiaryContainer = Color(0xFFB71C1C),
            onErrorContainer = Color(0xFFB71C1C),
            surfaceVariant = Color(0xFFF7F0F0),
            onSurfaceVariant = Color(0xFF4A3030),
            outline = Color(0xFF7A6060),
            outlineVariant = Color(0xFFD0B0B0),
            surfaceTint = Color(0xFFB71C1C),
            inverseSurface = Color(0xFF313033),
            inverseOnSurface = Color(0xFFF4EFF4),
            inversePrimary = Color(0xFFFF8A80),
            surfaceDim = Color(0xFFE1D0D0),
            surfaceBright = Color(0xFFFFFBFE),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFFAF2F2),
            surfaceContainer = Color(0xFFF4ECEC),
            surfaceContainerHigh = Color(0xFFEFE7E7),
            surfaceContainerHighest = Color(0xFFE9E1E1)
        )

        // Turquoise Palette
        private val turquoiseDark = PaletteColors(
            primary = Color(0xFF40E0D0),
            secondary = Color(0xFF00CED1),
            tertiary = Color(0xFF48D1CC),
            error = Color(0xFFFF6B6B),
            background = Color(0xFF0A1A1A),
            surface = Color(0xFF1A2E2E),
            onPrimary = Color(0xFF0A1A1A),
            onSecondary = Color(0xFF0A1A1A),
            onTertiary = Color(0xFF0A1A1A),
            onError = Color(0xFFFFFFFF),
            onBackground = Color(0xFFF0FFFF),
            onSurface = Color(0xFFF0FFFF),
            primaryContainer = Color(0xFF205C5C),
            secondaryContainer = Color(0xFF1A5C5C),
            tertiaryContainer = Color(0xFF2A5C5C),
            errorContainer = Color(0xFF5C1A1A),
            onPrimaryContainer = Color(0xFFB8FFF8),
            onSecondaryContainer = Color(0xFFB8FFF8),
            onTertiaryContainer = Color(0xFFB8FFF8),
            onErrorContainer = Color(0xFFFFB8B8),
            surfaceVariant = Color(0xFF2E4F4F),
            onSurfaceVariant = Color(0xFFC0E0E0),
            outline = Color(0xFF70A0A0),
            outlineVariant = Color(0xFF404A4A),
            surfaceTint = Color(0xFF40E0D0),
            inverseSurface = Color(0xFFF0FFFF),
            inverseOnSurface = Color(0xFF0A1A1A),
            inversePrimary = Color(0xFF205C5C),
            surfaceDim = Color(0xFF131A1A),
            surfaceBright = Color(0xFF254040),
            surfaceContainerLowest = Color(0xFF0A1212),
            surfaceContainerLow = Color(0xFF162424),
            surfaceContainer = Color(0xFF1F2E2E),
            surfaceContainerHigh = Color(0xFF2A3838),
            surfaceContainerHighest = Color(0xFF354242)
        )

        private val turquoiseLight = PaletteColors(
            primary = Color(0xFF00695C),
            secondary = Color(0xFF00897B),
            tertiary = Color(0xFF26A69A),
            error = Color(0xFFD32F2F),
            background = Color(0xFFF8FFFF),
            surface = Color(0xFFFFFBFE),
            onPrimary = Color(0xFFFFFFFF),
            onSecondary = Color(0xFFFFFFFF),
            onTertiary = Color(0xFFFFFFFF),
            onError = Color(0xFFFFFFFF),
            onBackground = Color(0xFF1C1B1F),
            onSurface = Color(0xFF1C1B1F),
            primaryContainer = Color(0xFFE0F2F1),
            secondaryContainer = Color(0xFFB2DFDB),
            tertiaryContainer = Color(0xFFB2DFDB),
            errorContainer = Color(0xFFFFEBEE),
            onPrimaryContainer = Color(0xFF004D40),
            onSecondaryContainer = Color(0xFF00695C),
            onTertiaryContainer = Color(0xFF00695C),
            onErrorContainer = Color(0xFFB71C1C),
            surfaceVariant = Color(0xFFF0F7F7),
            onSurfaceVariant = Color(0xFF304A4A),
            outline = Color(0xFF607A7A),
            outlineVariant = Color(0xFFB0D0D0),
            surfaceTint = Color(0xFF00695C),
            inverseSurface = Color(0xFF313033),
            inverseOnSurface = Color(0xFFF4EFF4),
            inversePrimary = Color(0xFF80CBC4),
            surfaceDim = Color(0xFFD0E1E1),
            surfaceBright = Color(0xFFFFFBFE),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFF2FAF2),
            surfaceContainer = Color(0xFFECF4EC),
            surfaceContainerHigh = Color(0xFFE7EFE7),
            surfaceContainerHighest = Color(0xFFE1E9E1)
        )

        // Golden Yellow Palette
        private val goldenYellowDark = PaletteColors(
            primary = Color(0xFFFFD700),
            secondary = Color(0xFFFFA500),
            tertiary = Color(0xFFFFB347),
            error = Color(0xFFFF4444),
            background = Color(0xFF1A1A0A),
            surface = Color(0xFF2E2E1A),
            onPrimary = Color(0xFF1A1A0A),
            onSecondary = Color(0xFF1A1A0A),
            onTertiary = Color(0xFF1A1A0A),
            onError = Color(0xFFFFFFFF),
            onBackground = Color(0xFFFFFFF0),
            onSurface = Color(0xFFFFFFF0),
            primaryContainer = Color(0xFF5C5C20),
            secondaryContainer = Color(0xFF5C4A00),
            tertiaryContainer = Color(0xFF5C4A20),
            errorContainer = Color(0xFF5C1A1A),
            onPrimaryContainer = Color(0xFFFFF8B8),
            onSecondaryContainer = Color(0xFFFFF4B8),
            onTertiaryContainer = Color(0xFFFFF8D4),
            onErrorContainer = Color(0xFFFFB8B8),
            surfaceVariant = Color(0xFF4F4F2E),
            onSurfaceVariant = Color(0xFFE0E0C0),
            outline = Color(0xFFA0A070),
            outlineVariant = Color(0xFF4A4A2A),
            surfaceTint = Color(0xFFFFD700),
            inverseSurface = Color(0xFFFFFFF0),
            inverseOnSurface = Color(0xFF1A1A0A),
            inversePrimary = Color(0xFF5C5C20),
            surfaceDim = Color(0xFF1A1A13),
            surfaceBright = Color(0xFF404025),
            surfaceContainerLowest = Color(0xFF12120A),
            surfaceContainerLow = Color(0xFF242416),
            surfaceContainer = Color(0xFF2E2E1F),
            surfaceContainerHigh = Color(0xFF38382A),
            surfaceContainerHighest = Color(0xFF424235)
        )

        private val goldenYellowLight = PaletteColors(
            primary = Color(0xFFFF8F00),
            secondary = Color(0xFFFFB300),
            tertiary = Color(0xFFFFC107),
            error = Color(0xFFD32F2F),
            background = Color(0xFFFFFFF8),
            surface = Color(0xFFFFFBFE),
            onPrimary = Color(0xFFFFFFFF),
            onSecondary = Color(0xFF000000),
            onTertiary = Color(0xFF000000),
            onError = Color(0xFFFFFFFF),
            onBackground = Color(0xFF1C1B1F),
            onSurface = Color(0xFF1C1B1F),
            primaryContainer = Color(0xFFFFF8E1),
            secondaryContainer = Color(0xFFFFF8E1),
            tertiaryContainer = Color(0xFFFFF8E1),
            errorContainer = Color(0xFFFFEBEE),
            onPrimaryContainer = Color(0xFFE65100),
            onSecondaryContainer = Color(0xFFFF8F00),
            onTertiaryContainer = Color(0xFFFF8F00),
            onErrorContainer = Color(0xFFB71C1C),
            surfaceVariant = Color(0xFFF7F7F0),
            onSurfaceVariant = Color(0xFF4A4A30),
            outline = Color(0xFF7A7A60),
            outlineVariant = Color(0xFFD0D0B0),
            surfaceTint = Color(0xFFFF8F00),
            inverseSurface = Color(0xFF313033),
            inverseOnSurface = Color(0xFFF4EFF4),
            inversePrimary = Color(0xFFFFD54F),
            surfaceDim = Color(0xFFE1E1D0),
            surfaceBright = Color(0xFFFFFBFE),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFFAFAF2),
            surfaceContainer = Color(0xFFF4F4EC),
            surfaceContainerHigh = Color(0xFFEFEFE7),
            surfaceContainerHighest = Color(0xFFE9E9E1)
        )

        // Pastel Dream Palette
        private val pastelDreamDark = PaletteColors(
            primary = Color(0xFFFFB3E6),
            secondary = Color(0xFFE6B3FF),
            tertiary = Color(0xFFB3E6FF),
            error = Color(0xFFFF8A80),
            background = Color(0xFF1A1A1A),
            surface = Color(0xFF2E2E2E),
            onPrimary = Color(0xFF1A1A1A),
            onSecondary = Color(0xFF1A1A1A),
            onTertiary = Color(0xFF1A1A1A),
            onError = Color(0xFF000000),
            onBackground = Color(0xFFF8F8F8),
            onSurface = Color(0xFFF8F8F8),
            primaryContainer = Color(0xFF5C3A5C),
            secondaryContainer = Color(0xFF5C3A5C),
            tertiaryContainer = Color(0xFF3A5C5C),
            errorContainer = Color(0xFF5C2A2A),
            onPrimaryContainer = Color(0xFFFFD4F0),
            onSecondaryContainer = Color(0xFFE8D4FF),
            onTertiaryContainer = Color(0xFFD4E8FF),
            onErrorContainer = Color(0xFFFFD4D4),
            surfaceVariant = Color(0xFF4F4F4F),
            onSurfaceVariant = Color(0xFFE0E0E0),
            outline = Color(0xFFA0A0A0),
            outlineVariant = Color(0xFF4A4A4A),
            surfaceTint = Color(0xFFFFB3E6),
            inverseSurface = Color(0xFFF8F8F8),
            inverseOnSurface = Color(0xFF1A1A1A),
            inversePrimary = Color(0xFF5C3A5C),
            surfaceDim = Color(0xFF1A1A1A),
            surfaceBright = Color(0xFF404040),
            surfaceContainerLowest = Color(0xFF121212),
            surfaceContainerLow = Color(0xFF242424),
            surfaceContainer = Color(0xFF2E2E2E),
            surfaceContainerHigh = Color(0xFF383838),
            surfaceContainerHighest = Color(0xFF424242)
        )

        private val pastelDreamLight = PaletteColors(
            primary = Color(0xFFE91E63),
            secondary = Color(0xFF9C27B0),
            tertiary = Color(0xFF2196F3),
            error = Color(0xFFE57373),
            background = Color(0xFFFFF8FC),
            surface = Color(0xFFFFFBFE),
            onPrimary = Color(0xFFFFFFFF),
            onSecondary = Color(0xFFFFFFFF),
            onTertiary = Color(0xFFFFFFFF),
            onError = Color(0xFFFFFFFF),
            onBackground = Color(0xFF1C1B1F),
            onSurface = Color(0xFF1C1B1F),
            primaryContainer = Color(0xFFFCE4EC),
            secondaryContainer = Color(0xFFF3E5F5),
            tertiaryContainer = Color(0xFFE3F2FD),
            errorContainer = Color(0xFFFFEBEE),
            onPrimaryContainer = Color(0xFFAD1457),
            onSecondaryContainer = Color(0xFF4A148C),
            onTertiaryContainer = Color(0xFF0D47A1),
            onErrorContainer = Color(0xFFB71C1C),
            surfaceVariant = Color(0xFFF7F2F7),
            onSurfaceVariant = Color(0xFF4A4A4A),
            outline = Color(0xFF7A7A7A),
            outlineVariant = Color(0xFFD0D0D0),
            surfaceTint = Color(0xFFE91E63),
            inverseSurface = Color(0xFF313033),
            inverseOnSurface = Color(0xFFF4EFF4),
            inversePrimary = Color(0xFFF8BBD9),
            surfaceDim = Color(0xFFE1D8E1),
            surfaceBright = Color(0xFFFFFBFE),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFFAF2FA),
            surfaceContainer = Color(0xFFF4ECF4),
            surfaceContainerHigh = Color(0xFFEFE7EF),
            surfaceContainerHighest = Color(0xFFE9E1E9)
        )

        // Monochrome Palette
        private val monochromeDark = PaletteColors(
            primary = Color(0xFFFFFFFF),
            secondary = Color(0xFFE0E0E0),
            tertiary = Color(0xFFC0C0C0),
            error = Color(0xFFFF6B6B),
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E),
            onPrimary = Color(0xFF000000),
            onSecondary = Color(0xFF000000),
            onTertiary = Color(0xFF000000),
            onError = Color(0xFFFFFFFF),
            onBackground = Color(0xFFFFFFFF),
            onSurface = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFF404040),
            secondaryContainer = Color(0xFF606060),
            tertiaryContainer = Color(0xFF808080),
            errorContainer = Color(0xFF5C1A1A),
            onPrimaryContainer = Color(0xFFFFFFFF),
            onSecondaryContainer = Color(0xFFFFFFFF),
            onTertiaryContainer = Color(0xFFFFFFFF),
            onErrorContainer = Color(0xFFFFB8B8),
            surfaceVariant = Color(0xFF404040),
            onSurfaceVariant = Color(0xFFE0E0E0),
            outline = Color(0xFF808080),
            outlineVariant = Color(0xFF606060),
            surfaceTint = Color(0xFFFFFFFF),
            inverseSurface = Color(0xFFE0E0E0),
            inverseOnSurface = Color(0xFF121212),
            inversePrimary = Color(0xFF404040),
            surfaceDim = Color(0xFF1A1A1A),
            surfaceBright = Color(0xFF2D2D2D),
            surfaceContainerLowest = Color(0xFF0F0F0F),
            surfaceContainerLow = Color(0xFF1F1F1F),
            surfaceContainer = Color(0xFF2A2A2A),
            surfaceContainerHigh = Color(0xFF353535),
            surfaceContainerHighest = Color(0xFF404040)
        )

        private val monochromeLight = PaletteColors(
            primary = Color(0xFF000000),
            secondary = Color(0xFF424242),
            tertiary = Color(0xFF616161),
            error = Color(0xFFD32F2F),
            background = Color(0xFFFFFFFF),
            surface = Color(0xFFFFFBFE),
            onPrimary = Color(0xFFFFFFFF),
            onSecondary = Color(0xFFFFFFFF),
            onTertiary = Color(0xFFFFFFFF),
            onError = Color(0xFFFFFFFF),
            onBackground = Color(0xFF1C1B1F),
            onSurface = Color(0xFF1C1B1F),
            primaryContainer = Color(0xFFE0E0E0),
            secondaryContainer = Color(0xFFE0E0E0),
            tertiaryContainer = Color(0xFFE0E0E0),
            errorContainer = Color(0xFFFFEBEE),
            onPrimaryContainer = Color(0xFF000000),
            onSecondaryContainer = Color(0xFF000000),
            onTertiaryContainer = Color(0xFF000000),
            onErrorContainer = Color(0xFFB71C1C),
            surfaceVariant = Color(0xFFF5F5F5),
            onSurfaceVariant = Color(0xFF484848),
            outline = Color(0xFF797979),
            outlineVariant = Color(0xFFCACACA),
            surfaceTint = Color(0xFF000000),
            inverseSurface = Color(0xFF313033),
            inverseOnSurface = Color(0xFFF4EFF4),
            inversePrimary = Color(0xFFE0E0E0),
            surfaceDim = Color(0xFFDDD8E1),
            surfaceBright = Color(0xFFFFFBFE),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFF7F2FA),
            surfaceContainer = Color(0xFFF1ECF4),
            surfaceContainerHigh = Color(0xFFECE6F0),
            surfaceContainerHighest = Color(0xFFE6E0E9)
        )
    }
}

// Global state for current palette
object PaletteManager {
    var currentPalette by mutableStateOf(ColorPalette.ELECTRIC_BLUE)
    var isDarkMode by mutableStateOf(true)

    fun getCurrentColors(): ColorPalette.PaletteColors {
        return currentPalette.getColors(isDarkMode)
    }

    fun switchPalette(newPalette: ColorPalette) {
        currentPalette = newPalette
    }

    fun toggleDarkMode() {
        isDarkMode = !isDarkMode
    }
}

// Extension function to convert PaletteColors to ColorScheme
fun ColorPalette.PaletteColors.toColorScheme(): androidx.compose.material3.ColorScheme {
    return if (PaletteManager.isDarkMode) {
        darkColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            error = error,
            onError = onError,
            errorContainer = errorContainer,
            onErrorContainer = onErrorContainer,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            outline = outline,
            outlineVariant = outlineVariant,
            surfaceTint = surfaceTint,
            inverseSurface = inverseSurface,
            inverseOnSurface = inverseOnSurface,
            inversePrimary = inversePrimary,
            surfaceDim = surfaceDim,
            surfaceBright = surfaceBright,
            surfaceContainerLowest = surfaceContainerLowest,
            surfaceContainerLow = surfaceContainerLow,
            surfaceContainer = surfaceContainer,
            surfaceContainerHigh = surfaceContainerHigh,
            surfaceContainerHighest = surfaceContainerHighest
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            error = error,
            onError = onError,
            errorContainer = errorContainer,
            onErrorContainer = onErrorContainer,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            outline = outline,
            outlineVariant = outlineVariant,
            surfaceTint = surfaceTint,
            inverseSurface = inverseSurface,
            inverseOnSurface = inverseOnSurface,
            inversePrimary = inversePrimary,
            surfaceDim = surfaceDim,
            surfaceBright = surfaceBright,
            surfaceContainerLowest = surfaceContainerLowest,
            surfaceContainerLow = surfaceContainerLow,
            surfaceContainer = surfaceContainer,
            surfaceContainerHigh = surfaceContainerHigh,
            surfaceContainerHighest = surfaceContainerHighest
    )
}
}

// Custom typography for the app
val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
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
        lineHeight = 20.sp,
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

// Main theme composable
@Composable
fun ShredItTheme(
    darkTheme: Boolean = PaletteManager.isDarkMode,
    content: @Composable () -> Unit
) {
    val colors = PaletteManager.getCurrentColors().toColorScheme()

    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        content = content
    )
}

// Helper functions for theme utilities
object ThemeUtils {

    /**
     * Get all available color palettes
     */
    fun getAllPalettes(): List<ColorPalette> {
        return ColorPalette.values().toList()
    }

    /**
     * Get palette by name
     */
    fun getPaletteByName(name: String): ColorPalette? {
        return ColorPalette.values().find { it.name == name }
    }

    /**
     * Get next palette in sequence
     */
    fun getNextPalette(): ColorPalette {
        val currentIndex = ColorPalette.values().indexOf(PaletteManager.currentPalette)
        val nextIndex = (currentIndex + 1) % ColorPalette.values().size
        return ColorPalette.values()[nextIndex]
    }

    /**
     * Get previous palette in sequence
     */
    fun getPreviousPalette(): ColorPalette {
        val currentIndex = ColorPalette.values().indexOf(PaletteManager.currentPalette)
        val previousIndex = if (currentIndex == 0) ColorPalette.values().size - 1 else currentIndex - 1
        return ColorPalette.values()[previousIndex]
    }

    /**
     * Apply palette with animation support
     */
    fun applyPalette(palette: ColorPalette) {
        PaletteManager.switchPalette(palette)
    }

    /**
     * Get complementary color for better contrast
     */
    fun getComplementaryColor(color: Color): Color {
        return if (isColorDark(color)) Color.White else Color.Black
    }

    /**
     * Check if a color is considered dark
     */
    private fun isColorDark(color: Color): Boolean {
        val luminance = (0.299 * color.red + 0.587 * color.green + 0.114 * color.blue)
        return luminance < 0.5
    }

    /**
     * Generate a preview of all palettes
     */
    fun generatePalettePreview(): List<Pair<ColorPalette, ColorPalette.PaletteColors>> {
        return ColorPalette.values().map { palette ->
            palette to palette.getColors(PaletteManager.isDarkMode)
        }
    }
}

// Preview data class for color swatches
data class ColorSwatch(
    val name: String,
    val color: Color,
    val onColor: Color
)

// Extension functions for color manipulation
fun Color.lighten(factor: Float = 0.1f): Color {
    return Color(
        red = (red + (1f - red) * factor).coerceIn(0f, 1f),
        green = (green + (1f - green) * factor).coerceIn(0f, 1f),
        blue = (blue + (1f - blue) * factor).coerceIn(0f, 1f),
        alpha = alpha
    )
}

fun Color.darken(factor: Float = 0.1f): Color {
    return Color(
        red = (red * (1f - factor)).coerceIn(0f, 1f),
        green = (green * (1f - factor)).coerceIn(0f, 1f),
        blue = (blue * (1f - factor)).coerceIn(0f, 1f),
        alpha = alpha
    )
}

fun Color.adjustAlpha(alpha: Float): Color {
    return this.copy(alpha = alpha.coerceIn(0f, 1f))
}

// Theme preview helper
object ThemePreview {
    @Composable
    fun ColorPalettePreview(palette: ColorPalette) {
        val colors = palette.getColors(PaletteManager.isDarkMode)

        // This would typically contain preview UI components
        // showing how the palette looks in practice
    }

    fun getColorSwatches(palette: ColorPalette): List<ColorSwatch> {
        val colors = palette.getColors(PaletteManager.isDarkMode)
        return listOf(
            ColorSwatch("Primary", colors.primary, colors.onPrimary),
            ColorSwatch("Secondary", colors.secondary, colors.onSecondary),
            ColorSwatch("Tertiary", colors.tertiary, colors.onTertiary),
            ColorSwatch("Error", colors.error, colors.onError),
            ColorSwatch("Background", colors.background, colors.onBackground),
            ColorSwatch("Surface", colors.surface, colors.onSurface)
        )
    }
}

// Constants for theme configuration
object ThemeConstants {
    const val ANIMATION_DURATION_MS = 300
    const val PALETTE_TRANSITION_DELAY_MS = 150

    // Default spacing values
    val SPACING_EXTRA_SMALL = 4.sp
    val SPACING_SMALL = 8.sp
    val SPACING_MEDIUM = 16.sp
    val SPACING_LARGE = 24.sp
    val SPACING_EXTRA_LARGE = 32.sp

    // Corner radius values
    val CORNER_RADIUS_SMALL = 4.sp
    val CORNER_RADIUS_MEDIUM = 8.sp
    val CORNER_RADIUS_LARGE = 12.sp
    val CORNER_RADIUS_EXTRA_LARGE = 16.sp
}