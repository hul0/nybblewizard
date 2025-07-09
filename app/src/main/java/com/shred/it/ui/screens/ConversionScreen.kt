package com.shred.it.ui.screens

import android.content.Context
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.shred.it.core.ConversionCore // Import ConversionCore
import java.io.File // Still needed for some path operations or defaults
import java.math.BigInteger

// Data class to represent a file category for conversion
data class FileCategory(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val supportedConversions: List<ConversionType>
)

// Data class to represent a specific conversion type (e.g., PNG to JPEG)
data class ConversionType(
    val fromFormat: String,
    val toFormat: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversionScreen(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    val coroutineScope = rememberCoroutineScope()

    // Animation states
    var isVisible by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<FileCategory?>(null) }
    var selectedConversion by remember { mutableStateOf<ConversionType?>(null) }
    var isLoading by remember { mutableStateOf(false) } // State for loading indicator

    // New states for renaming and folder selection
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var outputFileName by remember { mutableStateOf("") }
    var outputFolderUri by remember { mutableStateOf<Uri?>(null) }
    var showConfirmationDialog by remember { mutableStateOf(false) }

    // Animated values for header
    val headerScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f), label = "headerScale"
    )

    // Animated values for content
    val contentAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(1000, delayMillis = 200), label = "contentAlpha"
    )

    // Data for file categories and their supported conversions
    val fileCategories = remember {
        listOf(
            FileCategory(
                title = "Images",
                icon = Icons.Default.Image,
                color = Color(0xFF4CAF50), // Green
                supportedConversions = listOf(
                    ConversionType("PNG", "JPEG"),
                    ConversionType("JPEG", "PNG"),
                    ConversionType("WEBP", "PNG"),
                    ConversionType("WEBP", "JPEG"),
                    ConversionType("GIF", "PNG")
                )
            ),
            // Document, Audio, Video categories are placeholders for future implementation
            FileCategory(
                title = "Documents",
                icon = Icons.Default.Description,
                color = Color(0xFF2196F3), // Blue
                supportedConversions = listOf(
                    ConversionType("PDF", "DOCX"),
                    ConversionType("DOCX", "PDF"),
                    ConversionType("TXT", "PDF"),
                    ConversionType("PDF", "TXT")
                )
            ),
            FileCategory(
                title = "Audio",
                icon = Icons.Default.Audiotrack,
                color = Color(0xFFFFC107), // Amber
                supportedConversions = listOf(
                    ConversionType("MP3", "WAV"),
                    ConversionType("WAV", "MP3"),
                    ConversionType("FLAC", "MP3")
                )
            ),
            FileCategory(
                title = "Video",
                icon = Icons.Default.Videocam,
                color = Color(0xFF9C27B0), // Purple
                supportedConversions = listOf(
                    ConversionType("MP4", "AVI"),
                    ConversionType("AVI", "MP4"),
                    ConversionType("MKV", "MP4")
                )
            )
        )
    }

    // Launcher for picking an input image file
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedFileUri = uri
        // Automatically set a default output file name based on input and selected conversion
        uri?.lastPathSegment?.let { originalName ->
            val baseName = originalName.substringBeforeLast('.', originalName)
            selectedConversion?.let { conv ->
                outputFileName = "${baseName}_converted.${conv.toFormat.lowercase()}"
            } ?: run {
                outputFileName = "${baseName}_converted" // Fallback if no conversion selected yet
            }
        } ?: run {
            outputFileName = ""
        }
    }

    // Launcher for picking an output folder (directory)
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        outputFolderUri = uri
        if (uri != null) {
            // Persist permission for this URI
            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            Toast.makeText(context, "Save location selected.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "No save location selected.", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    // Function to perform the actual conversion
    val performConversion: () -> Unit = {
        selectedFileUri?.let { inputUri ->
            selectedConversion?.let { conversion ->
                outputFolderUri?.let { outputFolder ->
                    if (outputFileName.isBlank()) {
                        Toast.makeText(context, "Please enter an output file name.", Toast.LENGTH_SHORT).show()
                        return@let
                    }

                    // Check if the selected category is "Images" and if the Android version supports ImageDecoder
                    if (selectedCategory?.title == "Images" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        isLoading = true
                        coroutineScope.launch {
                            val targetFormat = ConversionCore.getCompressFormatFromString(conversion.toFormat)
                            if (targetFormat != null) {
                                val outputUri = ConversionCore.convertImageFile(
                                    context = context, // Pass context
                                    inputUri = inputUri,
                                    outputFolderUri = outputFolder,
                                    outputFileName = outputFileName,
                                    targetFormat = targetFormat,
                                    contentResolver = contentResolver
                                )

                                isLoading = false
                                if (outputUri != null) {
                                    Toast.makeText(context, "Conversion successful! Saved to: ${outputUri.path}", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "Conversion failed!", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                isLoading = false
                                Toast.makeText(context, "Unsupported target format: ${conversion.toFormat}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else if (selectedCategory?.title != "Images") {
                        Toast.makeText(context, "Only image conversions are supported for now.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Your Android version does not support this conversion (requires Android P+).", Toast.LENGTH_SHORT).show()
                    }
                } ?: Toast.makeText(context, "Please select a save folder.", Toast.LENGTH_SHORT).show()
            } ?: Toast.makeText(context, "Please select a conversion type first.", Toast.LENGTH_SHORT).show()
        } ?: Toast.makeText(context, "Please select an input file first.", Toast.LENGTH_SHORT).show()
        showConfirmationDialog = false // Dismiss dialog after initiating conversion
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .alpha(contentAlpha),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(headerScale),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top App Bar


                    // Hero Section
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                            Color.Transparent
                                        ),
                                        radius = 800f
                                    )
                                )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "Convert Your Files Easily",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Category Selection Section
            item {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select Category",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp), // Reduced spacing
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        items(fileCategories) { category ->
                            CategoryCard(
                                category = category,
                                isSelected = selectedCategory == category,
                                onClick = {
                                    selectedCategory = category
                                    selectedConversion = null // Reset conversion type on category change
                                    selectedFileUri = null // Reset selected file
                                    outputFileName = "" // Reset output file name
                                }
                            )
                        }
                    }
                }
            }

            // Conversion Type Selection Section
            selectedCategory?.let { category ->
                item {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Select Conversion Type",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Icon(
                                imageVector = Icons.Default.Transform,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp), // Reduced spacing
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            items(category.supportedConversions) { conversion ->
                                ConversionTypeCard(
                                    conversion = conversion,
                                    isSelected = selectedConversion == conversion,
                                    onClick = {
                                        selectedConversion = conversion
                                        // Update default output file name if a file is already selected
                                        selectedFileUri?.lastPathSegment?.let { originalName ->
                                            val baseName = originalName.substringBeforeLast('.', originalName)
                                            outputFileName = "${baseName}_converted.${conversion.toFormat.lowercase()}"
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Input File Selection Section
            item {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Input File",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Icon(
                            imageVector = Icons.Default.FileUpload,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (selectedCategory?.title == "Images" && selectedConversion != null) {
                                    pickImageLauncher.launch("image/*")
                                } else if (selectedCategory == null) {
                                    Toast.makeText(context, "Please select a category first.", Toast.LENGTH_SHORT).show()
                                } else if (selectedConversion == null) {
                                    Toast.makeText(context, "Please select a conversion type first.", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Only image conversions are supported for now.", Toast.LENGTH_SHORT).show()
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Select File",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = selectedFileUri?.lastPathSegment ?: "No file selected",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = "Select File",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // Output Settings Section (New Feature)
            item {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Output Settings",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Output File Name Input
                    OutlinedTextField(
                        value = outputFileName,
                        onValueChange = { outputFileName = it },
                        label = { Text("Output File Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Save Location Selection
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { folderPickerLauncher.launch(Uri.EMPTY) }, // Uri.EMPTY is a placeholder, actual URI doesn't matter for OpenDocumentTree
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Save Location",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = outputFolderUri?.path ?: "Select folder",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.FolderShared,
                                contentDescription = "Select Folder",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // Convert Button Section
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            if (selectedFileUri == null) {
                                Toast.makeText(context, "Please select an input file.", Toast.LENGTH_SHORT).show()
                            } else if (selectedConversion == null) {
                                Toast.makeText(context, "Please select a conversion type.", Toast.LENGTH_SHORT).show()
                            } else if (outputFileName.isBlank()) {
                                Toast.makeText(context, "Please enter an output file name.", Toast.LENGTH_SHORT).show()
                            } else if (outputFolderUri == null) {
                                Toast.makeText(context, "Please select a save location.", Toast.LENGTH_SHORT).show()
                            } else {
                                showConfirmationDialog = true // Show confirmation dialog
                            }
                        },
                        enabled = !isLoading, // Disable button while loading
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text(
                                text = "Convert File",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Confirmation Dialog (New Feature)
        if (showConfirmationDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmationDialog = false },
                title = { Text("Confirm Conversion") },
                text = {
                    Column {
                        Text("Input File: ${selectedFileUri?.lastPathSegment ?: "N/A"}")
                        Text("Conversion: ${selectedConversion?.fromFormat ?: "N/A"} to ${selectedConversion?.toFormat ?: "N/A"}")
                        Text("Output Name: ${outputFileName}")
                        Text("Save Location: ${outputFolderUri?.path ?: "N/A"}")
                    }
                },
                confirmButton = {
                    TextButton(onClick = performConversion) { // Call performConversion on confirm
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmationDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun CategoryCard(
    category: FileCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.6f), label = "categoryCardScale"
    )

    Card(
        modifier = Modifier
            .width(120.dp) // Reduced width
            .height(100.dp) // Reduced height
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                category.color.copy(alpha = 0.1f)
            else
                MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (isSelected)
            BorderStroke(2.dp, category.color)
        else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp), // Adjusted padding
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                tint = category.color,
                modifier = Modifier.size(28.dp) // Adjusted icon size
            )

            Spacer(modifier = Modifier.height(4.dp)) // Adjusted spacing

            Text(
                text = category.title,
                style = MaterialTheme.typography.titleSmall, // Kept titleSmall for readability
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ConversionTypeCard(
    conversion: ConversionType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.6f), label = "conversionTypeCardScale"
    )

    Card(
        modifier = Modifier
            .width(140.dp) // Reduced width
            .height(80.dp) // Reduced height
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (isSelected)
            BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
        else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp), // Adjusted padding
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${conversion.fromFormat} to ${conversion.toFormat}",
                style = MaterialTheme.typography.bodyLarge, // Changed to bodyLarge for better fit
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp)) // Adjusted spacing
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp) // Adjusted icon size
            )
        }
    }
}
