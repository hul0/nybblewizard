package com.shred.it.core

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.util.Log
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
                null
            }
        } catch (e: Exception) {
            null
        } finally {
            try {
                outputStream.close()
            } catch (e: IOException) {
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    suspend fun convertImageFile(
        context: Context,
        inputUri: Uri,
        outputFolderUri: Uri,
        outputFileName: String,
        targetFormatString: String,
        quality: Int = 90,
        contentResolver: ContentResolver
    ): Uri? {
        var decodedBitmap: Bitmap? = null
        var outputStream: OutputStream? = null
        var newFileUri: Uri? = null

        try {
            decodedBitmap = decodeImageToBitmap(context, inputUri, contentResolver)

            if (decodedBitmap == null) {
                return null
            }

            val outputMimeType = getMimeTypeForFormat(targetFormatString)
            if (outputMimeType == null) {
                return null
            }

            newFileUri = DocumentsContract.createDocument(
                contentResolver,
                DocumentsContract.buildDocumentUriUsingTree(outputFolderUri, DocumentsContract.getTreeDocumentId(outputFolderUri)),
                outputMimeType,
                outputFileName
            )

            if (newFileUri == null) {
                return null
            }

            outputStream = contentResolver.openOutputStream(newFileUri)
            if (outputStream == null) {
                DocumentsContract.deleteDocument(contentResolver, newFileUri)
                return null
            }

            val success = encodeBitmapToFile(decodedBitmap, targetFormatString, quality, outputStream)

            if (!success) {
                DocumentsContract.deleteDocument(contentResolver, newFileUri)
                return null
            }

            return newFileUri

        } catch (e: SecurityException) {
        } catch (e: IOException) {
        } catch (e: OutOfMemoryError) {
        } catch (e: Exception) {
        } finally {
            try {
                outputStream?.close()
            } catch (e: IOException) {
            }
            decodedBitmap?.recycle()
        }
        return null
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun decodeImageToBitmap(context: Context, inputUri: Uri, contentResolver: ContentResolver): Bitmap? {
        val mimeType = contentResolver.getType(inputUri)

        var bitmap: Bitmap? = null
        var inputStream: InputStream? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, inputUri)
                bitmap = ImageDecoder.decodeBitmap(source) { decoder, info, src ->
                    decoder.isMutableRequired = true
                }
            }

            if (bitmap == null) {
                when (mimeType) {
                    "image/svg+xml" -> {
                        inputStream = contentResolver.openInputStream(inputUri)
                        inputStream?.let {
                            bitmap = decodeSvgToBitmap(it)
                        }
                    }
                    "image/heic", "image/heif", "image/avif" -> {
                        inputStream = contentResolver.openInputStream(inputUri)
                        inputStream?.let {
                            bitmap = BitmapFactory.decodeStream(it)
                        }
                    }
                    else -> {
                        inputStream = contentResolver.openInputStream(inputUri)
                        inputStream?.let {
                            bitmap = BitmapFactory.decodeStream(it)
                        }
                    }
                }
            }

        } catch (e: ImageDecoder.DecodeException) {
        } catch (e: IOException) {
        } catch (e: Exception) {
        } finally {
            try {
                inputStream?.close()
            } catch (e: IOException) {
            }
        }
        return bitmap
    }

    private fun encodeBitmapToFile(
        bitmap: Bitmap,
        targetFormatString: String,
        quality: Int,
        outputStream: OutputStream
    ): Boolean {
        getCompressFormatFromString(targetFormatString)?.let { compressFormat ->
            return bitmap.compress(compressFormat, quality, outputStream)
        }

        return when (targetFormatString.uppercase()) {
            "SVG" -> false
            "HEIC", "HEIF" -> false
            "AVIF" -> false
            else -> false
        }
    }

    private fun decodeSvgToBitmap(inputStream: InputStream): Bitmap? {
        return try {
            val svg = SVG.getFromInputStream(inputStream)
            val picture = svg.renderToPicture()
            val bitmap = Bitmap.createBitmap(picture.width, picture.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            picture.draw(canvas)
            bitmap
        } catch (e: SVGParseException) {
            null
        } catch (e: Exception) {
            null
        }
    }

    fun getMimeTypeForFormat(formatString: String): String? {
        return when (formatString.uppercase()) {
            "JPEG", "JPG" -> "image/jpeg"
            "PNG" -> "image/png"
            "WEBP", "WEBP_LOSSY", "WEBP_LOSSLESS" -> "image/webp"
            "HEIC", "HEIF" -> "image/heic"
            "AVIF" -> "image/avif"
            "SVG" -> "image/svg+xml"
            else -> null
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
            else -> "dat"
        }
    }

    fun getCompressFormatFromString(formatString: String): Bitmap.CompressFormat? {
        return when (formatString.uppercase()) {
            "JPEG", "JPG" -> Bitmap.CompressFormat.JPEG
            "PNG" -> Bitmap.CompressFormat.PNG
            "WEBP" -> Bitmap.CompressFormat.WEBP
            "WEBP_LOSSY" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Bitmap.CompressFormat.WEBP_LOSSY else Bitmap.CompressFormat.WEBP
            "WEBP_LOSSLESS" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Bitmap.CompressFormat.WEBP_LOSSLESS else Bitmap.CompressFormat.WEBP
            else -> null
        }
    }
}
