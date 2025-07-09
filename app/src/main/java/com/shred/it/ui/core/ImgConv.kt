package com.shred.it.core

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * Object containing core image conversion logic.
 * Currently supports bitmap image conversions (JPEG, PNG, WEBP).
 */
object ImgConv {

    private const val TAG = "ImgConv"

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
            try {
                outputStream.close()
            } catch (e: IOException) {
                Log.e(TAG, "Error closing ByteArrayOutputStream: ${e.message}", e)
            }
        }
    }

    /**
     * Converts an image from one format to another by decoding to Bitmap and re-encoding.
     * This function saves the output to a user-selected directory URI using DocumentsContract.
     *
     * @param context The application context.
     * @param inputUri The Uri of the input image file.
     * @param outputFolderUri The Uri of the directory where the converted file should be saved.
     * @param outputFileName The desired name for the output file (e.g., "converted_image.jpeg").
     * @param targetFormat The desired output format (e.g., Bitmap.CompressFormat.JPEG, Bitmap.CompressFormat.PNG, Bitmap.CompressFormat.WEBP).
     * @param quality The compression quality for formats that support it (0-100).
     * @param contentResolver The ContentResolver to resolve the input Uri and open output streams.
     * @return The Uri of the saved output file, or null if conversion fails.
     */
    @RequiresApi(Build.VERSION_CODES.P)
    suspend fun convertImageFile(
        context: Context,
        inputUri: Uri,
        outputFolderUri: Uri,
        outputFileName: String,
        targetFormat: Bitmap.CompressFormat,
        quality: Int = 90,
        contentResolver: ContentResolver
    ): Uri? {
        var bitmap: Bitmap? = null
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        var newFileUri: Uri? = null

        try {
            // Decode the image into a Bitmap
            inputStream = contentResolver.openInputStream(inputUri)
            if (inputStream == null) {
                Log.e(TAG, "Failed to open input stream for URI: $inputUri")
                return null
            }

            val source = ImageDecoder.createSource(contentResolver, inputUri)
            bitmap = ImageDecoder.decodeBitmap(source) { decoder, info, src ->
                decoder.isMutableRequired = true
            }

            bitmap ?: run {
                Log.e(TAG, "Failed to decode bitmap from input Uri: $inputUri")
                return null
            }

            // Create a new file in the specified output directory using DocumentsContract
            val docId = DocumentsContract.getTreeDocumentId(outputFolderUri)
            newFileUri = DocumentsContract.createDocument(
                contentResolver,
                DocumentsContract.buildDocumentUriUsingTree(outputFolderUri, docId),
                "image/${getFileExtension(targetFormat)}",
                outputFileName
            )

            if (newFileUri == null) {
                Log.e(TAG, "Failed to create new document for output file.")
                return null
            }

            // Open output stream for the newly created file
            outputStream = contentResolver.openOutputStream(newFileUri)
            if (outputStream == null) {
                Log.e(TAG, "Failed to open output stream for new file: $newFileUri")
                DocumentsContract.deleteDocument(contentResolver, newFileUri) // Clean up
                return null
            }

            // Compress the bitmap to the target format and quality
            val success = bitmap.compress(targetFormat, quality, outputStream)
            if (!success) {
                Log.e(TAG, "Bitmap compression failed.")
                DocumentsContract.deleteDocument(contentResolver, newFileUri) // Clean up
                return null
            }

            Log.i(TAG, "Successfully converted and saved file to: $newFileUri")
            return newFileUri

        } catch (e: IOException) {
            Log.e(TAG, "IOException during image conversion: ${e.message}", e)
        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "OutOfMemoryError during image conversion: ${e.message}", e)
        } catch (e: Exception) {
            Log.e(TAG, "An unexpected error occurred during image conversion: ${e.message}", e)
        } finally {
            try {
                inputStream?.close()
                outputStream?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Error closing streams: ${e.message}", e)
            }
            bitmap?.recycle()
        }
        return null
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
            else -> "dat"
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
