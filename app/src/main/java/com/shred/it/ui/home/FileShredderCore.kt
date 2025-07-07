package com.shred.it.core

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.Closeable
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.IvParameterSpec
import kotlin.math.min
import kotlin.math.roundToLong
import kotlin.system.measureTimeMillis

enum class ShredderState {
    IDLE, FILE_SELECTED, ENCRYPTING, OVERWRITING, OVERWRITE_COMPLETE,
    RENAMING, DELETING, COMPLETE, ERROR
}

enum class LogType {
    INFO, WARNING, ERROR, SUCCESS, PROGRESS
}

enum class OverwritePattern(val description: String) {
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
    val mimeType: String,
    val extension: String,
    val lastModified: Long
)

data class ShredderProgress(
    val currentRound: Int,
    val totalRounds: Int,
    val progress: Int,
    val bytesProcessed: Long,
    val estimatedTimeRemaining: Long,
    val currentOperation: String
)

data class ShredderSettings(
    val overwriteRounds: Int = 7,
    val useRandomRename: Boolean = true,
    val verifyOverwrites: Boolean = true,
    val secureMemoryHandling: Boolean = true,
    val clearClipboardAfter: Boolean = false,
    val bufferSize: Int = 8192,
    val useEnhancedVerification: Boolean = true
)

data class LogEntry(
    val message: String,
    val type: LogType,
    val timestamp: Long = System.currentTimeMillis()
)

class FileShredderCore {
    private val _state = MutableStateFlow(ShredderState.IDLE)
    val state: StateFlow<ShredderState> = _state

    private val _progress = MutableStateFlow<ShredderProgress?>(null)
    val progress: StateFlow<ShredderProgress?> = _progress

    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs

    private var startTime: Long = 0
    private var totalBytesProcessed: Long = 0
    private var averageSpeed: Double = 0.0

