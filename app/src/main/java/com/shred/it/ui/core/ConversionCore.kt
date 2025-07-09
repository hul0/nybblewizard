package com.shred.it.core

import android.content.ContentResolver
import android.content.Context // Import Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.documentfile.provider.DocumentFile // Import DocumentFile
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream // Keep for potential direct file writing, though DocumentFile handles streams

/**
 * Object containing core conversion logic for the File Converter app.
 * Currently supports bitmap image conversions.
 */
object ConversionCore {

    private const val TAG = "ConversionCore"

    /**
     * Converts a given bitmap to a specified image format (JPEG, PNG, WEBP).
     *
     * @param bitmap The input Bitmap to convert.
     * @param targetFormat The desired output format (e.g., Bitmap.CompressFormat.JPEG, Bitmap.CompressFormat.PNG, Bitmap.CompressFormat.WEBP).
     * @param quality The compression quality for formats that support it (0-100).
     * @return A ByteArray containing the converted image data, or null if conversion fails.
     */
    fun convertBitmapToByteArray(
        bitmap: Bitmap,
        targetFormat: Bitmap.CompressFormat,
        quality: Int = 90
    ): ByteArray? {
        val outputStream = ByteArrayOutputStream()
        return try {
            bitmap.compress(targetFormat, quality, outputStream)
            outputStream.toByteArray()
        } catch (e: Exception) {
            Log.e(TAG, "Error compressing bitmap to $targetFormat", e)
            null
        } finally {
            outputStream.close()
        }
    }

    /**
     * Converts an image from one format to another by decoding to Bitmap and re-encoding.
     * This function now saves the output to a user-selected directory URI using DocumentFile.
     *
     * @param context The application context, required for DocumentFile operations.
     * @param inputUri The Uri of the input image file.
     * @param outputFolderUri The Uri of the directory where the converted file should be saved (obtained from ACTION_OPEN_DOCUMENT_TREE).
     * @param outputFileName The desired name for the output file (e.g., "converted_image.jpeg").
     * @param targetFormat The desired output format (e.g., Bitmap.CompressFormat.JPEG, Bitmap.CompressFormat.PNG, Bitmap.CompressFormat.WEBP).
     * @param quality The compression quality for formats that support it (0-100).
     * @param contentResolver The ContentResolver to resolve the input Uri and open output streams.
     * @return The Uri of the saved output file, or null if conversion fails.
     */
     // ImageDecoder is available from API 28 (P)
    fun convertImageFile(
        context: Context, // Added Context parameter
        inputUri: Uri,
        outputFolderUri: Uri, // Changed from File to Uri
        outputFileName: String,
        targetFormat: Bitmap.CompressFormat,
        quality: Int = 90,
        contentResolver: ContentResolver
    ): Uri? {
        var bitmap: Bitmap? = null
        try {
            // Decode the image into a Bitmap
            bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, inputUri)
                ImageDecoder.decodeBitmap(source) { decoder, info, source ->
                    decoder.isMutableRequired = true // If you need a mutable bitmap
                }
            } else {
                // For older APIs, use BitmapFactory (though this function is @RequiresApi P)
                contentResolver.openInputStream(inputUri)?.use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
            }

            bitmap ?: run {
                Log.e(TAG, "Failed to decode bitmap from input Uri: $inputUri")
                return null
            }

            // Convert the bitmap to the target format byte array
            val convertedByteArray = convertBitmapToByteArray(bitmap, targetFormat, quality)
            convertedByteArray ?: run {
                Log.e(TAG, "Failed to convert bitmap to byte array for target format: $targetFormat")
                return null
            }

            // --- New logic for saving to user-selected directory URI using DocumentFile ---
            val pickedDir = DocumentFile.fromTreeUri(context, outputFolderUri)
            if (pickedDir == null || !pickedDir.isDirectory) {
                Log.e(TAG, "Invalid output folder URI or not a directory: $outputFolderUri")
                return null
            }

            // Determine the MIME type for the new file
            val mimeType = "image/${getFileExtension(targetFormat)}"
            val newFile = pickedDir.createFile(mimeType, outputFileName) // Create file in the selected directory

            if (newFile == null) {
                Log.e(TAG, "Failed to create new file in directory: ${pickedDir.uri}")
                return null
            }

            // Open output stream and write the converted data
            contentResolver.openOutputStream(newFile.uri)?.use { fos ->
                fos.write(convertedByteArray)
            } ?: run {
                Log.e(TAG, "Failed to open output stream for file: ${newFile.uri}")
                return null
            }

            Log.i(TAG, "Successfully converted and saved file to: ${newFile.uri}")
            return newFile.uri

        } catch (e: Exception) {
            Log.e(TAG, "Error converting image file: $inputUri to $outputFileName", e)
            return null
        } finally {
            bitmap?.recycle() // Recycle the bitmap if it's no longer needed
        }
    }

    /**
     * Determines the appropriate file extension for a given Bitmap.CompressFormat.
     *
     * @param format The Bitmap.CompressFormat.
     * @return The corresponding file extension (e.g., "jpeg", "png", "webp").
     */
    fun getFileExtension(format: Bitmap.CompressFormat): String {
        return when (format) {
            Bitmap.CompressFormat.JPEG -> "jpeg"
            Bitmap.CompressFormat.PNG -> "png"
            Bitmap.CompressFormat.WEBP -> "webp"
            else -> "dat" // Default or unknown
        }
    }

    /**
     * Helper to get the Bitmap.CompressFormat from a string.
     */
    fun getCompressFormatFromString(formatString: String): Bitmap.CompressFormat? {
        return when (formatString.uppercase()) {
            "JPEG", "JPG" -> Bitmap.CompressFormat.JPEG
            "PNG" -> Bitmap.CompressFormat.PNG
            "WEBP" -> Bitmap.CompressFormat.WEBP
            else -> null
        }
    }
}
