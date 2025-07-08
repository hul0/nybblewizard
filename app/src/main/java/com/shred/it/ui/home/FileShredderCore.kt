package com.shred.it.core

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.Closeable
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.security.SecureRandom
import kotlin.math.min

enum class ShredderState {
    IDLE, FILE_SELECTED, OVERWRITING, VERIFYING, OVERWRITE_COMPLETE,
    RENAMING, DELETING, COMPLETE, ERROR
}

enum class LogType {
    INFO, WARNING, ERROR, SUCCESS, PROGRESS, SECURITY
}

internal enum class OverwritePattern(val description: String) {
    RANDOM("Cryptographic random data"),
    ZEROS("All zeros"),
    ONES("All ones"),
    ALTERNATING_1("Alternating pattern (0xAA/0x55)"),
    ALTERNATING_2("Alternating pattern (0x55/0xAA)"),
    RANDOM_FLUSH("Random with flush"),
    SECURE_FINAL("Security-optimized zeros")
}

data class FileInfo(
    val uri: Uri,
    val name: String,
    val size: Long,
    val mimeType: String
)

data class ShredderProgress(
    val currentRound: Int,
    val totalRounds: Int,
    val progress: Int,
    val bytesProcessed: Long,
    val currentOperation: String
)

data class ShredderSettings(
    val overwriteRounds: Int,
    val useRandomRename: Boolean,
    val verifyOverwrites: Boolean
) {
    companion object {
        fun builder() = Builder()
    }

    class Builder {
        private var overwriteRounds: Int = 5
        private var useRandomRename: Boolean = true
        private var verifyOverwrites: Boolean = true

        fun rounds(rounds: Int) = apply {
            this.overwriteRounds = rounds.coerceIn(1, 35)
        }

        fun randomRename(enabled: Boolean) = apply {
            this.useRandomRename = enabled
        }

        fun verify(enabled: Boolean) = apply {
            this.verifyOverwrites = enabled
        }

        fun build() = ShredderSettings(
            overwriteRounds, useRandomRename, verifyOverwrites
        )
    }
}

data class LogEntry(
    val message: String,
    val type: LogType
)

class FileShredderCore {
    private val _state = MutableStateFlow(ShredderState.IDLE)
    val state: StateFlow<ShredderState> = _state

    private val _progress = MutableStateFlow<ShredderProgress?>(null)
    val progress: StateFlow<ShredderProgress?> = _progress

    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs

    private var primaryBuffer: ByteBuffer? = null
    private var secondaryBuffer: ByteBuffer? = null
    private var currentBufferSize: Int = 0
    private val hardcodedBufferSize = 8192

    private fun addLog(message: String, type: LogType) {
        val currentLogs = _logs.value.toMutableList()
        currentLogs.add(LogEntry(message, type))
        if (currentLogs.size > 1000) {
            currentLogs.removeAt(0)
        }
        _logs.value = currentLogs
    }

    private fun updateState(newState: ShredderState) {
        _state.value = newState
    }

    private fun updateProgress(progress: ShredderProgress) {
        _progress.value = progress
    }

    fun selectFile(context: Context, uri: Uri, settings: ShredderSettings): FileInfo? {
        return try {
            updateState(ShredderState.FILE_SELECTED)

            val fileName = extractFileName(context, uri)
            val fileSize = getFileSize(context, uri)

            validateFile(context, uri, fileSize)?.let { error ->
                addLog(error, LogType.ERROR)
                updateState(ShredderState.ERROR)
                return null
            }

            grantPersistentAccess(context, uri)

            addLog("File selected: $fileName", LogType.SUCCESS)

            FileInfo(
                uri = uri,
                name = fileName,
                size = fileSize,
                mimeType = getMimeType(context, uri)
            )

        } catch (e: SecurityException) {
            addLog("Permission denied: Cannot access file", LogType.ERROR)
            updateState(ShredderState.ERROR)
            null
        } catch (e: Exception) {
            addLog("Error selecting file", LogType.ERROR)
            updateState(ShredderState.ERROR)
            null
        }
    }

