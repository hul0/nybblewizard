package com.republicwing.bufferwing.app.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.republicwing.bufferwing.core.FileInfo
import com.republicwing.bufferwing.core.FileShredderCore
import com.republicwing.bufferwing.core.LogEntry
import com.republicwing.bufferwing.core.ShredderProgress
import com.republicwing.bufferwing.core.ShredderSettings
import com.republicwing.bufferwing.core.ShredderState
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
        viewModelScope.launch(Dispatchers.IO) {
            val f = core.selectFile(ctx, uri, _settings.value)
            _file.value = f
        }
    }

    fun shredFile(ctx: Context, f: FileInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            core.shredFile(ctx, f, _settings.value)
            if (core.state.value == ShredderState.COMPLETE) {
                delay(3000) // Keep the delay for UX after shredding
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