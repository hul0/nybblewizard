package com.shred.it.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
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
fun FileShredderScreen(vm: FileShredderViewModel = viewModel()) {
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

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val isTablet = screenWidth >= 600.dp

    val contentPadding = if (isTablet) {
        PaddingValues(horizontal = screenWidth * 0.1f, vertical = 32.dp)
    } else {
        PaddingValues(20.dp)
    }

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

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = colors.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(padding) // Use padding from Scaffold
                .padding(contentPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Shredder",
                    color = colors.primary,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { ThemePreferences.isDarkMode = !ThemePreferences.isDarkMode }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Toggle theme",
                            tint = colors.primary
                        )
                    }
                    IconButton(onClick = { showSet = true }) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Open Settings",
                            tint = colors.primary
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = file != null,
                enter = slideInVertically { -it } + fadeIn(),
                exit = slideOutVertically { -it } + fadeOut()
            ) {
                file?.let { f ->
                    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = f.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${vm.formatFileSize(f.size)} • ${f.mimeType}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = {
                                vm.clearFile()
                                scope.launch { snackbarHostState.showSnackbar("File removed from queue") }
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear Selected File")
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { pick.launch(arrayOf("*/*")) },
                    modifier = Modifier.weight(1f).height(56.dp),
                    enabled = state == ShredderState.IDLE || state == ShredderState.FILE_SELECTED || state == ShredderState.COMPLETE || state == ShredderState.ERROR
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("SELECT", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        if (file != null) {
                            confStep = 0
                            showConf = true
                        } else {
                            scope.launch { snackbarHostState.showSnackbar("Please select a file to shred first") }
                        }
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.error),
                    enabled = file != null && (state == ShredderState.IDLE || state == ShredderState.FILE_SELECTED)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(if (state == ShredderState.OVERWRITING) "SHREDDING" else "SHRED", fontWeight = FontWeight.Bold)
                }
            }

            AnimatedVisibility(
                visible = prog != null && (state == ShredderState.OVERWRITING || state == ShredderState.VERIFYING || state == ShredderState.RENAMING || state == ShredderState.DELETING),
                enter = slideInVertically { it } + fadeIn(),
                exit = fadeOut()
            ) {
                prog?.let { p ->
                    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Round ${p.currentRound}/${p.totalRounds}", fontWeight = FontWeight.Bold)
                                Text("${p.progress}%", color = colors.error, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { p.progress / 100f },
                                modifier = Modifier.fillMaxWidth(),
                                color = colors.error
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = p.currentOperation,
                                color = colors.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ACTIVITY LOGS", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                TextButton(
                    onClick = {
                        vm.clearLogs()
                        scope.launch { snackbarHostState.showSnackbar("All log entries have been removed") }
                    },
                    enabled = logs.isNotEmpty()
                ) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear Logs", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Clear Logs")
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp, max = 300.dp)
            ) {
                if (logs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No activity yet.", color = colors.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        reverseLayout = true
                    ) {
                        items(logs.reversed()) { log ->
                            val logColor = when (log.type) {
                                LogType.ERROR -> colors.error
                                LogType.WARNING -> colors.secondary
                                LogType.SUCCESS -> colors.tertiary
                                LogType.PROGRESS -> colors.primary
                                LogType.INFO -> colors.onSurfaceVariant
                                LogType.SECURITY -> colors.error
                            }
                            Text(text = log.message, color = logColor, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }


    if (showConf) {
        AlertDialog(
            onDismissRequest = { showConf = false },
            title = {
                AnimatedContent(
                    targetState = if (confStep == 0) "CONFIRM SHRED" else "FINAL WARNING",
                    label = "Confirmation Dialog Title"
                ) { titleText ->
                    Text(titleText, color = colors.error, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                AnimatedContent(
                    targetState = if (confStep == 0) "This will permanently destroy the selected file. This action is irreversible. Are you sure you want to proceed?"
                    else "The file will be unrecoverable after this operation. THIS IS YOUR LAST CHANCE TO CANCEL.",
                    label = "Confirmation Dialog Text"
                ) { descText ->
                    Text(descText, fontSize = 16.sp, lineHeight = 22.sp)
                }
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
                    colors = ButtonDefaults.buttonColors(containerColor = colors.error)
                ) {
                    Text(if (confStep == 0) "CONTINUE" else "DESTROY FILE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConf = false }) {
                    Text("CANCEL")
                }
            }
        )
    }

    if (showSet) {
        AlertDialog(
            onDismissRequest = { showSet = false },
            title = { Text("SHREDDER SETTINGS", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SettingRow(
                        title = "Overwrite Rounds",
                        description = "${set.overwriteRounds}",
                        icon = Icons.Default.Refresh
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
                        icon = Icons.Default.Edit
                    ) {
                        vm.toggleRename()
                        scope.launch { snackbarHostState.showSnackbar("Random rename ${if (!set.useRandomRename) "enabled" else "disabled"}") }
                    }
                    SettingRow(
                        title = "Verify Overwrites",
                        description = if (set.verifyOverwrites) "ENABLED" else "DISABLED",
                        icon = Icons.Default.CheckCircle
                    ) {
                        vm.toggleVerify()
                        scope.launch { snackbarHostState.showSnackbar("Verify overwrites ${if (!set.verifyOverwrites) "enabled" else "disabled"}") }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showSet = false }) {
                    Text("DONE")
                }
            }
        )
    }
}

@Composable
private fun SettingRow(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                AnimatedContent(
                    targetState = description,
                    label = "Setting Description Animation"
                ) { desc ->
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}