package com.shred.it.ui.reusable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.shred.it.core.ShredderSettings
import kotlinx.coroutines.launch

@Composable
fun SettingsDialog(
    settings: ShredderSettings,
    onUpdateRounds: (Int) -> Unit,
    onToggleRename: () -> Unit,
    onToggleVerify: () -> Unit,
    snackbarHostState: SnackbarHostState, // Pass SnackbarHostState to show messages
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope() // Use a local scope for snackbar messages

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("SHREDDER SETTINGS", style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.SansSerif)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SettingRow(
                    title = "Overwrite Rounds",
                    description = "${settings.overwriteRounds}",
                    icon = Icons.Filled.Loop
                ) {
                    val currentRounds = settings.overwriteRounds
                    val nextRounds = when (currentRounds) {
                        1 -> 3; 3 -> 5; 5 -> 7; 7 -> 10; 10 -> 15; else -> 1
                    }
                    onUpdateRounds(nextRounds)
                    scope.launch { snackbarHostState.showSnackbar("Overwrite rounds set to $nextRounds") }
                }
                SettingRow(
                    title = "Random Rename File",
                    description = if (settings.useRandomRename) "ENABLED" else "DISABLED",
                    icon = Icons.Filled.Edit
                ) {
                    onToggleRename()
                    scope.launch { snackbarHostState.showSnackbar("Random rename ${if (!settings.useRandomRename) "enabled" else "disabled"}") }
                }
                SettingRow(
                    title = "Verify Overwrites",
                    description = if (settings.verifyOverwrites) "ENABLED" else "DISABLED",
                    icon = Icons.Filled.VerifiedUser
                ) {
                    onToggleVerify()
                    scope.launch { snackbarHostState.showSnackbar("Verify overwrites ${if (!settings.verifyOverwrites) "enabled" else "disabled"}") }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("DONE", style = MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.SansSerif))
            }
        }
    )
}