package com.shred.it.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInBack
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseOutBounce
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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


@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun FileShredderScreen(vm: FileShredderViewModel = viewModel()) {
    // Use MaterialTheme.colorScheme everywhere!
    val scrollState = rememberScrollState()
    val colors = MaterialTheme.colorScheme
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

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val isTablet = screenWidth >= 600.dp

    val contentPadding = if (isTablet) {
        PaddingValues(horizontal = screenWidth * 0.1f, vertical = 32.dp)
    } else {
        PaddingValues(20.dp)
    }

    val backgroundAnimation = remember { Animatable(0f) }
    val titleAnimation = remember { Animatable(0f) }
    val isShredding = state == ShredderState.OVERWRITING

    LaunchedEffect(Unit) {
        titleAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(1000, easing = EaseOutBounce)
        )
    }

    LaunchedEffect(isShredding) {
        if (isShredding) {
            backgroundAnimation.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            backgroundAnimation.animateTo(0f)
        }
    }

    val pick = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            vm.selectFile(ctx, it)
            toastManager.showInfoToast(
                heading = "File Selected",
                description = "File loaded successfully"
            )
        }
    }

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colors.background,
                        colors.background.copy(alpha = 0.1f + backgroundAnimation.value * 0.3f),
                        colors.background
                    )
                )
            )
    ) {
        // Make all content scrollable except logs, which are independently scrollable with max height
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(contentPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInHorizontally(
                        initialOffsetX = { -300 },
                        animationSpec = tween(800, easing = EaseOutBack)
                    ) + fadeIn(tween(800))
                ) {
                    Text(
                        text = "SHREDDER",
                        color = colors.primary,
                        fontSize = (if (isTablet) 36 else 28 + titleAnimation.value * 4).toInt().sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        modifier = Modifier
                            .scale(0.9f + titleAnimation.value * 0.1f)
                            .animateContentSize()
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { ThemePreferences.isDarkMode = !ThemePreferences.isDarkMode },
                        modifier = Modifier
                            .background(
                                color = colors.surfaceVariant,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = colors.primary.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(
                            if (ThemePreferences.isDarkMode) Icons.Default.Refresh else Icons.Default.Refresh,
                            contentDescription = "Toggle theme",
                            tint = colors.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(
                        onClick = { showSet = true },
                        modifier = Modifier
                            .background(
                                color = colors.surfaceVariant,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = colors.primary.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = file != null,
                enter = slideInVertically(
                    initialOffsetY = { -100 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(tween(600)),
                exit = slideOutVertically(
                    targetOffsetY = { -100 },
                    animationSpec = tween(400, easing = EaseInBack)
                ) + fadeOut(tween(400))
            ) {
                file?.let { f ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colors.surface
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(20.dp)
                                .animateContentSize()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = f.name,
                                        color = colors.onSurface,
                                        fontSize = if (isTablet) 20.sp else 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${vm.formatFileSize(f.size)} • ${f.mimeType}",
                                        color = colors.onSurfaceVariant,
                                        fontSize = if (isTablet) 16.sp else 14.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        vm.clearFile()
                                        toastManager.showInfoToast(
                                            heading = "File Cleared",
                                            description = "File removed from queue"
                                        )
                                    },
                                    modifier = Modifier.background(
                                        color = colors.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = null,
                                        tint = colors.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { pick.launch(arrayOf("*/*")) },
                    modifier = Modifier
                        .weight(1f)
                        .height(if (isTablet) 64.dp else 56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.surfaceVariant,
                        contentColor = colors.onSurface
                    ),
                    shape = RoundedCornerShape(16.dp),
                    enabled = state == ShredderState.IDLE
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "SELECT",
                        fontSize = if (isTablet) 18.sp else 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
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
                    modifier = Modifier
                        .weight(1f)
                        .height(if (isTablet) 64.dp else 56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isShredding) colors.error else colors.error.copy(alpha = 0.8f),
                        contentColor = colors.onError
                    ),
                    shape = RoundedCornerShape(16.dp),
                    enabled = file != null && (state == ShredderState.IDLE || state == ShredderState.FILE_SELECTED)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isShredding) "SHREDDING" else "SHRED",
                        fontSize = if (isTablet) 18.sp else 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            AnimatedVisibility(
                visible = prog != null,
                enter = slideInVertically(
                    initialOffsetY = { 100 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(tween(600))
            ) {
                prog?.let { p ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colors.surface
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(20.dp)
                                .animateContentSize()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                AnimatedContent(
                                    targetState = "Round ${p.currentRound}/${p.totalRounds}",
                                    transitionSpec = {
                                        slideInVertically { -20 } + fadeIn() togetherWith
                                                slideOutVertically { 20 } + fadeOut()
                                    }
                                ) { roundText ->
                                    Text(
                                        text = roundText,
                                        color = colors.onSurface,
                                        fontSize = if (isTablet) 18.sp else 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                AnimatedContent(
                                    targetState = "${p.progress}%",
                                    transitionSpec = {
                                        slideInVertically { -20 } + fadeIn() togetherWith
                                                slideOutVertically { 20 } + fadeOut()
                                    }
                                ) { progressText ->
                                    Text(
                                        text = progressText,
                                        color = colors.error,
                                        fontSize = if (isTablet) 18.sp else 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(colors.surfaceVariant)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(p.progress / 100f)
                                        .background(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(
                                                    colors.error,
                                                    colors.error.copy(alpha = 0.8f)
                                                )
                                            ),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            AnimatedContent(
                                targetState = p.currentOperation,
                                transitionSpec = {
                                    slideInVertically { 20 } + fadeIn() togetherWith
                                            slideOutVertically { -20 } + fadeOut()
                                }
                            ) { operation ->
                                Text(
                                    text = operation,
                                    color = colors.onSurfaceVariant,
                                    fontSize = if (isTablet) 16.sp else 14.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIVITY LOGS",
                    color = colors.onSurfaceVariant,
                    fontSize = if (isTablet) 18.sp else 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                TextButton(
                    onClick = {
                        vm.clearLogs()
                        toastManager.showInfoToast(
                            heading = "Logs Cleared",
                            description = "All log entries have been removed"
                        )
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear Logs",
                            tint = colors.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Clear Logs",
                            color = colors.onSurfaceVariant,
                            fontSize = if (isTablet) 16.sp else 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Logs: independently scrollable, max height = 300dp (adjust as needed)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surface)
                    .padding(16.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(logs) { log ->
                        val logColor = when (log.type) {
                            LogType.ERROR -> colors.error
                            LogType.WARNING -> colors.secondary
                            LogType.SUCCESS -> colors.tertiary
                            LogType.PROGRESS -> colors.primary
                            LogType.INFO -> colors.onSurfaceVariant
                        }

                        Text(
                            text = log.message,
                            color = logColor,
                            fontSize = if (isTablet) 15.sp else 13.sp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }

    if (showConf) {
        AlertDialog(
            onDismissRequest = { showConf = false },
            containerColor = colors.surface,
            shape = RoundedCornerShape(16.dp),
            title = {
                AnimatedContent(
                    targetState = when (confStep) {
                        0 -> "CONFIRM SHRED"
                        else -> "FINAL WARNING"
                    }
                ) { titleText ->
                    Text(
                        text = titleText,
                        color = colors.error,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isTablet) 24.sp else 20.sp,
                        letterSpacing = 1.sp
                    )
                }
            },
            text = {
                AnimatedContent(
                    targetState = when (confStep) {
                        0 -> "This will permanently destroy the file. Are you sure?"
                        else -> "This action cannot be undone. File will be unrecoverable."
                    }
                ) { descText ->
                    Text(
                        text = descText,
                        color = colors.onSurface,
                        fontSize = if (isTablet) 18.sp else 16.sp
                    )
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.error,
                        contentColor = colors.onError
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (confStep == 0) "CONTINUE" else "DESTROY",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = if (isTablet) 16.sp else 14.sp
                    )
                }
            },
            dismissButton = {
                Button(
                    onClick = { showConf = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.surfaceVariant,
                        contentColor = colors.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "CANCEL",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = if (isTablet) 16.sp else 14.sp
                    )
                }
            }
        )
    }

    if (showSet) {
        AlertDialog(
            onDismissRequest = { showSet = false },
            containerColor = colors.surface,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "SETTINGS",
                    color = colors.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isTablet) 24.sp else 20.sp,
                    letterSpacing = 1.sp
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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
                        containerColor = colors.surfaceVariant,
                        contentColor = colors.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "DONE",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = if (isTablet) 16.sp else 14.sp
                    )
                }
            }
        )
    }

    CustomToast(
        toastData = toastManager.toastData.value,
        onDismiss = { toastManager.hideToast() }
    )
}
@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
private fun SettingRow(
    title: String,
    desc: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp.dp >= 600.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .background(
                color = colors.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = colors.primary.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = colors.onSurface,
                fontSize = if (isTablet) 18.sp else 16.sp,
                fontWeight = FontWeight.Bold
            )
            AnimatedContent(
                targetState = desc,
                transitionSpec = {
                    slideInVertically { 20 } + fadeIn() togetherWith
                            slideOutVertically { -20 } + fadeOut()
                }
            ) { description ->
                Text(
                    text = description,
                    color = colors.onSurfaceVariant,
                    fontSize = if (isTablet) 16.sp else 14.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        Icon(
            Icons.Default.Build,
            contentDescription = null,
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}