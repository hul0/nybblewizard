package com.republicwing.bufferwing.core

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.util.Log // Added for logging potential issues
import androidx.annotation.RequiresApi
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGParseException
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

object ImgConv {

    private const val TAG = "ImgConv"

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
                Log.e(TAG, "Bitmap compression failed.")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during bitmap to byte array conversion", e)
            null
        } finally {
            try {
                outputStream.close()
            } catch (e: IOException) {
                // Ignored
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    suspend fun convertImageFile(
        context: Context,
        inputUri: Uri,
        outputFolderUri: Uri,
        outputFileName: String, // This name should ideally include the correct extension
//        targetCompressFormat: Bitmap.CompressFormat, // For actual compression
        targetFormatString: Bitmap.CompressFormat, // For MIME type and extension, passed from ConversionCore
        quality: Int = 90,
        contentResolver: ContentResolver
    ): Uri? {
        var decodedBitmap: Bitmap? = null
        var outputStream: OutputStream? = null
        var newFileUri: Uri? = null

        try {
            decodedBitmap = decodeImageToBitmap(context, inputUri, contentResolver)

            if (decodedBitmap == null) {
                Log.e(TAG, "Failed to decode image to bitmap from URI: $inputUri")
                return null
            }

            // Use targetFormatString to get the MIME type
            val outputMimeType = getMimeTypeForFormat(targetFormatString.name)
            if (outputMimeType == null) {
                Log.e(TAG, "Could not determine MIME type for format: $targetFormatString")
                return null
            }

            // Ensure outputFileName has the correct extension based on targetFormatString
            // It's better if the caller provides the full name with extension.
            // If not, we could try to adjust it, but that adds complexity.
            // For now, assuming outputFileName is correct or determined by caller.

            newFileUri = DocumentsContract.createDocument(
                contentResolver,
                DocumentsContract.buildDocumentUriUsingTree(
                    outputFolderUri,
                    DocumentsContract.getTreeDocumentId(outputFolderUri)
                ),
                outputMimeType,
                outputFileName // Use the provided outputFileName
            )

            if (newFileUri == null) {
                Log.e(TAG, "Failed to create document for output.")
                return null
            }

            outputStream = contentResolver.openOutputStream(newFileUri)
            if (outputStream == null) {
                Log.e(TAG, "Failed to open output stream for URI: $newFileUri")
                DocumentsContract.deleteDocument(contentResolver, newFileUri)
                return null
            }

            // Use targetCompressFormat for encoding
            val success = decodedBitmap.compress(targetFormatString, quality, outputStream)

            if (!success) {
                Log.e(TAG, "Failed to compress and encode bitmap to file.")
                DocumentsContract.deleteDocument(contentResolver, newFileUri)
                return null
            }

            return newFileUri

        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException during image conversion", e)
        } catch (e: IOException) {
            Log.e(TAG, "IOException during image conversion", e)
        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "OutOfMemoryError during image conversion", e)
        } catch (e: Exception) {
            Log.e(TAG, "Generic exception during image conversion", e)
        } finally {
            try {
                outputStream?.close()
            } catch (e: IOException) {
                // Ignored
            }
            // decodedBitmap?.recycle() // Recycle is handled by ImageDecoder on P+ or if manually created
            // Be careful with manual recycling if bitmap is used elsewhere
        }
        // If any error occurred and was caught, newFileUri might still exist but be incomplete.
        // It's safer to delete it if we are returning null due to an error.
        if (newFileUri != null) {
            try {
                DocumentsContract.deleteDocument(contentResolver, newFileUri)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to clean up partially created document: $newFileUri", e)
            }
        }
        return null
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun decodeImageToBitmap(
        context: Context,
        inputUri: Uri,
        contentResolver: ContentResolver
    ): Bitmap? {
        val mimeType = contentResolver.getType(inputUri)
        var bitmap: Bitmap? = null
        var inputStream: InputStream? = null

        try {
            // Prefer ImageDecoder for modern Android versions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, inputUri)
                bitmap = ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.isMutableRequired = true // Make mutable if you plan to draw on it
                }
                // ImageDecoder handles closing the source stream
            }

            // Fallback for older versions or if ImageDecoder fails/doesn't support format
            if (bitmap == null) {
                inputStream = contentResolver.openInputStream(inputUri)
                inputStream?.let {
                    bitmap = when (mimeType) {
                        "image/svg+xml" -> decodeSvgToBitmap(it)
                        // BitmapFactory should handle common types like HEIC/HEIF/AVIF on supported OS versions
                        // No need for specific cases here unless special handling is required
                        else -> BitmapFactory.decodeStream(it)
                    }
                }
            }
        } catch (e: ImageDecoder.DecodeException) {
            Log.e(TAG, "ImageDecoder.DecodeException for URI: $inputUri", e)
        } catch (e: IOException) {
            Log.e(TAG, "IOException during bitmap decoding for URI: $inputUri", e)
        } catch (e: Exception) {
            Log.e(TAG, "Exception during bitmap decoding for URI: $inputUri", e)
        } finally {
            try {
                inputStream?.close() // Close only if we opened it manually
            } catch (e: IOException) {
                // Ignored
            }
        }
        return bitmap
    }

    // This function is no longer directly used by convertImageFile
    // as compression is done directly with Bitmap.CompressFormat.
    // It can be removed if not used elsewhere, or kept for other purposes.
    /*
    private fun encodeBitmapToFile(
        bitmap: Bitmap,
        targetFormatString: String, // This was the parameter causing confusion
        quality: Int,
        outputStream: OutputStream
    ): Boolean {
        getCompressFormatFromString(targetFormatString)?.let { compressFormat ->
            return bitmap.compress(compressFormat, quality, outputStream)
        }
        // This part becomes mostly irrelevant if we always use Bitmap.CompressFormat
        Log.w(TAG, "encodeBitmapToFile called with a format string '$targetFormatString' that doesn't map to Bitmap.CompressFormat. This indicates an issue.")
        return false // Should not happen if logic is correct
    }
    */

    private fun decodeSvgToBitmap(inputStream: InputStream): Bitmap? {
        return try {
            val svg = SVG.getFromInputStream(inputStream)
            // if (!svg.documentRenderable()) return null // Check if SVG can be rendered

            val picture = svg.renderToPicture()
            if (picture == null || picture.width <= 0 || picture.height <= 0) {
                Log.e(TAG, "SVG rendering resulted in an invalid picture.")
                return null
            }
            val bitmap =
                Bitmap.createBitmap(picture.width, picture.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            picture.draw(canvas)
            bitmap
        } catch (e: SVGParseException) {
            Log.e(TAG, "SVGParseException during SVG decoding", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Exception during SVG decoding", e)
            null
        }
    }

    // This function remains essential for getting MIME type for DocumentsContract
    fun getMimeTypeForFormat(formatString: String): String? {
        return when (formatString.uppercase()) {
            "JPEG", "JPG" -> "image/jpeg"
            "PNG" -> "image/png"
            "WEBP", "WEBP_LOSSY", "WEBP_LOSSLESS" -> "image/webp"
            // Add other types if you intend to support them as output (like HEIC, AVIF, SVG)
            // Note: Encoding to SVG, HEIC, AVIF from a Bitmap is complex and not directly supported by Bitmap.compress
            "HEIC", "HEIF" -> "image/heic" // Typically for decoding, encoding is harder
            "AVIF" -> "image/avif"       // Typically for decoding, encoding is harder
            "SVG" -> "image/svg+xml"     // Typically for decoding, encoding is harder
            else -> {
                Log.w(TAG, "Unsupported format string for MIME type: $formatString")
                null
            }
        }
    }

    fun getFileExtension(formatString: String): String {
        return when (formatString.uppercase()) {
            "JPEG", "JPG" -> "jpeg"
            "PNG" -> "png"
            "WEBP", "WEBP_LOSSY", "WEBP_LOSSLESS" -> "webp"
            "HEIC", "HEIF" -> "heic"
            "AVIF" -> "avif"
            "SVG" -> "svg"
            else -> "dat" // Fallback extension
        }
    }

    fun getCompressFormatFromString(formatString: String): Bitmap.CompressFormat? {
        return when (formatString.uppercase()) {
            "JPEG", "JPG" -> Bitmap.CompressFormat.JPEG
            "PNG" -> Bitmap.CompressFormat.PNG
            "WEBP" -> Bitmap.CompressFormat.WEBP // Default WEBP, usually lossy
            "WEBP_LOSSY" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Bitmap.CompressFormat.WEBP_LOSSY else Bitmap.CompressFormat.WEBP
            "WEBP_LOSSLESS" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Bitmap.CompressFormat.WEBP_LOSSLESS else Bitmap.CompressFormat.PNG // Fallback to PNG for lossless on older APIs
            else -> {
                Log.w(TAG, "Cannot get Bitmap.CompressFormat for string: $formatString")
                null
            }
        }
    }
}
