package com.shred.it.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility // Import for animation
import androidx.compose.animation.expandVertically // Import for expand animation
import androidx.compose.animation.shrinkVertically // Import for shrink animation
import androidx.compose.foundation.clickable // Import for making row clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Loop

// New imports for log icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp


import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector // Import for ImageVector type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.shred.it.core.FileInfo
import com.shred.it.core.FileShredderCore
import com.shred.it.core.LogEntry
import com.shred.it.core.LogType
import com.shred.it.core.ShredderProgress
import com.shred.it.core.ShredderSettings
import com.shred.it.core.ShredderState
import com.shred.it.ui.theme.ThemePreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ViewModel remains unchanged
class FileShredderViewModel : ViewModel() {
    private val core = FileShredderCore()

    val state: StateFlow<ShredderState> = core.state
    val progress: StateFlow<ShredderProgress?> = core.progress
    val logs: StateFlow<List<LogEntry>> = core.logs

    private val _file = MutableStateFlow<FileInfo?>(null)
    val file: StateFlow<FileInfo?> = _file

    private val _settings = MutableStateFlow(
        ShredderSettings.builder().build()
    )
    val settings: StateFlow<ShredderSettings> = _settings

    fun selectFile(ctx: Context, uri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            val f = core.selectFile(ctx, uri, _settings.value)
            _file.value = f
        }
    }

    fun shredFile(ctx: Context, f: FileInfo) {
        CoroutineScope(Dispatchers.IO).launch {
            core.shredFile(ctx, f, _settings.value)
            if (core.state.value == ShredderState.COMPLETE) {
                delay(3000)
                _file.value = null
                core.resetState()
            }
        }
    }

    fun clearFile() {
        _file.value = null
        core.resetState()
    }

    fun clearLogs() {
        core.clearLogs()
    }

    fun updateRounds(r: Int) {
        _settings.value = _settings.value.copy(overwriteRounds = r)
    }

    fun toggleRename() {
        _settings.value = _settings.value.copy(useRandomRename = !_settings.value.useRandomRename)
    }

    fun toggleVerify() {
        _settings.value = _settings.value.copy(verifyOverwrites = !_settings.value.verifyOverwrites)
    }

    fun formatFileSize(bytes: Long): String = core.formatFileSize(bytes)
}


