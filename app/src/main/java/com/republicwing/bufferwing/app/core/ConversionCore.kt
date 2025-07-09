package com.republicwing.bufferwing.core

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi

object ConversionCore {

    /**
     * Initiates an image file conversion by delegating to ImgConv.
     *
     * @param context The application context.
     * @param inputUri Uri of the source image.
     * @param outputFolderUri Uri of the destination folder.
     * @param outputFileName Desired output filename without extension.
     * @param targetFormat Desired Bitmap.CompressFormat (JPEG, PNG, WEBP).
     * @param quality Compression quality (0–100).
     * @param contentResolver ContentResolver instance.
     * @return Uri of the converted file or null if conversion fails.
     */
    @RequiresApi(Build.VERSION_CODES.P)
    suspend fun convertImageFile(
        context: Context,
        inputUri: Uri,
        outputFolderUri: Uri,
        outputFileName: String,
        targetFormat: Bitmap.CompressFormat,
        quality: Int,
        contentResolver: ContentResolver
    ): Uri? {
        return ImgConv.convertImageFile(
            context,
            inputUri,
            outputFolderUri,
            outputFileName,
            targetFormat,
            quality,
            contentResolver
        )
    }

    /**
     * Initiates a video file conversion by delegating to VidConv.
     */
    fun convertVideoFile(
        context: Context,
        inputUri: Uri,
        outputFolderUri: Uri,
        outputFileName: String,
        targetFormat: String,
        contentResolver: ContentResolver
    ): Uri? {
        return VidConv.convertVideoFile(
            context,
            inputUri,
            outputFolderUri,
            outputFileName,
            targetFormat,
            contentResolver
        )
    }

    /**
     * Initiates a document file conversion by delegating to DocConv.
     */
    fun convertDocumentFile(
        context: Context,
        inputUri: Uri,
        outputFolderUri: Uri,
        outputFileName: String,
        targetFormat: String,
        contentResolver: ContentResolver
    ): Uri? {
        return DocConv.convertDocumentFile(
            context,
            inputUri,
            outputFolderUri,
            outputFileName,
            targetFormat,
            contentResolver
        )
    }

    /**
     * Initiates an audio file conversion by delegating to AudConv.
     */
    fun convertAudioFile(
        context: Context,
        inputUri: Uri,
        outputFolderUri: Uri,
        outputFileName: String,
        targetFormat: String,
        contentResolver: ContentResolver
    ): Uri? {
        return AudConv.convertAudioFile(
            context,
            inputUri,
            outputFolderUri,
            outputFileName,
            targetFormat,
            contentResolver
        )
    }

    /**
     * Helper to convert a format string (e.g., "JPEG", "PNG") into a Bitmap.CompressFormat.
     */
    fun getCompressFormatFromString(formatString: String): Bitmap.CompressFormat? {
        return ImgConv.getCompressFormatFromString(formatString)
    }
}