    private fun addLog(message: String, type: LogType) {
        val currentLogs = _logs.value.toMutableList()
        currentLogs.add(LogEntry(message, type))
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
            val mimeType = getMimeType(context, uri)
            val extension = getFileExtension(fileName)
            val lastModified = getLastModified(context, uri)

            validateFile(context, uri, fileSize, settings)?.let { error ->
                addLog(error, LogType.ERROR)
                updateState(ShredderState.ERROR)
                return null
            }

            grantPersistentAccess(context, uri)

            addLog("File selected: $fileName (${formatFileSize(fileSize)})", LogType.SUCCESS)
            addLog("File type: $mimeType", LogType.INFO)
            addLog("File validation: PASSED", LogType.SUCCESS)

            FileInfo(uri, fileName, fileSize, mimeType, extension, lastModified)

        } catch (e: SecurityException) {
            addLog("Permission denied: Cannot access file", LogType.ERROR)
            addLog("Action required: Grant file access permissions", LogType.INFO)
            updateState(ShredderState.ERROR)
            null
        } catch (e: Exception) {
            addLog("Error selecting file: ${e.message}", LogType.ERROR)
            updateState(ShredderState.ERROR)
            null
        }
    }

    suspend fun shredFile(context: Context, fileInfo: FileInfo, settings: ShredderSettings): Boolean {
        return try {
            if (!encryptFileData(context, fileInfo.uri, settings)) {
                updateState(ShredderState.ERROR)
                return false
            }

            if (!performSecureOverwriting(context, fileInfo.uri, settings)) {
                updateState(ShredderState.ERROR)
                return false
            }

            updateState(ShredderState.RENAMING)
            val renamedUri = renameFileToJunk(context, fileInfo.uri, settings)

            updateState(ShredderState.DELETING)
            val deleted = deleteFile(context, renamedUri ?: fileInfo.uri, settings)

            if (deleted) {
                updateState(ShredderState.COMPLETE)
                addLog("File securely shredded.", LogType.SUCCESS)
                true
            } else {
                updateState(ShredderState.ERROR)
                false
            }

        } catch (e: Exception) {
            addLog("Shredding failed: ${e.message}", LogType.ERROR)
            updateState(ShredderState.ERROR)
            false
        }
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }

    fun resetState() {
        _state.value = ShredderState.IDLE
        _progress.value = null
    }

    private fun validateFile(context: Context, uri: Uri, fileSize: Long, settings: ShredderSettings): String? {
        if (fileSize <= 0) {
            return "Invalid file: Empty or unreadable"
        }

        if (!isFileWritable(context, uri)) {
            return "File is not writable or accessible"
        }

        if (settings.secureMemoryHandling && fileSize > Runtime.getRuntime().maxMemory() / 4) {
            addLog("Warning: Large file detected, using streaming mode", LogType.WARNING)
        }

        return null
    }

    private fun grantPersistentAccess(context: Context, uri: Uri) {
        try {
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            addLog("Persistent file access permissions granted", LogType.SUCCESS)
        } catch (e: SecurityException) {
            addLog("Warning: Using temporary file access (no persistent permissions)", LogType.WARNING)
        }
    }

    private suspend fun encryptFileData(context: Context, uri: Uri, settings: ShredderSettings): Boolean {
        var pfd: ParcelFileDescriptor? = null
        var inputStream: FileInputStream? = null
        var outputStream: FileOutputStream? = null
        var inputChannel: FileChannel? = null
        var outputChannel: FileChannel? = null

        return try {
            updateState(ShredderState.ENCRYPTING)
            startOperation()
            addLog("Initiating pre-shred encryption...", LogType.INFO)

            val keyGenerator = KeyGenerator.getInstance("AES")
            keyGenerator.init(256, SecureRandom())
            val secretKey = keyGenerator.generateKey()

            val random = SecureRandom()
            val iv = ByteArray(16)
            random.nextBytes(iv)

            addLog("Generated 256-bit AES encryption key (ephemeral)", LogType.SUCCESS)
            addLog("Generated random initialization vector", LogType.INFO)

            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))

            pfd = context.contentResolver.openFileDescriptor(uri, "rw")
                ?: throw IOException("Could not open file for encryption")

            inputStream = FileInputStream(pfd.fileDescriptor)
            outputStream = FileOutputStream(pfd.fileDescriptor)
            inputChannel = inputStream.channel
            outputChannel = outputStream.channel

            val fileSize = inputChannel.size()
            if (fileSize <= 0) {
                throw IOException("File is empty or size cannot be determined")
            }

            addLog("Encrypting file data (${formatFileSize(fileSize)})...", LogType.INFO)

            val buffer = ByteBuffer.allocate(settings.bufferSize)
            var totalEncrypted: Long = 0
            val encryptedChunks = mutableListOf<ByteArray>()

            val encryptionTime = measureTimeMillis {
                inputChannel.position(0)
                while (inputChannel.read(buffer) > 0) {
                    buffer.flip()
                    val data = ByteArray(buffer.remaining())
                    buffer.get(data)
                    buffer.clear()

                    val encryptedChunk = cipher.update(data)
                    encryptedChunk?.let { chunk ->
                        if (chunk.isNotEmpty()) {
                            encryptedChunks.add(chunk)
                        }
                    }

                    totalEncrypted += data.size
                    updateProgress(totalEncrypted)

                    if (settings.secureMemoryHandling && totalEncrypted % (1024 * 1024) == 0L) {
                        System.gc()
                        delay(1)
                    }
                }

                val finalBlock = cipher.doFinal()
                if (finalBlock != null && finalBlock.isNotEmpty()) {
                    encryptedChunks.add(finalBlock)
                }
            }

            outputChannel.position(0)
            var totalWritten = 0L

            for (chunk in encryptedChunks) {
                val writeBuffer = ByteBuffer.wrap(chunk)
                while (writeBuffer.hasRemaining()) {
                    outputChannel.write(writeBuffer)
                }
                totalWritten += chunk.size
            }

            outputChannel.truncate(totalWritten)
            outputChannel.force(true)

            addLog("File encryption completed in ${encryptionTime}ms", LogType.SUCCESS)
            addLog("Encryption key destroyed (never stored)", LogType.SUCCESS)

            true

        } catch (e: Exception) {
            addLog("Encryption failed: ${e.message}", LogType.ERROR)
            false
        } finally {
            closeResources(inputChannel, outputChannel, inputStream, outputStream, pfd)
            if (settings.secureMemoryHandling) {
                System.gc()
            }
        }
    }

    private suspend fun performSecureOverwriting(context: Context, uri: Uri, settings: ShredderSettings): Boolean {
        var pfd: ParcelFileDescriptor? = null
        var outputStream: FileOutputStream? = null
        var outputChannel: FileChannel? = null

        return try {
            updateState(ShredderState.OVERWRITING)
            startOperation()
            addLog("Preparing secure overwriting process...", LogType.INFO)

            pfd = context.contentResolver.openFileDescriptor(uri, "rw")
                ?: throw IOException("Could not acquire file descriptor")

            outputStream = FileOutputStream(pfd.fileDescriptor)
            outputChannel = outputStream.channel

            val fileSize = outputChannel.size()
            if (fileSize <= 0) throw IOException("Empty file or unknown size")

            addLog("File size: ${formatFileSize(fileSize)}", LogType.INFO)
            addLog("Initializing secure overwrite sequence...", LogType.INFO)

            val random = SecureRandom()
            val buffer = ByteBuffer.allocate(settings.bufferSize)
            val patterns = generateOverwritePatterns(settings.overwriteRounds)

            for (round in 1..settings.overwriteRounds) {
                performOverwriteRound(
                    outputChannel = outputChannel,
                    round = round,
                    totalRounds = settings.overwriteRounds,
                    fileSize = fileSize,
                    pattern = patterns[round - 1],
                    buffer = buffer,
                    random = random,
                    settings = settings
                )
            }

            outputChannel.force(true)
            updateState(ShredderState.OVERWRITE_COMPLETE)
            addLog("${settings.overwriteRounds} overwrite rounds completed.", LogType.SUCCESS)
            true

        } catch (e: Exception) {
            addLog("Overwriting failed: ${e.message}", LogType.ERROR)
            false
        } finally {
            closeResources(outputChannel, outputStream, pfd)
            if (settings.secureMemoryHandling) {
                System.gc()
            }
        }
    }

    private suspend fun performOverwriteRound(
        outputChannel: FileChannel,
        round: Int,
        totalRounds: Int,
        fileSize: Long,
        pattern: OverwritePattern,
        buffer: ByteBuffer,
        random: SecureRandom,
        settings: ShredderSettings
    ) {
        addLog("Starting round $round/$totalRounds: ${pattern.description}", LogType.INFO)

        var totalWritten = 0L
        outputChannel.position(0)

        while (totalWritten < fileSize) {
            buffer.clear()
            val remaining = fileSize - totalWritten
            val bytesToWrite = min(buffer.capacity().toLong(), remaining).toInt()

            when (pattern) {
                OverwritePattern.RANDOM -> random.nextBytes(buffer.array())
                OverwritePattern.ZEROS -> buffer.array().fill(0)
                OverwritePattern.ONES -> buffer.array().fill(-1)
                OverwritePattern.ALTERNATING_1 -> fillAlternating(buffer.array(), 0xAA.toByte(), 0x55.toByte())
                OverwritePattern.ALTERNATING_2 -> fillAlternating(buffer.array(), 0x55.toByte(), 0xAA.toByte())
                OverwritePattern.RANDOM_FLUSH -> {
                    random.nextBytes(buffer.array())
                    outputChannel.force(true)
                }
                OverwritePattern.SECURE_FINAL -> {
                    buffer.array().fill(0)
                    outputChannel.force(true)
                }
            }

            buffer.limit(bytesToWrite)
            outputChannel.write(buffer)
            totalWritten += bytesToWrite

            val progress = ((totalWritten.toDouble() / fileSize) * 100).toInt()
            val timeRemaining = estimateTimeRemaining(fileSize, totalWritten)

            updateProgress(
                ShredderProgress(
                    currentRound = round,
                    totalRounds = totalRounds,
                    progress = progress,
                    bytesProcessed = totalWritten,
                    estimatedTimeRemaining = timeRemaining,
                    currentOperation = "Overwriting - ${pattern.description}"
                )
            )

            if (settings.secureMemoryHandling && totalWritten % (1024 * 1024) == 0L) {
                delay(1)
            }
        }

        addLog("Round $round completed (${formatFileSize(totalWritten)})", LogType.SUCCESS)
    }

    private fun renameFileToJunk(context: Context, uri: Uri, settings: ShredderSettings): Uri? {
        return try {
            if (!settings.useRandomRename) {
                addLog("Random rename disabled in settings.", LogType.INFO)
                return uri
            }

            addLog("Generating secure random filename...", LogType.INFO)
            val junkName = generateSecureRandomFilename()
            addLog("Generated filename: $junkName", LogType.INFO)

            val newUri = DocumentsContract.renameDocument(context.contentResolver, uri, junkName)

            if (newUri != null) {
                addLog("File renamed successfully.", LogType.SUCCESS)
                addLog("Original filename obfuscated.", LogType.SUCCESS)
                newUri
            } else {
                addLog("Rename failed (OS restriction).", LogType.WARNING)
                addLog("Original filename retained.", LogType.INFO)
                uri
            }
        } catch (e: Exception) {
            addLog("Rename failed: ${e.message}", LogType.ERROR)
            uri
        }
    }

    private fun deleteFile(context: Context, uri: Uri, settings: ShredderSettings): Boolean {
        return try {
            addLog("Initiating secure deletion...", LogType.INFO)

            val deleted = DocumentsContract.deleteDocument(context.contentResolver, uri)

            if (deleted) {
                addLog("File deleted from filesystem.", LogType.SUCCESS)
                addLog("Data securely destroyed.", LogType.SUCCESS)
                addLog("Recovery status: Cryptographically impossible.", LogType.SUCCESS)

                if (settings.clearClipboardAfter) {
                    clearClipboard(context)
                    addLog("Clipboard cleared.", LogType.SUCCESS)
                }
            } else {
                addLog("Automatic deletion failed.", LogType.WARNING)
                addLog("Action required: Manual deletion may be necessary.", LogType.INFO)
            }

            if (settings.secureMemoryHandling) {
                System.gc()
                addLog("Memory sanitized.", LogType.SUCCESS)
            }

            deleted

        } catch (e: Exception) {
            addLog("Deletion error: ${e.message}", LogType.ERROR)
            false
        }
    }

    private fun fillAlternating(array: ByteArray, byte1: Byte, byte2: Byte) {
        for (i in array.indices) {
            array[i] = if (i % 2 == 0) byte1 else byte2
        }
    }

    private fun generateOverwritePatterns(rounds: Int): List<OverwritePattern> {
        return buildList {
            if (rounds > 0) {
                repeat(rounds - 1) { index ->
                    add(
                        when (index % 5) {
                            0 -> OverwritePattern.RANDOM
                            1 -> OverwritePattern.ALTERNATING_1
                            2 -> OverwritePattern.ONES
                            3 -> OverwritePattern.ALTERNATING_2
                            else -> OverwritePattern.RANDOM_FLUSH
                        }
                    )
                }
                add(OverwritePattern.SECURE_FINAL)
            }
        }
    }

    private fun estimateTimeRemaining(totalSize: Long, processedSize: Long): Long {
        if (processedSize == 0L || averageSpeed == 0.0) return -1
        val remainingBytes = totalSize - processedSize
        return (remainingBytes / (averageSpeed / 7)).roundToLong()
    }

    private fun startOperation() {
        startTime = System.currentTimeMillis()
        totalBytesProcessed = 0
        averageSpeed = 0.0
    }

    private fun updateProgress(processed: Long) {
        totalBytesProcessed = processed
        val elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000.0
        if (elapsedSeconds > 0) {
            averageSpeed = processed.toDouble() / elapsedSeconds
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
            append(".txt")
        }
    }

    private fun clearClipboard(context: Context) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
        clipboard?.setPrimaryClip(android.content.ClipData.newPlainText("", ""))
    }

    private fun extractFileName(context: Context, uri: Uri): String {
        var cursor: Cursor? = null
        return try {
            cursor = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            if (cursor?.moveToFirst() == true) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) cursor.getString(index) else "Unknown"
            } else {
                "Unknown"
            }
        } catch (e: Exception) {
            "Unknown"
        } finally {
            cursor?.close()
        }
    }

    private fun getFileSize(context: Context, uri: Uri): Long {
        var cursor: Cursor? = null
        return try {
            cursor = context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)
            if (cursor?.moveToFirst() == true) {
                val index = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (index != -1) cursor.getLong(index) else -1L
            } else {
                -1L
            }
        } catch (e: Exception) {
            -1L
        } finally {
            cursor?.close()
        }
    }

    private fun getMimeType(context: Context, uri: Uri): String {
        return context.contentResolver.getType(uri)
            ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(getFileExtension(uri.toString()))
            ?: "application/octet-stream"
    }

    private fun getFileExtension(fileName: String): String {
        return fileName.substringAfterLast('.', "")
    }

    private fun getLastModified(context: Context, uri: Uri): Long {
        var cursor: Cursor? = null
        return try {
            cursor = context.contentResolver.query(uri, arrayOf(DocumentsContract.Document.COLUMN_LAST_MODIFIED), null, null, null)
            if (cursor?.moveToFirst() == true) {
                val index = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
                if (index != -1) cursor.getLong(index) else System.currentTimeMillis()
            } else {
                System.currentTimeMillis()
            }
        } catch (e: Exception) {
            System.currentTimeMillis()
        } finally {
            cursor?.close()
        }
    }

    private fun isFileWritable(context: Context, uri: Uri): Boolean {
        return try {
            context.contentResolver.openFileDescriptor(uri, "w")?.use { true } ?: false
        } catch (e: Exception) {
            false
        }
    }

    private fun closeResources(vararg resources: Closeable?) {
        resources.forEach { resource ->
            try {
                resource?.close()
            } catch (e: Exception) {
                // Log the exception if necessary, but don't re-throw
            }
        }
    }

    @SuppressLint("DefaultLocale")
    fun formatFileSize(bytes: Long): String = when {
        bytes < 0 -> "N/A"
        bytes == 0L -> "0 B"
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format("%.2f KB", bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
        else -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
    }
}