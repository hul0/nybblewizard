package com.shred.it.ui.screens

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shred.it.core.LogType
import com.shred.it.core.ShredderState
import com.shred.it.ui.reusable.FilePreviewCard // Import the new FilePreviewCard
import com.shred.it.ui.viewmodel.FileShredderViewModel // Import the new ViewModel

import kotlinx.coroutines.launch


@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun FileShredderScreen(
    innerPadding: FileShredderViewModel, // Accept innerPadding from Scaffold
    vm: FileShredderViewModel = viewModel()
) {
    val scrollState = rememberScrollState()
    val colors = MaterialTheme.colorScheme
    val ctx = LocalContext.current
    val state by vm.state.collectAsState()
    val prog by vm.progress.collectAsState()
    val logs by vm.logs.collectAsState()
    val file by vm.file.collectAsState()
    val set by vm.settings.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showConf by remember { mutableStateOf(false) }
    var confStep by remember { mutableIntStateOf(0) }
    var logsExpanded by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val isTablet = screenWidth >= 600.dp

    val contentHorizontalPadding = if (isTablet) screenWidth * 0.1f else 20.dp
    val contentVerticalPadding = if (isTablet) 32.dp else 20.dp

    val pick = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            vm.selectFile(ctx, it)
            scope.launch {
                snackbarHostState.showSnackbar("File loaded successfully")
            }
        }
    }

    LaunchedEffect(state) {
        when (state) {
            ShredderState.OVERWRITING -> scope.launch { snackbarHostState.showSnackbar("Shredding Started: Secure file destruction in progress") }
            ShredderState.COMPLETE -> scope.launch { snackbarHostState.showSnackbar("Shredding Complete: File has been securely destroyed") }
            ShredderState.ERROR -> scope.launch { snackbarHostState.showSnackbar("Shredding Failed: An error occurred") }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            // Apply the Scaffold's padding correctly.
            .verticalScroll(scrollState)
            // Apply only horizontal padding here to avoid compounding vertical padding.
            .padding(horizontal = contentHorizontalPadding)
    ) {
        // Spacer to create the desired padding at the top of the content.
        Spacer(Modifier.height(contentVerticalPadding))

        // --- File Selection/Info Card ---
        // Conditionally display FilePreviewCard if a file is selected
        file?.let { selectedFile ->
            FilePreviewCard(
                file = selectedFile,
                onClearFile = {
                    vm.clearFile()
                    scope.launch { snackbarHostState.showSnackbar("File removed from queue") }
                },
                formatFileSize = vm::formatFileSize // Pass the formatting function from ViewModel
            )
        }


        // --- Action Buttons ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ElevatedButton(
                onClick = { pick.launch(arrayOf("*/*")) },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp),
                enabled = state == ShredderState.IDLE || state == ShredderState.FILE_SELECTED || state == ShredderState.COMPLETE || state == ShredderState.ERROR
            ) {
                Icon(Icons.AutoMirrored.Filled.InsertDriveFile, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("SELECT", style = MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.SansSerif))
            }

            ElevatedButton(
                onClick = {
                    if (file != null) {
                        confStep = 0
                        showConf = true
                    } else {
                        scope.launch { snackbarHostState.showSnackbar("Please select a file to shred first") }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.elevatedButtonColors(containerColor = colors.error),
                elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp),
                enabled = file != null && (state == ShredderState.IDLE || state == ShredderState.FILE_SELECTED)
            ) {
                Icon(Icons.Filled.DeleteForever, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (state == ShredderState.OVERWRITING) "SHREDDING" else "SHRED", style = MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.SansSerif))
            }
        }

        // --- Progress Indicator ---
        if (prog != null && (state == ShredderState.OVERWRITING || state == ShredderState.VERIFYING || state == ShredderState.RENAMING || state == ShredderState.DELETING)) {
            prog?.let { p ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 3.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Round ${p.currentRound}/${p.totalRounds}", style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.SansSerif))
                            Text("${p.progress}%", color = colors.error, style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.SansSerif))
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { p.progress / 100f },
                            modifier = Modifier.fillMaxWidth(),
                            color = colors.error,
                            trackColor = colors.errorContainer
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = p.currentOperation,
                            color = colors.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.SansSerif),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // --- Activity Logs Section (Collapsible) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { logsExpanded = !logsExpanded }
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "ACTIVITY LOGS",
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold),
                        color = colors.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (logsExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = if (logsExpanded) "Collapse logs" else "Expand logs",
                        tint = colors.onSurfaceVariant
                    )
                }

                TextButton(
                    onClick = {
                        vm.clearLogs()
                        scope.launch { snackbarHostState.showSnackbar("All log entries have been removed") }
                    },
                    enabled = logs.isNotEmpty()
                ) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear Logs", modifier = Modifier.size(18.dp), tint = colors.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear Logs", style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.SansSerif))
                }
            }

            // Animated visibility for the logs content
            AnimatedVisibility(
                visible = logsExpanded,
                enter = expandVertically(expandFrom = Alignment.Top),
                exit = shrinkVertically(shrinkTowards = Alignment.Top)
            ) {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 150.dp, max = 300.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = colors.surface,
                        contentColor = colors.onSurface
                    )
                ) {
                    if (logs.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "No activity yet.",
                                color = colors.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.SansSerif)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            reverseLayout = true
                        ) {
                            items(logs.reversed()) { log ->
                                val logColor = when (log.type) {
                                    LogType.ERROR -> colors.error
                                    LogType.WARNING -> colors.secondary
                                    LogType.SUCCESS -> colors.primary
                                    LogType.PROGRESS -> colors.onSurfaceVariant
                                    LogType.INFO -> colors.onSurfaceVariant
                                    LogType.SECURITY -> colors.error
                                }
                                val logIcon: ImageVector = when (log.type) {
                                    LogType.ERROR -> Icons.Filled.Error
                                    LogType.WARNING -> Icons.Filled.Warning
                                    LogType.SUCCESS -> Icons.Filled.CheckCircle
                                    LogType.PROGRESS -> Icons.Filled.Sync
                                    LogType.INFO -> Icons.Filled.Info
                                    LogType.SECURITY -> Icons.Filled.Security
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = logIcon,
                                        contentDescription = log.type.name,
                                        tint = logColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = log.message,
                                        color = logColor,
                                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.SansSerif)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Information/Warning Section ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 3.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Filled.Warning, contentDescription = "Warning", tint = colors.tertiary)
                    Text(
                        text = "Once a file is shredded, its data is irreversibly destroyed and cannot be recovered.",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.SansSerif),
                        color = colors.onSurfaceVariant.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // Spacer to create the desired padding at the bottom of the content.
        Spacer(Modifier.height(contentVerticalPadding))
    }


    // --- AlertDialogs (Confirm Shred, Settings) ---
    if (showConf) {
        AlertDialog(
            onDismissRequest = { showConf = false },
            title = {
                Text(
                    if (confStep == 0) "CONFIRM SHRED" else "FINAL WARNING",
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.SansSerif, color = colors.error)
                )
            },
            text = {
                Text(
                    if (confStep == 0) "This will permanently destroy the selected file. This action is irreversible. Are you sure you want to proceed?"
                    else "The file will be unrecoverable after this operation. THIS IS YOUR LAST CHANCE TO CANCEL. By destroying the file you agree to our terms of service .",
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.SansSerif)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (confStep == 0) {
                            confStep = 1
                        } else {
                            showConf = false
                            file?.let { vm.shredFile(ctx, it) }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.error),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text(if (confStep == 0) "CONTINUE" else "DESTROY FILE", style = MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.SansSerif))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConf = false }) {
                    Text("CANCEL", style = MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.SansSerif))
                }
            }
        )
    }
}
