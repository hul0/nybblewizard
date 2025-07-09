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
 * Note: While this object aims to support various formats, direct Bitmap.compress
 * functionality is limited to JPEG, PNG, and WEBP. Other formats listed in ConvTypes.kt
 * (like SVG, HEIC, AVIF, HDR) would require additional decoding/encoding logic
 * often involving external libraries or specific Android SDK features not covered
 * by standard Bitmap/ImageDecoder for all scenarios.
 */
object ImgConv {

    private const val TAG = "ImgConv"

    /**
     * Converts a given bitmap to a specified image format (JPEG, PNG, WEBP).
     *
     * @param bitmap The input Bitmap to convert.
     * @param targetFormat The desired output format (e.g., Bitmap.CompressFormat.JPEG, Bitmap.CompressFormat.PNG, Bitmap.CompressFormat.WEBP).
     * @param quality The compression quality for formats that support it (0-100).
     * For JPEG and WEBP, this affects file size and visual quality.
     * For PNG, this parameter primarily affects the compression effort/speed, not visual quality,
     * as PNG is a lossless format. Lower quality might result in a larger file size for PNG.
     * @return A ByteArray containing the converted image data, or null if conversion fails.
     */
    fun convertBitmapToByteArray(
        bitmap: Bitmap,
        targetFormat: Bitmap.CompressFormat,
        quality: Int = 90
    ): ByteArray? {
        val outputStream = ByteArrayOutputStream()
        return try {
            val success = bitmap.compress(targetFormat, quality, outputStream)
            if (success) {
                outputStream.toByteArray()
            } else {
                Log.e(TAG, "Bitmap compression failed for format: $targetFormat, quality: $quality")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error compressing bitmap to $targetFormat with quality $quality", e)
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
     * IMPORTANT: This function primarily supports converting to/from formats that Android's
     * ImageDecoder can natively decode into a Bitmap, and then re-encoding into
     * Bitmap.CompressFormat (JPEG, PNG, WEBP). For other formats like SVG, HEIC, AVIF, HDR,
     * additional handling or external libraries would be required for full support.
     *
     * @param context The application context.
     * @param inputUri The Uri of the input image file.
     * @param outputFolderUri The Uri of the directory where the converted file should be saved.
     * @param outputFileName The desired name for the output file (e.g., "converted_image.jpeg").
     * @param targetFormat The desired output format (e.g., Bitmap.CompressFormat.JPEG, Bitmap.CompressFormat.PNG, Bitmap.CompressFormat.WEBP).
     * @param quality The compression quality for formats that support it (0-100).
     * For JPEG and WEBP, this affects file size and visual quality.
     * For PNG, this parameter primarily affects the compression effort/speed, not visual quality,
     * as PNG is a lossless format. Lower quality might result in a larger file size for PNG.
     * @param contentResolver The ContentResolver to resolve the input Uri and open output streams.
     * @return The Uri of the saved output file, or null if conversion fails.
     */
    @RequiresApi(Build.VERSION_CODES.P)
    suspend fun convertImageFile(
        context: Context, // Context is not used directly, can be removed if not needed for future extensions.
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
            // ImageDecoder is generally preferred for modern Android versions (API 28+)
            // as it offers more features and better error handling than BitmapFactory.
            val source = ImageDecoder.createSource(contentResolver, inputUri)
            bitmap = ImageDecoder.decodeBitmap(source) { decoder, info, src ->
                // Make bitmap mutable if modifications are needed (though not strictly for conversion)
                decoder.isMutableRequired = true
            }

            // Fallback for older Android versions or if ImageDecoder fails (less robust)
            if (bitmap == null && Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                Log.w(TAG, "ImageDecoder failed or not available for API < P. Attempting with BitmapFactory.")
                inputStream = contentResolver.openInputStream(inputUri)
                inputStream?.let {
                    bitmap = BitmapFactory.decodeStream(it)
                }
            }

            if (bitmap == null) {
                Log.e(TAG, "Failed to decode bitmap from input Uri: $inputUri. Input stream might be null or image format not supported by decoder.")
                return null
            }

            // Create a new file in the specified output directory using DocumentsContract
            // This is crucial for Storage Access Framework (SAF) and user-selected directories.
            val outputMimeType = "image/${getFileExtension(targetFormat)}"
            newFileUri = DocumentsContract.createDocument(
                contentResolver,
                DocumentsContract.buildDocumentUriUsingTree(outputFolderUri, DocumentsContract.getTreeDocumentId(outputFolderUri)),
                outputMimeType,
                outputFileName
            )

            if (newFileUri == null) {
                Log.e(TAG, "Failed to create new document for output file with MIME type: $outputMimeType and filename: $outputFileName")
                return null
            }

            // Open output stream for the newly created file
            outputStream = contentResolver.openOutputStream(newFileUri)
            if (outputStream == null) {
                Log.e(TAG, "Failed to open output stream for new file: $newFileUri")
                DocumentsContract.deleteDocument(contentResolver, newFileUri) // Clean up the created document if output stream fails
                return null
            }

            // Compress the bitmap to the target format and quality
            val success = bitmap.compress(targetFormat, quality, outputStream)
            if (!success) {
                Log.e(TAG, "Bitmap compression failed for target format: $targetFormat and quality: $quality.")
                DocumentsContract.deleteDocument(contentResolver, newFileUri) // Clean up if compression fails
                return null
            }

            Log.i(TAG, "Successfully converted and saved file to: $newFileUri (Format: $targetFormat, Quality: $quality)")
            return newFileUri

        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException: Permission denied to access URI. Ensure permissions are granted. ${e.message}", e)
        } catch (e: IOException) {
            Log.e(TAG, "IOException during image conversion: ${e.message}", e)
        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "OutOfMemoryError during image conversion: ${e.message}. Consider resizing large images before conversion.", e)
            // Optionally, try to downsample the image and retry if OOM occurs
        } catch (e: Exception) {
            Log.e(TAG, "An unexpected error occurred during image conversion: ${e.message}", e)
        } finally {
            try {
                inputStream?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Error closing input stream: ${e.message}", e)
            }
            try {
                outputStream?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Error closing output stream: ${e.message}", e)
            }
            bitmap?.recycle() // Release native memory associated with the bitmap
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
            // WEBP_LOSSY and WEBP_LOSSLESS are for API 30+, but getFileExtension
            // should provide a generic "webp" extension.
            // If you need distinct extensions for lossy/lossless WebP, consider this.
            // Bitmap.CompressFormat.WEBP_LOSSY -> "webp"
            // Bitmap.CompressFormat.WEBP_LOSSLESS -> "webp"
            else -> "dat" // Fallback for any unexpected or unhandled format
        }
    }

    /**
     * Helper to get the Bitmap.CompressFormat from a string.
     * Includes WEBP_LOSSY and WEBP_LOSSLESS for more precise WebP handling (API 30+).
     */
    fun getCompressFormatFromString(formatString: String): Bitmap.CompressFormat? {
        return when (formatString.uppercase()) {
            "JPEG", "JPG" -> Bitmap.CompressFormat.JPEG
            "PNG" -> Bitmap.CompressFormat.PNG
            "WEBP" -> {
                // For "WEBP", default to WEBP (lossy) for broader compatibility
                // or consider WEBP_LOSSY/WEBP_LOSSLESS based on API level.
                // Here, it defaults to the original WEBP enum for simplicity.
                Bitmap.CompressFormat.WEBP
            }
            // For API 30+, you can differentiate between WEBP_LOSSY and WEBP_LOSSLESS
            // if you explicitly want to control the type of WebP compression.
            // For now, mapping both to the generic WEBP is usually sufficient
            // unless specific control is needed.
            "WEBP_LOSSY" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Bitmap.CompressFormat.WEBP_LOSSY else Bitmap.CompressFormat.WEBP
            "WEBP_LOSSLESS" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Bitmap.CompressFormat.WEBP_LOSSLESS else Bitmap.CompressFormat.WEBP
            else -> null
        }
    }
}
