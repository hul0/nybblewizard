package com.republicwing.bufferwing.core

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

data class FileCategory(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val supportedConversions: List<ConversionType>,
    val tooltip: String
)

data class ConversionType(
    val fromFormat: String,
    val toFormat: String,
    val tooltip: String,
    val mimeType: String // This field is correctly defined
)

// Helper function to get a generic MIME type based on format
// This is a simplified example. For robust MIME type handling,
// you might use a more comprehensive mapping or a library.
fun getMimeTypeForFormat(format: String, categoryTitle: String): String {
    return when (categoryTitle) {
        "Images" -> "image/${format.lowercase()}"
        "Documents" -> when (format.uppercase()) {
            "PDF" -> "application/pdf"
            "DOCX" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "TXT" -> "text/plain"
            "HTML" -> "text/html"
            "CSV" -> "text/csv"
            // Add more specific document MIME types as needed
            else -> "application/octet-stream" // Generic fallback for documents
        }
        "Audio" -> "audio/${format.lowercase()}"
        "Video" -> "video/${format.lowercase()}"
        else -> "*/*" // Generic fallback
    }
}

// Helper function to generate all-to-all conversions for a given list of formats
fun generateAllToAllConversions(formats: List<String>, categoryTitle: String): List<ConversionType> {
    val conversions = mutableListOf<ConversionType>()
    for (from in formats) {
        for (to in formats) {
            if (from != to) {
                // Determine the MIME type for the 'from' format
                val fromMimeType = getMimeTypeForFormat(from, categoryTitle)
                conversions.add(
                    ConversionType(
                        fromFormat = from,
                        toFormat = to,
                        tooltip = "Convert $from to $to",
                        mimeType = fromMimeType // Provide the mimeType here
                    )
                )
            }
        }
    }
    return conversions
}

// Define simplified lists of formats for each category
val imageFormats = listOf(
    "PNG", "JPEG", "WEBP" // Kept these as they are very common web/mobile formats
)

val documentFormats = listOf(
    "PDF", "DOCX", "TXT", "HTML", "CSV" // Focused on widely used document types
)

val audioFormats = listOf(
    "MP3", "WAV", "AAC", "M4A" // Common audio formats
)

val videoFormats = listOf(
    "MP4", "AVI", "MKV", "MOV" // Popular video container formats
)

val fileCategories = listOf(
    FileCategory(
        title = "Images",
        icon = Icons.Default.Image,
        color = Color(0xFF4CAF50),
        tooltip = "Convert between various image formats",
        supportedConversions = generateAllToAllConversions(imageFormats, "Images") // Pass category title
    ),
    FileCategory(
        title = "Documents",
        icon = Icons.Default.Description,
        color = Color(0xFF2196F3),
        tooltip = "Convert between document formats like PDF, DOCX, TXT, etc.",
        supportedConversions = generateAllToAllConversions(documentFormats, "Documents") // Pass category title
    ),
    FileCategory(
        title = "Audio",
        icon = Icons.Default.Audiotrack,
        color = Color(0xFFFFC107),
        tooltip = "Convert between audio formats like MP3, WAV, FLAC, etc.",
        supportedConversions = generateAllToAllConversions(audioFormats, "Audio") // Pass category title
    ),
    FileCategory(
        title = "Video",
        icon = Icons.Default.Videocam,
        color = Color(0xFF9C27B0),
        tooltip = "Convert between video formats like MP4, AVI, MKV, etc.",
        supportedConversions = generateAllToAllConversions(videoFormats, "Video") // Pass category title
    )
)
