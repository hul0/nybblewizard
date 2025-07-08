package com.shred.it.ui.reusable

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets // Added import
import androidx.compose.foundation.layout.WindowInsetsSides // Added import
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only // Added import
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars // Added import
import androidx.compose.foundation.layout.windowInsetsPadding // Added import
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shred.it.ui.theme.ColorPalette
import com.shred.it.ui.theme.PaletteManager // Corrected import

@Composable
fun TopNavBar(
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    var showPaletteDialog by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            // This line adds padding dynamically to push content below the system status bar
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
            .padding(all = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Shredder",
            color = colors.primary,
            style = MaterialTheme.typography.displaySmall.copy(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.ExtraBold
            )
        )

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            // Theme toggle button
            IconButton(onClick = {
                PaletteManager.toggleDarkMode() // Use PaletteManager
            }) {
                Icon(
                    imageVector = if (PaletteManager.isDarkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode, // Use PaletteManager
                    contentDescription = "Toggle theme",
                    tint = colors.onSurfaceVariant
                )
            }

            // Color palette button
            IconButton(onClick = { showPaletteDialog = true }) {
                Icon(
                    Icons.Filled.Palette,
                    contentDescription = "Change color palette",
                    tint = colors.onSurfaceVariant
                )
            }

            // Settings button
            IconButton(onClick = onSettingsClick) {
                Icon(
                    Icons.Filled.Settings,
                    contentDescription = "Open Settings",
                    tint = colors.onSurfaceVariant
                )
            }
        }
    }

    // Color Palette Selection Dialog
    if (showPaletteDialog) {
        ColorPaletteDialog(
            onDismiss = { showPaletteDialog = false },
            onPaletteSelected = { palette ->
                PaletteManager.switchPalette(palette) // Use PaletteManager
                showPaletteDialog = false
            }
        )
    }
}

@Composable
private fun ColorPaletteDialog(
    onDismiss: () -> Unit,
    onPaletteSelected: (ColorPalette) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "COLOR PALETTES",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)

            ) {
                ColorPalette.values().forEach { palette ->
                    PaletteOption(
                        palette = palette,
                        isSelected = PaletteManager.currentPalette == palette, // Use PaletteManager
                        onClick = { onPaletteSelected(palette) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "DONE",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = FontFamily.SansSerif
                    )
                )
            }
        }
    )
}

@Composable
private fun PaletteOption(
    palette: ColorPalette,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    // Use PaletteManager.isDarkMode to get colors for preview consistency
    val paletteColors = palette.getColors(PaletteManager.isDarkMode)

    val buttonColors = if (isSelected) {
        ButtonDefaults.buttonColors(
            containerColor = colors.primaryContainer,
            contentColor = colors.onPrimaryContainer
        )
    } else {
        ButtonDefaults.outlinedButtonColors()
    }

    // Apply the correct button type based on selection
    if (isSelected) {
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            colors = buttonColors,
            shape = RoundedCornerShape(12.dp)
        ) {
            PaletteContent(palette, paletteColors)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            PaletteContent(palette, paletteColors)
        }
    }
}

@Composable
private fun PaletteContent(
    palette: ColorPalette,
    paletteColors: ColorPalette.PaletteColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = palette.displayName,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium
            )
        )

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            // Show preview colors
            ColorPreview(paletteColors.primary)
            ColorPreview(paletteColors.secondary)
            ColorPreview(paletteColors.tertiary)
        }
    }
}

@Composable
private fun ColorPreview(color: Color) {
    Canvas(
        modifier = Modifier.size(16.dp)
    ) {
        drawCircle(color = color)
    }
}