@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun FileShredderScreen(
    innerPadding: PaddingValues, // Accept innerPadding from Scaffold
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
    var showSet by remember { mutableStateOf(false) }
    var confStep by remember { mutableIntStateOf(0) }
    var logsExpanded by remember { mutableStateOf(false) } // State for logs dropdown

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
            .padding(innerPadding) // Apply the Scaffold's padding
            .verticalScroll(scrollState)
            .padding(horizontal = contentHorizontalPadding, vertical = contentVerticalPadding) // Apply screen-specific padding
    ) {
        // --- Header Section ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp), // Slightly reduced padding
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Shredder",
                color = colors.primary,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.ExtraBold // More impactful weight
                )
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { // Reduced spacing for icons
                IconButton(onClick = { ThemePreferences.isDarkMode = !ThemePreferences.isDarkMode }) {
                    Icon(
                        // Conditionally choose the icon based on the current theme state
                        imageVector = if (ThemePreferences.isDarkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                        contentDescription = "Toggle theme",
                        tint = colors.onSurfaceVariant
                    )
                }
                IconButton(onClick = { showSet = true }) {
                    Icon(
                        Icons.Filled.Settings, // Material 3 extended icon
                        contentDescription = "Open Settings",
                        tint = colors.onSurfaceVariant
                    )
                }
            }
        }

        // --- File Selection/Info Card ---
        // Conditionally display FilePreviewCard if a file is selected
        file?.let { selectedFile ->
            FilePreviewCard(
                file = selectedFile,
                onClearFile = {
                    vm.clearFile()
                    scope.launch { snackbarHostState.showSnackbar("File removed from queue") }
                },
                formatFileSize = vm::formatFileSize // Pass the formatting function
            )
        }


        // --- Action Buttons ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), // Increased padding
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ElevatedButton( // Using ElevatedButton for built-in elevation
                onClick = { pick.launch(arrayOf("*/*")) },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp), // Slightly larger than 48dp for better touch target, but not too big
                shape = RoundedCornerShape(12.dp), // Consistent rounded corners
                elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp), // Clearer elevation
                enabled = state == ShredderState.IDLE || state == ShredderState.FILE_SELECTED || state == ShredderState.COMPLETE || state == ShredderState.ERROR
            ) {
                Icon(Icons.AutoMirrored.Filled.InsertDriveFile, contentDescription = null, modifier = Modifier.size(20.dp)) // New icon
                Spacer(modifier = Modifier.width(8.dp)) // Reduced spacing
                Text("SELECT", style = MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.SansSerif)) // SansSerif for button text
            }

            ElevatedButton( // Using ElevatedButton
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
                    .height(52.dp), // Consistent height
                shape = RoundedCornerShape(12.dp), // Consistent rounded corners
                colors = ButtonDefaults.elevatedButtonColors(containerColor = colors.error), // Error color for shred
                elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp), // Clearer elevation
                enabled = file != null && (state == ShredderState.IDLE || state == ShredderState.FILE_SELECTED)
            ) {
                Icon(Icons.Filled.DeleteForever, contentDescription = null, modifier = Modifier.size(20.dp)) // New icon
                Spacer(modifier = Modifier.width(8.dp)) // Reduced spacing
                Text(if (state == ShredderState.OVERWRITING) "SHREDDING" else "SHRED", style = MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.SansSerif)) // SansSerif for button text
            }
        }

        // --- Progress Indicator ---
        if (prog != null && (state == ShredderState.OVERWRITING || state == ShredderState.VERIFYING || state == ShredderState.RENAMING || state == ShredderState.DELETING)) {
            prog?.let { p ->
                Surface( // Using Surface for progress card
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp), // Increased padding
                    shape = RoundedCornerShape(12.dp), // Consistent rounded corners
                    tonalElevation = 3.dp // Subtle elevation
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
                            trackColor = colors.errorContainer // Use errorContainer for track
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
                .padding(bottom = 24.dp) // Adjusted padding for the whole log section
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { logsExpanded = !logsExpanded } // Make the row clickable to toggle
                    .padding(bottom = 12.dp), // Adjusted padding
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
                    Icon(Icons.Default.Clear, contentDescription = "Clear Logs", modifier = Modifier.size(18.dp), tint = colors.onSurfaceVariant) // Smaller icon, subtle tint
                    Spacer(modifier = Modifier.width(4.dp)) // Reduced spacing
                    Text("Clear Logs", style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.SansSerif)) // Smaller label
                }
            }

            // Animated visibility for the logs content
            AnimatedVisibility(
                visible = logsExpanded,
                enter = expandVertically(expandFrom = Alignment.Top),
                exit = shrinkVertically(shrinkTowards = Alignment.Top)
            ) {
                OutlinedCard( // Using OutlinedCard for a distinct log area
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 150.dp, max = 300.dp), // Min height for better visibility
                    shape = RoundedCornerShape(12.dp), // Consistent rounded corners
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = colors.surface, // Background for logs
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
                            verticalArrangement = Arrangement.spacedBy(4.dp), // Tighter spacing for logs
                            reverseLayout = true
                        ) {
                            items(logs.reversed()) { log ->
                                val logColor = when (log.type) {
                                    LogType.ERROR -> colors.error
                                    LogType.WARNING -> colors.secondary
                                    LogType.SUCCESS -> colors.primary // Success uses primary for a sharp look
                                    LogType.PROGRESS -> colors.onSurfaceVariant
                                    LogType.INFO -> colors.onSurfaceVariant
                                    LogType.SECURITY -> colors.error // Security warnings are critical
                                }
                                val logIcon: ImageVector = when (log.type) { // <--- NEW: Determine icon based on log type
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
                                        modifier = Modifier.size(16.dp) // Smaller icon for logs
                                    )
                                    Text(
                                        text = log.message,
                                        color = logColor,
                                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.SansSerif) // SansSerif for logs
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        // Spacer(modifier = Modifier.height(24.dp)) // This spacer should be after the entire logs column

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
                    shape = RoundedCornerShape(12.dp), // Consistent rounded corners
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

    if (showSet) {
        AlertDialog(
            onDismissRequest = { showSet = false },
            title = { Text("SHREDDER SETTINGS", style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.SansSerif)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SettingRow(
                        title = "Overwrite Rounds",
                        description = "${set.overwriteRounds}",
                        icon = Icons.Filled.Loop
                    ) {
                        val currentRounds = set.overwriteRounds
                        val nextRounds = when (currentRounds) {
                            1 -> 3; 3 -> 5; 5 -> 7; 7 -> 10; 10 -> 15; else -> 1
                        }
                        vm.updateRounds(nextRounds)
                        scope.launch { snackbarHostState.showSnackbar("Overwrite rounds set to $nextRounds") }
                    }
                    SettingRow(
                        title = "Random Rename File",
                        description = if (set.useRandomRename) "ENABLED" else "DISABLED",
                        icon = Icons.Filled.Edit
                    ) {
                        vm.toggleRename()
                        scope.launch { snackbarHostState.showSnackbar("Random rename ${if (!set.useRandomRename) "enabled" else "disabled"}") }
                    }
                    SettingRow(
                        title = "Verify Overwrites",
                        description = if (set.verifyOverwrites) "ENABLED" else "DISABLED",
                        icon = Icons.Filled.VerifiedUser
                    ) {
                        vm.toggleVerify()
                        scope.launch { snackbarHostState.showSnackbar("Verify overwrites ${if (!set.verifyOverwrites) "enabled" else "disabled"}") }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showSet = false }) {
                    Text("DONE", style = MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.SansSerif))
                }
            }
        )
    }
}

// FilePreviewCard remains unchanged from previous step
@Composable
private fun FilePreviewCard(
    file: FileInfo,
    onClearFile: () -> Unit,
    formatFileSize: (Long) -> String // Function to format file size
) {
    val colors = MaterialTheme.colorScheme
    val isImage = file.mimeType.startsWith("image/")

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 3.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Conditionally display image or default icon
            if (isImage) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp) // Fixed height for image preview
                        .clip(RoundedCornerShape(8.dp)), // Slightly less rounded corners for inner image
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage( //
                        model = file.uri, //
                        contentDescription = "Selected Image Preview", //
                        modifier = Modifier.fillMaxSize(), //
                        contentScale = ContentScale.Crop //
                    )
                }
                Spacer(modifier = Modifier.height(12.dp)) // Space between image and text
            } else {
                // If not an image, show a generic file icon
                Icon(
                    Icons.AutoMirrored.Filled.InsertDriveFile,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp).align(Alignment.CenterHorizontally),
                    tint = colors.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // File details and clear button (common for both image and non-image files)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = file.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${formatFileSize(file.size)} • ${file.mimeType}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.SansSerif),
                        color = colors.onSurfaceVariant
                    )
                }
                IconButton(onClick = onClearFile) {
                    Icon(Icons.Default.Close, contentDescription = "Clear Selected File", tint = colors.onSurfaceVariant)
                }
            }
        }
    }
}


@Composable
private fun SettingRow(
    title: String,
    description: String,
    icon: ImageVector, // Use ImageVector for the icon type
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.SansSerif),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}