package com.shred.it.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import com.shred.it.ui.reusable.CustomToast
import com.shred.it.ui.reusable.ToastManager
import com.shred.it.ui.reusable.showErrorToast
import com.shred.it.ui.reusable.showInfoToast
import com.shred.it.ui.reusable.showSuccessToast
import com.shred.it.ui.reusable.showWarningToast
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

    private val _settings = MutableStateFlow(ShredderSettings())
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileShredderScreen(vm: FileShredderViewModel = viewModel()) {
    val ctx = LocalContext.current
    val state by vm.state.collectAsState()
    val prog by vm.progress.collectAsState()
    val logs by vm.logs.collectAsState()
    val file by vm.file.collectAsState()
    val set by vm.settings.collectAsState()
    val toastManager = remember { ToastManager() }

    var showConf by remember { mutableStateOf(false) }
    var showSet by remember { mutableStateOf(false) }
    var confStep by remember { mutableIntStateOf(0) }

    val pick = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            vm.selectFile(ctx, it)
            toastManager.showInfoToast(
                heading = "File Selected",
                description = "File loaded successfully"
            )
        }
    }

    // Monitor state changes for toast notifications
    LaunchedEffect(state) {
        when (state) {
            ShredderState.OVERWRITING -> {
                toastManager.showInfoToast(
                    heading = "Shredding Started",
                    description = "Secure file destruction in progress"
                )
            }
            ShredderState.COMPLETE -> {
                toastManager.showSuccessToast(
                    heading = "Shredding Complete",
                    description = "File has been securely destroyed"
                )
            }
            ShredderState.ERROR -> {
                toastManager.showErrorToast(
                    heading = "Shredding Failed",
                    description = "An error occurred during file destruction"
                )
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Shredder",
                color = Color(0xFF12F190),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { showSet = true }) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFF666666))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        AnimatedVisibility(
            visible = file != null,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            file?.let { f ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = f.name,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${vm.formatFileSize(f.size)} • ${f.mimeType}",
                                    color = Color(0xFF888888),
                                    fontSize = 12.sp
                                )
                            }
                            IconButton(onClick = {
                                vm.clearFile()
                                toastManager.showInfoToast(
                                    heading = "File Cleared",
                                    description = "File removed from queue"
                                )
                            }) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFF666666))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { pick.launch(arrayOf("*/*")) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2A2A2A),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(4.dp),
                enabled = state == ShredderState.IDLE
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("SELECT")
            }

            Button(
                onClick = {
                    if (file != null) {
                        confStep = 0
                        showConf = true
                    } else {
                        toastManager.showWarningToast(
                            heading = "No File Selected",
                            description = "Please select a file to shred first"
                        )
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE53E3E),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(4.dp),
                enabled = file != null && (state == ShredderState.IDLE || state == ShredderState.FILE_SELECTED)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("SHRED")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedVisibility(
            visible = prog != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            prog?.let { p ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Round ${p.currentRound}/${p.totalRounds}",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${p.progress}%",
                                color = Color(0xFFE53E3E),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { p.progress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = Color(0xFFE53E3E),
                            trackColor = Color(0xFF333333)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = p.currentOperation,
                            color = Color(0xFF888888),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LOGS",
                color = Color(0xFF666666),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = {
                vm.clearLogs()
                toastManager.showInfoToast(
                    heading = "Logs Cleared",
                    description = "All log entries have been removed"
                )
            }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Clear Logs",
                        tint = Color(0xFF666666),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Clear Logs",
                        color = Color(0xFF666666)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF1A1A1A))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(logs) { log ->
                val col = when (log.type) {
                    LogType.ERROR -> Color(0xFFFF6B6B)
                    LogType.WARNING -> Color(0xFFFFD93D)
                    LogType.SUCCESS -> Color(0xFF6BCF7F)
                    LogType.PROGRESS -> Color(0xFF4ECDC4)
                    LogType.INFO -> Color(0xFF888888)
                }

                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically() + fadeIn()
                ) {
                    Text(
                        text = log.message,
                        color = col,
                        fontSize = 12.sp,
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }

    if (showConf) {
        AlertDialog(
            onDismissRequest = { showConf = false },
            containerColor = Color(0xFF1A1A1A),
            shape = RoundedCornerShape(4.dp),
            title = {
                Text(
                    text = when (confStep) {
                        0 -> "CONFIRM SHRED"
                        else -> "FINAL WARNING"
                    },
                    color = Color(0xFFE53E3E),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = when (confStep) {
                        0 -> "This will permanently destroy the file. Are you sure?"
                        else -> "This action cannot be undone. File will be unrecoverable."
                    },
                    color = Color.White
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53E3E),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(if (confStep == 0) "CONTINUE" else "DESTROY")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showConf = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2A2A2A),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("CANCEL")
                }
            }
        )
    }

    if (showSet) {
        AlertDialog(
            onDismissRequest = { showSet = false },
            containerColor = Color(0xFF1A1A1A),
            shape = RoundedCornerShape(4.dp),
            title = {
                Text(
                    text = "SETTINGS",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    SettingRow(
                        title = "Overwrite Rounds",
                        desc = "${set.overwriteRounds}",
                        icon = Icons.Default.Refresh
                    ) {
                        vm.updateRounds(if (set.overwriteRounds < 15) set.overwriteRounds + 1 else 1)
                        toastManager.showInfoToast(
                            heading = "Settings Updated",
                            description = "Overwrite rounds set to ${if (set.overwriteRounds < 15) set.overwriteRounds + 1 else 1}"
                        )
                    }

                    SettingRow(
                        title = "Random Rename",
                        desc = if (set.useRandomRename) "ON" else "OFF",
                        icon = Icons.Default.Edit
                    ) {
                        vm.toggleRename()
                        toastManager.showInfoToast(
                            heading = "Settings Updated",
                            description = "Random rename ${if (!set.useRandomRename) "enabled" else "disabled"}"
                        )
                    }

                    SettingRow(
                        title = "Verify Overwrites",
                        desc = if (set.verifyOverwrites) "ON" else "OFF",
                        icon = Icons.Default.CheckCircle
                    ) {
                        vm.toggleVerify()
                        toastManager.showInfoToast(
                            heading = "Settings Updated",
                            description = "Verify overwrites ${if (!set.verifyOverwrites) "enabled" else "disabled"}"
                        )
                    }


                }
            },
            confirmButton = {
                Button(
                    onClick = { showSet = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2A2A2A),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("DONE")
                }
            }
        )
    }

    // Toast component - must be at the end
    CustomToast(
        toastData = toastManager.toastData.value,
        onDismiss = { toastManager.hideToast() }
    )
}

@Composable
fun SettingRow(
    title: String,
    desc: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color(0xFF666666),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 14.sp
            )
            Text(
                text = desc,
                color = Color(0xFF888888),
                fontSize = 12.sp
            )
        }
        Icon(
            Icons.Default.Build,
            contentDescription = null,
            tint = Color(0xFF666666),
            modifier = Modifier.size(16.dp)
        )
    }
}