package com.shred.it.ui.screens

import android.content.*
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import com.shred.it.core.ConversionCore
import java.io.File
import java.math.BigInteger

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
    val tooltip: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversionScreen(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    val coroutineScope = rememberCoroutineScope()

    var isVisible by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<FileCategory?>(null) }
    var selectedConversion by remember { mutableStateOf<ConversionType?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var outputFileName by remember { mutableStateOf("") }
    var outputFolderUri by remember { mutableStateOf<Uri?>(null) }
    var showConfirmationDialog by remember { mutableStateOf(false) }

    val headerScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f), label = "headerScale"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(1000, delayMillis = 200), label = "contentAlpha"
    )

    val fileCategories = remember {
        listOf(
            FileCategory(
                title = "Images",
                icon = Icons.Default.Image,
                color = Color(0xFF4CAF50),
                tooltip = "Convert between various image formats",
                supportedConversions = listOf(
                    ConversionType("PNG", "JPEG", "Convert PNG to JPEG"),
                    ConversionType("JPEG", "PNG", "Convert JPEG to PNG"),
                    ConversionType("WEBP", "PNG", "Convert WEBP to PNG"),
                    ConversionType("WEBP", "JPEG", "Convert WEBP to JPEG"),
                    ConversionType("GIF", "PNG", "Convert GIF to PNG"),
                    ConversionType("BMP", "PNG", "Convert BMP to PNG"),
                    ConversionType("TIFF", "JPEG", "Convert TIFF to JPEG"),
                    ConversionType("SVG", "PNG", "Convert SVG to PNG"),
                    ConversionType("HEIC", "JPEG", "Convert HEIC to JPEG"),
                    ConversionType("AVIF", "PNG", "Convert AVIF to PNG")
                )
            ),
            FileCategory(
                title = "Documents",
                icon = Icons.Default.Description,
                color = Color(0xFF2196F3),
                tooltip = "Convert between document formats like PDF, DOCX, TXT",
                supportedConversions = listOf(
                    ConversionType("PDF", "DOCX", "Convert PDF to DOCX"),
                    ConversionType("DOCX", "PDF", "Convert DOCX to PDF"),
                    ConversionType("TXT", "PDF", "Convert TXT to PDF"),
                    ConversionType("PDF", "TXT", "Convert PDF to TXT"),
                    ConversionType("XLSX", "PDF", "Convert XLSX to PDF"),
                    ConversionType("PPTX", "PDF", "Convert PPTX to PDF"),
                    ConversionType("HTML", "PDF", "Convert HTML to PDF"),
                    ConversionType("EPUB", "PDF", "Convert EPUB to PDF"),
                    ConversionType("MOBI", "PDF", "Convert MOBI to PDF"),
                    ConversionType("RTF", "PDF", "Convert RTF to PDF")
                )
            ),
            FileCategory(
                title = "Audio",
                icon = Icons.Default.Audiotrack,
                color = Color(0xFFFFC107),
                tooltip = "Convert between audio formats like MP3, WAV, FLAC",
                supportedConversions = listOf(
                    ConversionType("MP3", "WAV", "Convert MP3 to WAV"),
                    ConversionType("WAV", "MP3", "Convert WAV to MP3"),
                    ConversionType("FLAC", "MP3", "Convert FLAC to MP3"),
                    ConversionType("AAC", "MP3", "Convert AAC to MP3"),
                    ConversionType("OGG", "MP3", "Convert OGG to MP3"),
                    ConversionType("M4A", "MP3", "Convert M4A to MP3"),
                    ConversionType("WMA", "MP3", "Convert WMA to MP3"),
                    ConversionType("AIFF", "MP3", "Convert AIFF to MP3"),
                    ConversionType("ALAC", "MP3", "Convert ALAC to MP3"),
                    ConversionType("OPUS", "MP3", "Convert OPUS to MP3")
                )
            ),
            FileCategory(
                title = "Video",
                icon = Icons.Default.Videocam,
                color = Color(0xFF9C27B0),
                tooltip = "Convert between video formats like MP4, AVI, MKV",
                supportedConversions = listOf(
                    ConversionType("MP4", "AVI", "Convert MP4 to AVI"),
                    ConversionType("AVI", "MP4", "Convert AVI to MP4"),
                    ConversionType("MKV", "MP4", "Convert MKV to MP4"),
                    ConversionType("MOV", "MP4", "Convert MOV to MP4"),
                    ConversionType("WMV", "MP4", "Convert WMV to MP4"),
                    ConversionType("FLV", "MP4", "Convert FLV to MP4"),
                    ConversionType("WEBM", "MP4", "Convert WEBM to MP4"),
                    ConversionType("MPEG", "MP4", "Convert MPEG to MP4"),
                    ConversionType("3GP", "MP4", "Convert 3GP to MP4"),
                    ConversionType("TS", "MP4", "Convert TS to MP4")
                )
            )
        )
    }

    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedFileUri = uri
        uri?.lastPathSegment?.let { originalName ->
            val baseName = originalName.substringBeforeLast('.', originalName)
            selectedConversion?.let { conv ->
                outputFileName = "${baseName}_converted.${conv.toFormat.lowercase()}"
            } ?: run {
                outputFileName = "${baseName}_converted"
            }
        } ?: run {
            outputFileName = ""
        }
    }

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        outputFolderUri = uri
        if (uri != null) {
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

    val performConversion: () -> Unit = {
        selectedFileUri?.let { inputUri ->
            selectedConversion?.let { conversion ->
                outputFolderUri?.let { outputFolder ->
                    if (outputFileName.isBlank()) {
                        Toast.makeText(context, "Please enter an output file name.", Toast.LENGTH_SHORT).show()
                        return@let
                    }

                    if (selectedCategory?.title == "Images" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        isLoading = true
                        coroutineScope.launch {
                            val targetFormat = ConversionCore.getCompressFormatFromString(conversion.toFormat)
                            if (targetFormat != null) {
                                val outputUri = ConversionCore.convertImageFile(
                                    context = context,
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
        showConfirmationDialog = false
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(headerScale),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(20.dp)
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
                                        radius = 700f
                                    )
                                )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Convert Your Files Easily",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            item {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select Category",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(fileCategories) { category ->
                            CategoryCard(
                                category = category,
                                isSelected = selectedCategory == category,
                                onClick = {
                                    selectedCategory = category
                                    selectedConversion = null
                                    selectedFileUri = null
                                    outputFileName = ""
                                }
                            )
                        }
                    }
                }
            }

            selectedCategory?.let { category ->
                item {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Select Conversion Type",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Icon(
                                imageVector = Icons.Default.Transform,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            items(category.supportedConversions) { conversion ->
                                ConversionTypeCard(
                                    conversion = conversion,
                                    isSelected = selectedConversion == conversion,
                                    onClick = {
                                        selectedConversion = conversion
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

            item {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Input File",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Icon(
                            imageVector = Icons.Default.FileUpload,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (selectedCategory?.title == "Images" && selectedConversion != null) {
                                    pickFileLauncher.launch("image/*")
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
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
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
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = selectedFileUri?.lastPathSegment ?: "No file selected",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = "Select File",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            item {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Output Settings",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

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

                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { folderPickerLauncher.launch(Uri.EMPTY) },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
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
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = outputFolderUri?.path ?: "Select folder",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.FolderShared,
                                contentDescription = "Select Folder",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(20.dp))
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
                                showConfirmationDialog = true
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = "Convert File",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }

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
                    TextButton(onClick = performConversion) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryCard(
    category: FileCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val tooltipState = rememberTooltipState() // Initialize TooltipState

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.6f), label = "categoryCardScale"
    )

    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            PlainTooltip {
                Text(category.tooltip)
            }
        },
        state = tooltipState // Pass the TooltipState here
    ) {
        Card(
            modifier = Modifier
                .width(100.dp)
                .height(80.dp)
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
            shape = RoundedCornerShape(12.dp),
            border = if (isSelected)
                BorderStroke(2.dp, category.color)
            else null
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = category.color,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = category.title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversionTypeCard(
    conversion: ConversionType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val tooltipState = rememberTooltipState() // Initialize TooltipState

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.6f), label = "conversionTypeCardScale"
    )

    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            PlainTooltip {
                Text(conversion.tooltip)
            }
        },
        state = tooltipState // Pass the TooltipState here
    ) {
        Card(
            modifier = Modifier
                .width(120.dp)
                .height(70.dp)
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
            shape = RoundedCornerShape(12.dp),
            border = if (isSelected)
                BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
            else null
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${conversion.fromFormat} to ${conversion.toFormat}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(2.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