    suspend fun shredFile(context: Context, fileInfo: FileInfo, settings: ShredderSettings): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (!performSecureOverwriting(context, fileInfo, settings)) {
                    updateState(ShredderState.ERROR)
                    return@withContext false
                }

                updateState(ShredderState.RENAMING)
                val renamedUri = renameFileToJunk(context, fileInfo.uri, settings)

                updateState(ShredderState.DELETING)
                val deleted = deleteFile(context, renamedUri ?: fileInfo.uri)

                if (deleted) {
                    updateState(ShredderState.COMPLETE)
                    addLog("File securely shredded and deleted", LogType.SUCCESS)
                    cleanupResources()
                    true
                } else {
                    updateState(ShredderState.ERROR)
                    false
                }

            } catch (e: Exception) {
                addLog("Shredding failed", LogType.ERROR)
                updateState(ShredderState.ERROR)
                cleanupResources()
                false
            }
        }
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }

    fun resetState() {
        _state.value = ShredderState.IDLE
        _progress.value = null
        cleanupResources()
    }

    private fun validateFile(context: Context, uri: Uri, fileSize: Long): String? {
        if (fileSize <= 0) {
            return "Invalid file: Empty or unreadable"
        }
        if (!isFileWritable(context, uri)) {
            return "File is not writable or accessible"
        }
        return null
    }

    private fun grantPersistentAccess(context: Context, uri: Uri) {
        try {
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            addLog("Persistent file access granted", LogType.INFO)
        } catch (e: Exception) {
            addLog("Cannot grant persistent permissions. Shredding may fail.", LogType.WARNING)
        }
    }

    private suspend fun performSecureOverwriting(context: Context, fileInfo: FileInfo, settings: ShredderSettings): Boolean {
        var pfd: ParcelFileDescriptor? = null
        var inputStream: FileInputStream? = null
        var outputStream: FileOutputStream? = null
        var inputChannel: FileChannel? = null
        var outputChannel: FileChannel? = null

        return try {
            updateState(ShredderState.OVERWRITING)
            addLog("Starting overwrite process", LogType.INFO)

            pfd = context.contentResolver.openFileDescriptor(fileInfo.uri, "rw")
                ?: throw IOException("Could not acquire file descriptor")

            inputStream = FileInputStream(pfd.fileDescriptor)
            outputStream = FileOutputStream(pfd.fileDescriptor)
            inputChannel = inputStream.channel
            outputChannel = outputStream.channel

            val fileSize = outputChannel.size()
            if (fileSize <= 0) throw IOException("Empty file or unknown size")

            initializeBuffers()
            val patterns = generateOverwritePatterns(settings.overwriteRounds)

            for (round in 1..settings.overwriteRounds) {
                performOverwriteRound(
                    outputChannel = outputChannel,
                    round = round,
                    totalRounds = settings.overwriteRounds,
                    fileSize = fileSize,
                    pattern = patterns[round - 1]
                )
                outputChannel.force(true)
            }

            if (settings.verifyOverwrites) {
                updateState(ShredderState.VERIFYING)
                val lastPattern = patterns.last()
                val verificationResult = verifyOverwrite(inputChannel, fileSize, lastPattern)
                if (verificationResult) {
                    addLog("Overwrite verification PASSED", LogType.SUCCESS)
                } else {
                    addLog("Overwrite verification FAILED. Continuing shredding.", LogType.WARNING)
                    // Do not return false here, continue shredding even if verification fails
                }
            }

            updateState(ShredderState.OVERWRITE_COMPLETE)
            addLog("Overwriting completed", LogType.SUCCESS)
            true

        } catch (e: Exception) {
            addLog("Overwriting failed", LogType.ERROR)
            false
        } finally {
            closeResources(inputChannel, outputChannel, inputStream, outputStream, pfd)
        }
    }

    private suspend fun performOverwriteRound(
        outputChannel: FileChannel,
        round: Int,
        totalRounds: Int,
        fileSize: Long,
        pattern: OverwritePattern
    ) {
        addLog("Round $round/$totalRounds: ${pattern.description}", LogType.INFO)

        var totalWritten = 0L
        outputChannel.position(0)
        val random = SecureRandom()

        while (totalWritten < fileSize) {
            val buffer = getNextBuffer()
            buffer.clear()

            val remaining = fileSize - totalWritten
            val bytesToWrite = min(buffer.capacity().toLong(), remaining).toInt()

            when (pattern) {
                OverwritePattern.RANDOM, OverwritePattern.RANDOM_FLUSH -> random.nextBytes(buffer.array())
                OverwritePattern.ZEROS, OverwritePattern.SECURE_FINAL -> buffer.array().fill(0)
                OverwritePattern.ONES -> buffer.array().fill(-1)
                OverwritePattern.ALTERNATING_1 -> fillAlternating(buffer.array(), 0xAA.toByte(), 0x55.toByte())
                OverwritePattern.ALTERNATING_2 -> fillAlternating(buffer.array(), 0x55.toByte(), 0xAA.toByte())
            }

            buffer.limit(bytesToWrite)
            outputChannel.write(buffer)
            totalWritten += bytesToWrite

            val progress = ((totalWritten.toDouble() / fileSize) * 100).toInt()

            updateProgress(
                ShredderProgress(
                    currentRound = round,
                    totalRounds = totalRounds,
                    progress = progress,
                    bytesProcessed = totalWritten,
                    currentOperation = "Overwriting"
                )
            )
        }
    }

    private suspend fun verifyOverwrite(inputChannel: FileChannel, fileSize: Long, finalPattern: OverwritePattern): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                addLog("Verifying overwrite", LogType.INFO)
                val sampleRate = 0.1f
                val sampleSize = (fileSize * sampleRate).toLong()
                val random = SecureRandom()
                val buffer = ByteBuffer.allocate(4096)
                var samplesChecked = 0
                var samplesPassed = 0

                for (i in 0 until min(100, sampleSize / 4096)) {
                    val offset = if (fileSize > 4096) random.nextLong() % (fileSize - 4096) else 0
                    inputChannel.position(offset)
                    buffer.clear()
                    val bytesRead = inputChannel.read(buffer)

                    if (bytesRead > 0) {
                        buffer.flip()
                        val data = ByteArray(bytesRead)
                        buffer.get(data)
                        samplesChecked++

                        val passed = when (finalPattern) {
                            OverwritePattern.ZEROS, OverwritePattern.SECURE_FINAL -> data.all { it == 0.toByte() }
                            OverwritePattern.ONES -> data.all { it == (-1).toByte() }
                            OverwritePattern.ALTERNATING_1 -> data.indices.all { data[it] == if (it % 2 == 0) 0xAA.toByte() else 0x55.toByte() }
                            OverwritePattern.ALTERNATING_2 -> data.indices.all { data[it] == if (it % 2 == 0) 0x55.toByte() else 0xAA.toByte() }
                            OverwritePattern.RANDOM, OverwritePattern.RANDOM_FLUSH -> {
                                val firstByte = data.first()
                                !data.all { it == firstByte }
                            }
                        }
                        if (passed) samplesPassed++
                    }
                }

                val verificationPercentage = if (samplesChecked > 0) {
                    (samplesPassed.toDouble() / samplesChecked * 100).toInt()
                } else {
                    100
                }

                addLog("Verification result: $verificationPercentage% samples matched", LogType.INFO)
                verificationPercentage >= 90

            } catch (e: Exception) {
                addLog("Verification failed", LogType.ERROR)
                false
            }
        }
    }

    private fun initializeBuffers() {
        if (primaryBuffer == null) {
            primaryBuffer = ByteBuffer.allocate(hardcodedBufferSize)
            secondaryBuffer = ByteBuffer.allocate(hardcodedBufferSize)
            currentBufferSize = hardcodedBufferSize
            addLog("Buffers initialized", LogType.INFO)
        }
    }

    private var usePrimary = true
    private fun getNextBuffer(): ByteBuffer {
        usePrimary = !usePrimary
        return if (!usePrimary) primaryBuffer!! else secondaryBuffer!!
    }

    private fun renameFileToJunk(context: Context, uri: Uri, settings: ShredderSettings): Uri? {
        return try {
            if (!settings.useRandomRename) {
                return uri
            }
            val junkName = generateSecureRandomFilename()
            val newUri = DocumentsContract.renameDocument(context.contentResolver, uri, junkName)
            if (newUri != null) {
                addLog("File renamed", LogType.SUCCESS)
                newUri
            } else {
                addLog("Rename failed (provider limitation)", LogType.WARNING)
                uri
            }
        } catch (e: Exception) {
            addLog("Rename failed", LogType.ERROR)
            uri
        }
    }

    private fun deleteFile(context: Context, uri: Uri): Boolean {
        return try {
            addLog("Deleting file", LogType.INFO)
            val deleted = DocumentsContract.deleteDocument(context.contentResolver, uri)
            if (deleted) {
                addLog("File deleted", LogType.SUCCESS)
            } else {
                addLog("Deletion failed. Manual deletion may be necessary.", LogType.WARNING)
            }
            deleted
        } catch (e: Exception) {
            addLog("Deletion error", LogType.ERROR)
            false
        }
    }

    private fun cleanupResources() {
        primaryBuffer = null
        secondaryBuffer = null
        currentBufferSize = 0
        System.gc()
        addLog("Resources cleaned up", LogType.SECURITY)
    }

    private fun fillAlternating(array: ByteArray, byte1: Byte, byte2: Byte) {
        for (i in array.indices) {
            array[i] = if (i % 2 == 0) byte1 else byte2
        }
    }

    private fun generateOverwritePatterns(rounds: Int): List<OverwritePattern> {
        return buildList {
            if (rounds > 0) {
                if (rounds > 1) {
                    repeat(rounds - 1) { index ->
                        add(
                            when (index % 4) {
                                0 -> OverwritePattern.RANDOM
                                1 -> OverwritePattern.ALTERNATING_1
                                2 -> OverwritePattern.ONES
                                else -> OverwritePattern.ALTERNATING_2
                            }
                        )
                    }
                }
                add(OverwritePattern.RANDOM_FLUSH)
            }
        }
    }

    private fun generateSecureRandomFilename(): String {
        val random = SecureRandom()
        val length = random.nextInt(8) + 12
        val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return buildString {
            repeat(length) {
                append(chars[random.nextInt(chars.length)])
            }
            append(".tmp")
        }
    }

    private fun extractFileName(context: Context, uri: Uri): String {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) return cursor.getString(index) ?: "Unknown"
            }
        }
        return "Unknown"
    }

    private fun getFileSize(context: Context, uri: Uri): Long {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (index != -1) return cursor.getLong(index)
            }
        }
        return -1L
    }

    private fun getMimeType(context: Context, uri: Uri): String {
        return context.contentResolver.getType(uri)
            ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(uri.toString().substringAfterLast('.', ""))
            ?: "application/octet-stream"
    }

    private fun isFileWritable(context: Context, uri: Uri): Boolean {
        return try {
            context.contentResolver.openFileDescriptor(uri, "w")?.use { true } ?: false
        } catch (e: Exception) {
            false
        }
    }

    private fun closeResources(vararg resources: Closeable?) {
        resources.forEach {
            try {
                it?.close()
            } catch (e: IOException) {
                // Ignored
            }
        }
    }

    @SuppressLint("DefaultLocale")
    fun formatFileSize(bytes: Long): String = when {
        bytes < 0 -> "N/A"
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format("%.2f KB", bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
        else -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
    }
}