package com.shred.it.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
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
                        Color(0xFF0A0A0A),
                        Color(0xFF1A0A0A).copy(alpha = 0.1f + backgroundAnimation.value * 0.3f),
                        Color(0xFF0A0A0A)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
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
                        color = Color(0xFF12F190),
                        fontSize = (28 + titleAnimation.value * 4).sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        modifier = Modifier
                            .scale(0.9f + titleAnimation.value * 0.1f)
                            .animateContentSize()
                    )
                }

                val settingsScale = remember { Animatable(1f) }
                val settingsRotation = remember { Animatable(0f) }

                LaunchedEffect(showSet) {
                    if (showSet) {
                        settingsRotation.animateTo(
                            targetValue = 180f,
                            animationSpec = tween(300, easing = EaseInOutCubic)
                        )
                    } else {
                        settingsRotation.animateTo(0f)
                    }
                }

                IconButton(
                    onClick = { showSet = true },
                    modifier = Modifier
                        .scale(settingsScale.value)
                        .rotate(settingsRotation.value)
                        .background(
                            color = Color(0xFF2A2A2A),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color(0xFF12F190).copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = null,
                        tint = Color(0xFF12F190),
                        modifier = Modifier.size(24.dp)
                    )
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
                ) + fadeIn(tween(600)) + scaleIn(tween(600, easing = EaseOutBack)),
                exit = slideOutVertically(
                    targetOffsetY = { -100 },
                    animationSpec = tween(400, easing = EaseInBack)
                ) + fadeOut(tween(400)) + scaleOut(tween(400))
            ) {
                file?.let { f ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1A1A1A)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 8.dp
                        )
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
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${vm.formatFileSize(f.size)} • ${f.mimeType}",
                                        color = Color(0xFF888888),
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }

                                val closeScale = remember { Animatable(1f) }
                                IconButton(
                                    onClick = {
                                        vm.clearFile()
                                        toastManager.showInfoToast(
                                            heading = "File Cleared",
                                            description = "File removed from queue"
                                        )
                                    },
                                    modifier = Modifier
                                        .scale(closeScale.value)
                                        .background(
                                            color = Color(0xFF2A2A2A),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = null,
                                        tint = Color(0xFF666666),
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
                val selectButtonScale = remember { Animatable(1f) }
                val shredButtonScale = remember { Animatable(1f) }
                val shredButtonRotation = remember { Animatable(0f) }

                LaunchedEffect(isShredding) {
                    if (isShredding) {
                        shredButtonRotation.animateTo(
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(2000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            )
                        )
                    } else {
                        shredButtonRotation.snapTo(0f)
                    }
                }

                AnimatedVisibility(
                    visible = true,
                    enter = slideInHorizontally(
                        initialOffsetX = { -200 },
                        animationSpec = tween(600, 200, easing = EaseOutBack)
                    ) + fadeIn(tween(600, 200))
                ) {
                    Button(
                        onClick = { pick.launch(arrayOf("*/*")) },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .scale(selectButtonScale.value),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2A2A2A),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        enabled = state == ShredderState.IDLE,
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 2.dp
                        )
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "SELECT",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                AnimatedVisibility(
                    visible = true,
                    enter = slideInHorizontally(
                        initialOffsetX = { 200 },
                        animationSpec = tween(600, 400, easing = EaseOutBack)
                    ) + fadeIn(tween(600, 400))
                ) {
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
                            .height(56.dp)
                            .scale(shredButtonScale.value),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isShredding) Color(0xFFFF4444) else Color(0xFFE53E3E),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        enabled = file != null && (state == ShredderState.IDLE || state == ShredderState.FILE_SELECTED),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 2.dp
                        )
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                                .rotate(if (isShredding) shredButtonRotation.value else 0f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isShredding) "SHREDDING" else "SHRED",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
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
                ) + fadeIn(tween(600)) + scaleIn(tween(600, easing = EaseOutBack)),
                exit = slideOutVertically(
                    targetOffsetY = { 100 },
                    animationSpec = tween(400, easing = EaseInBack)
                ) + fadeOut(tween(400)) + scaleOut(tween(400))
            ) {
                prog?.let { p ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1A1A1A)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 8.dp
                        )
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
                                        color = Color.White,
                                        fontSize = 16.sp,
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
                                        color = Color(0xFFE53E3E),
                                        fontSize = 16.sp,
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
                                    .background(Color(0xFF333333))
                            ) {
                                AnimatedContent(
                                    targetState = p.progress / 100f,
                                    transitionSpec = {
                                        fadeIn(animationSpec = tween (300) ).togetherWith(fadeOut(animationSpec = tween(300)))
                                    }
                                ) { progress ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(progress)
                                            .background(
                                                brush = Brush.horizontalGradient(
                                                    colors = listOf(
                                                        Color(0xFFE53E3E),
                                                        Color(0xFFFF6B6B)
                                                    )
                                                ),
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                    )
                                }
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
                                    color = Color(0xFF888888),
                                    fontSize = 14.sp,
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
                    color = Color(0xFF666666),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                val clearLogsScale = remember { Animatable(1f) }
                TextButton(
                    onClick = {
                        vm.clearLogs()
                        toastManager.showInfoToast(
                            heading = "Logs Cleared",
                            description = "All log entries have been removed"
                        )
                    },
                    modifier = Modifier.scale(clearLogsScale.value)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear Logs",
                            tint = Color(0xFF666666),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Clear Logs",
                            color = Color(0xFF666666),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1A1A1A))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
                        enter = slideInVertically(
                            initialOffsetY = { 50 },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        ) + fadeIn(tween(400)) + scaleIn(tween(400, easing = EaseOutBack))
                    ) {
                        Text(
                            text = log.message,
                            color = col,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .animateItem()
                                .padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }

    if (showConf) {
        val dialogScale = remember { Animatable(0f) }
        LaunchedEffect(showConf) {
            dialogScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }

        AlertDialog(
            onDismissRequest = { showConf = false },
            containerColor = Color(0xFF1A1A1A),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.scale(dialogScale.value),
            title = {
                AnimatedContent(
                    targetState = when (confStep) {
                        0 -> "CONFIRM SHRED"
                        else -> "FINAL WARNING"
                    },
                    transitionSpec = {
                        slideInVertically { -30 } + fadeIn() togetherWith
                                slideOutVertically { 30 } + fadeOut()
                    }
                ) { titleText ->
                    Text(
                        text = titleText,
                        color = Color(0xFFE53E3E),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        letterSpacing = 1.sp
                    )
                }
            },
            text = {
                AnimatedContent(
                    targetState = when (confStep) {
                        0 -> "This will permanently destroy the file. Are you sure?"
                        else -> "This action cannot be undone. File will be unrecoverable."
                    },
                    transitionSpec = {
                        slideInVertically { 30 } + fadeIn() togetherWith
                                slideOutVertically { -30 } + fadeOut()
                    }
                ) { descText ->
                    Text(
                        text = descText,
                        color = Color.White,
                        fontSize = 16.sp
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
                        containerColor = Color(0xFFE53E3E),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    Text(
                        text = if (confStep == 0) "CONTINUE" else "DESTROY",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            },
            dismissButton = {
                Button(
                    onClick = { showConf = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2A2A2A),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    Text(
                        text = "CANCEL",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        )
    }

    if (showSet) {
        val settingsDialogScale = remember { Animatable(0f) }
        LaunchedEffect(showSet) {
            settingsDialogScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }

        AlertDialog(
            onDismissRequest = { showSet = false },
            containerColor = Color(0xFF1A1A1A),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.scale(settingsDialogScale.value),
            title = {
                Text(
                    text = "SETTINGS",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
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
                        containerColor = Color(0xFF2A2A2A),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    Text(
                        text = "DONE",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
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

@Composable
fun SettingRow(
    title: String,
    desc: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale = remember { Animatable(1f) }
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(interactionSource) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale.value)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onClick()
            }
            .background(
                color = Color(0xFF2A2A2A),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0xFF12F190).copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color(0xFF12F190),
            modifier = Modifier
                .size(24.dp)
                .rotate(rotation.value)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
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
                    color = Color(0xFF888888),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        Icon(
            Icons.Default.Build,
            contentDescription = null,
            tint = Color(0xFF666666),
            modifier = Modifier
                .size(20.dp)
                .rotate(rotation.value * 0.5f)
        )
    }
}