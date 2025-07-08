package com.shred.it.ui.reusable

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.shred.it.core.FileInfo

@Composable
fun FilePreviewCard(
    file: FileInfo,
    onClearFile: () -> Unit,
    formatFileSize: (Long) -> String // Function to format file size
) {
    val colors = MaterialTheme.colorScheme
    val isImage = file.mimeType.startsWith("image/")
    val context = LocalContext.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 3.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Conditionally display image or default icon
            if (isImage) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp) // Fixed height for image preview
                        .clip(RoundedCornerShape(8.dp)), // Slightly less rounded corners for inner image
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = file.uri,
                        contentDescription = "Selected Image Preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(12.dp)) // Space between image and text
            } else {
                // If not an image, show a generic file icon
                Icon(
                    Icons.AutoMirrored.Filled.InsertDriveFile,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.CenterHorizontally),
                    tint = colors.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // File details and clear button (common for both image and non-image files)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = file.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${formatFileSize(file.size)} • ${file.mimeType}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.SansSerif),
                        color = colors.onSurfaceVariant
                    )
                }
                Row {
                    IconButton(onClick = {
                        // Share button logic (Assuming file.uri is correct for sharing)
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_STREAM, file.uri)
                            type = "*/*"
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Grant URI permission for sharing
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Share File",
                            tint = colors.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onClearFile) {
                        Icon(Icons.Default.Close, contentDescription = "Clear Selected File", tint = colors.onSurfaceVariant)
                    }
                }
            }
        }
    }
}