package com.shred.it.core

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi

/**
 * Central object for orchestrating various file conversions.
 * It delegates conversion tasks to specific conversion modules (ImgConv, VidConv, etc.).
 */
object ConversionCore {

    private const val TAG = "ConversionCore"

    /**
     * Initiates an image file conversion.
     * Delegates the call to ImgConv.
     *
     * @param context The application context.
     * @param inputUri The Uri of the input image file.
     * @param outputFolderUri The Uri of the directory where the converted file should be saved.
     * @param outputFileName The desired name for the output file.
     * @param targetFormat The desired output format (e.g., "JPEG", "PNG").
     * @param quality The compression quality (0-100), primarily for lossy image formats.
     * @param contentResolver The ContentResolver instance.
     * @return The Uri of the newly created converted file, or null if the conversion fails.
     */
    @RequiresApi(Build.VERSION_CODES.P)
    suspend fun convertImageFile(
        context: Context,
        inputUri: Uri,
        outputFolderUri: Uri,
        outputFileName: String,
        targetFormat: String,
        quality: Int,
        contentResolver: ContentResolver
    ): Uri? {
        val bitmapTargetFormat = ImgConv.getCompressFormatFromString(targetFormat)
        return if (bitmapTargetFormat != null) {
            ImgConv.convertImageFile(
                context,
                inputUri,
                outputFolderUri,
                outputFileName,
                bitmapTargetFormat,
                quality,
                contentResolver
            )
        } else {
            Log.e(TAG, "Unsupported image target format: $targetFormat")
            null
        }
    }

    /**
     * Initiates a video file conversion.
     * Delegates the call to VidConv.
     *
     * @param context The application context.
     * @param inputUri The Uri of the input video file.
     * @param outputFolderUri The Uri of the directory where the converted file should be saved.
     * @param outputFileName The desired name for the output file.
     * @param targetFormat The desired output format (e.g., "MP4", "AVI").
     * @param contentResolver The ContentResolver instance.
     * @return The Uri of the newly created converted file, or null if the conversion fails.
     */
    fun convertVideoFile(
        context: Context,
        inputUri: Uri,
        outputFolderUri: Uri,
        outputFileName: String,
        targetFormat: String,
        contentResolver: ContentResolver
    ): Uri? {
        Log.w(TAG, "Video conversion is not yet implemented.")
        return VidConv.convertVideoFile(context, inputUri, outputFolderUri, outputFileName, targetFormat, contentResolver)
    }

    /**
     * Initiates a document file conversion.
     * Delegates the call to DocConv.
     *
     * @param context The application context.
     * @param inputUri The Uri of the input document file.
     * @param outputFolderUri The Uri of the directory where the converted file should be saved.
     * @param outputFileName The desired name for the output file.
     * @param targetFormat The desired output format (e.g., "PDF", "DOCX").
     * @param contentResolver The ContentResolver instance.
     * @return The Uri of the newly created converted file, or null if the conversion fails.
     */
    fun convertDocumentFile(
        context: Context,
        inputUri: Uri,
        outputFolderUri: Uri,
        outputFileName: String,
        targetFormat: String,
        contentResolver: ContentResolver
    ): Uri? {
        Log.w(TAG, "Document conversion is not yet implemented.")
        return DocConv.convertDocumentFile(context, inputUri, outputFolderUri, outputFileName, targetFormat, contentResolver)
    }

    /**
     * Initiates an audio file conversion.
     * Delegates the call to AudConv.
     *
     * @param context The application context.
     * @param inputUri The Uri of the input audio file.
     * @param outputFolderUri The Uri of the directory where the converted file should be saved.
     * @param outputFileName The desired name for the output file.
     * @param targetFormat The desired output format (e.g., "MP3", "WAV").
     * @param contentResolver The ContentResolver instance.
     * @return The Uri of the newly created converted file, or null if the conversion fails.
     */
    fun convertAudioFile(
        context: Context,
        inputUri: Uri,
        outputFolderUri: Uri,
        outputFileName: String,
        targetFormat: String,
        contentResolver: ContentResolver
    ): Uri? {
        Log.w(TAG, "Audio conversion is not yet implemented.")
        return AudConv.convertAudioFile(context, inputUri, outputFolderUri, outputFileName, targetFormat, contentResolver)
    }

    /**
     * Helper to get the Bitmap.CompressFormat from a string.
     * This method is now part of ImgConv, but kept here for backward compatibility or direct use if needed.
     */
    fun getCompressFormatFromString(formatString: String): Bitmap.CompressFormat? {
        return ImgConv.getCompressFormatFromString(formatString)
    }
}
