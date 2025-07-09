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
import androidx.compose.material.icons.Icons // Redundant import, already covered by androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import com.shred.it.core.FileCategory
import com.shred.it.core.ConversionType
import com.shred.it.core.fileCategories
// import java.io.File // Not used, can be removed
// import java.math.BigInteger // Not used, can be removed


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class) // Added ExperimentalLayoutApi for FlowRow
@Composable
fun ConversionScreen(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    val coroutineScope = rememberCoroutineScope()

    var isVisible by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<FileCategory?>(null) }
    var selectedFromFormat by remember { mutableStateOf<String?>(null) }
    var selectedConversion by remember { mutableStateOf<ConversionType?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var outputFileName by remember { mutableStateOf("") }
    var outputFolderUri by remember { mutableStateOf<Uri?>(null) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var conversionQuality by remember { mutableFloatStateOf(90f) }

    val headerScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f), label = "headerScale"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(1000, delayMillis = 200), label = "contentAlpha"
    )

    // CORRECTED: Moved remember calls to the correct Composable scope
    val uniqueFromFormats = remember(selectedCategory) {
        selectedCategory?.supportedConversions?.map { it.fromFormat }?.distinct()?.sorted() ?: emptyList()
    }

    val filteredConversions = remember(selectedCategory, selectedFromFormat) {
        if (selectedCategory != null && selectedFromFormat != null) {
            selectedCategory!!.supportedConversions.filter { it.fromFormat == selectedFromFormat }
        } else {
            emptyList()
        }
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
                outputFileName = "${baseName}_converted" // Default if no conversion type selected yet
            }
        } ?: run {
            outputFileName = "" // Clear if URI is null
        }
    }

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        outputFolderUri = uri
        if (uri != null) {
            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            try {
                context.contentResolver.takePersistableUriPermission(uri, takeFlags)
                Toast.makeText(context, "Save location selected.", Toast.LENGTH_SHORT).show()
            } catch (e: SecurityException) {
                Toast.makeText(context, "Failed to get permission for save location.", Toast.LENGTH_LONG).show()
                // Log.e("ConversionScreen", "Failed to take persistable URI permission", e)
                outputFolderUri = null // Reset if permission failed
            }
        } else {
            Toast.makeText(context, "No save location selected.", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        delay(100) // Small delay for animations to start after composition
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

                    isLoading = true
                    coroutineScope.launch {
                        val resultUri: Uri? = try {
                            when (selectedCategory?.title) {
                                "Images" -> {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                        ConversionCore.convertImageFile(
                                            context = context,
                                            inputUri = inputUri,
                                            outputFolderUri = outputFolder,
                                            outputFileName = outputFileName,
                                            targetFormat = conversion.toFormat,
                                            quality = conversionQuality.toInt(),
                                            contentResolver = contentResolver
                                        )
                                    } else {
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "Your Android version does not support image conversion (requires Android P+).", Toast.LENGTH_LONG).show()
                                        }
                                        null
                                    }
                                }
                                "Documents" -> {
                                    ConversionCore.convertDocumentFile(
                                        context = context,
                                        inputUri = inputUri,
                                        outputFolderUri = outputFolder,
                                        outputFileName = outputFileName,
                                        targetFormat = conversion.toFormat,
                                        contentResolver = contentResolver
                                    )
                                }
                                "Audio" -> {
                                    ConversionCore.convertAudioFile(
                                        context = context,
                                        inputUri = inputUri,
                                        outputFolderUri = outputFolder,
                                        outputFileName = outputFileName,
                                        targetFormat = conversion.toFormat,
                                        contentResolver = contentResolver
                                    )
                                }
                                "Video" -> {
                                    ConversionCore.convertVideoFile(
                                        context = context,
                                        inputUri = inputUri,
                                        outputFolderUri = outputFolder,
                                        outputFileName = outputFileName,
                                        targetFormat = conversion.toFormat,
                                        contentResolver = contentResolver
                                    )
                                }
                                else -> {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Unsupported category.", Toast.LENGTH_SHORT).show()
                                    }
                                    null
                                }
                            }
                        } catch (e: Exception) {
                            // Log.e("ConversionScreen", "Conversion error", e)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Conversion failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                            null
                        } finally {
                            isLoading = false
                        }

                        if (resultUri != null) {
                            Toast.makeText(context, "Conversion successful! Saved to: ${resultUri.path}", Toast.LENGTH_LONG).show()
                        } else {
                            if (!context.isRestricted) { // Check if context can show Toasts (e.g. not in background for too long)
                                Toast.makeText(context, "Conversion failed or was cancelled.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } ?: Toast.makeText(context, "Please select a save folder.", Toast.LENGTH_SHORT).show()
            } ?: Toast.makeText(context, "Please select a conversion type first.", Toast.LENGTH_SHORT).show()
        } ?: Toast.makeText(context, "Please select an input file first.", Toast.LENGTH_SHORT).show()
        showConfirmationDialog = false // Always hide dialog after action
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
                                    selectedFromFormat = null
                                    selectedConversion = null
                                    selectedFileUri = null
                                    outputFileName = ""
                                }
                            )
                        }
                    }
                }
            }

            if (selectedCategory != null && uniqueFromFormats.isNotEmpty()) {
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
                                text = "Select Source Format",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp) // Use this for vertical spacing in FlowRow
                        ) {
                            uniqueFromFormats.forEach { fromFormat ->
                                FormatSelectionCard(
                                    format = fromFormat,
                                    isSelected = selectedFromFormat == fromFormat,
                                    onClick = {
                                        selectedFromFormat = fromFormat
                                        selectedConversion = null
                                        selectedFileUri = null
                                        outputFileName = ""
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (selectedCategory != null && selectedFromFormat != null && filteredConversions.isNotEmpty()) {
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
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp) // Use this for vertical spacing in FlowRow
                        ) {
                            filteredConversions.forEach { conversion ->
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
                                if (selectedCategory == null) {
                                    Toast.makeText(context, "Please select a category first.", Toast.LENGTH_SHORT).show()
                                } else if (selectedFromFormat == null) {
                                    Toast.makeText(context, "Please select a source format first.", Toast.LENGTH_SHORT).show()
                                } else if (selectedConversion == null) { // Check if a conversion type is selected
                                    Toast.makeText(context, "Please select a conversion type first.", Toast.LENGTH_SHORT).show()
                                }
                                else {
                                    val mimeType = selectedCategory?.supportedConversions
                                        ?.firstOrNull { it.fromFormat == selectedFromFormat }
                                        ?.mimeType ?: "*/*" // Fallback to generic if specific mime not found
                                    pickFileLauncher.launch(mimeType)
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
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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
                        colors = OutlinedTextFieldDefaults.colors( // Use colors for Material 3
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { folderPickerLauncher.launch(null) }, // Pass null or specific initial URI if needed
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
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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

                    Spacer(modifier = Modifier.height(16.dp))

                    if (selectedCategory?.title == "Images" && selectedConversion?.toFormat?.let { it.equals("JPG", ignoreCase = true) || it.equals("JPEG", ignoreCase = true) || it.equals("WEBP", ignoreCase = true) } == true) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Quality: ${conversionQuality.toInt()}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Slider(
                                value = conversionQuality,
                                onValueChange = { conversionQuality = it },
                                valueRange = 0f..100f,
                                steps = 99, // 100 steps for 0-100
                                modifier = Modifier.fillMaxWidth(),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)
                                )
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
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Convert File",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp)) // Padding at the bottom
                }
            }
        }

        if (showConfirmationDialog) {
            AlertDialog(
                onDismissRequest = { if (!isLoading) showConfirmationDialog = false },
                title = { Text("Confirm Conversion") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Input: ${selectedFileUri?.lastPathSegment ?: "N/A"}")
                        Text("Convert: ${selectedConversion?.fromFormat ?: "N/A"} to ${selectedConversion?.toFormat ?: "N/A"}")
                        Text("Output: ${outputFileName}")
                        Text("Save To: ${outputFolderUri?.path ?: "N/A"}")
                        if (selectedCategory?.title == "Images" && selectedConversion?.toFormat?.let { it.equals("JPG", ignoreCase = true) || it.equals("JPEG", ignoreCase = true) || it.equals("WEBP", ignoreCase = true) } == true) {
                            Text("Quality: ${conversionQuality.toInt()}%")
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = performConversion,
                        enabled = !isLoading
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showConfirmationDialog = false },
                        enabled = !isLoading
                    ) {
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
    // TooltipState is not strictly necessary if not debugging or customizing tooltip visibility
    // val tooltipState = rememberTooltipState(isPersistent = true) // Use isPersistent for longer hover

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), label = "categoryCardScale"
    )

    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = { PlainTooltip { Text(category.tooltip) } },
        state = rememberTooltipState() // Each card needs its own state instance
    ) {
        Card(
            modifier = Modifier
                .width(100.dp)
                .height(80.dp)
                .scale(scale)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { offset ->
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        },
                        onTap = { onClick() }
                        // onLongPress = { tooltipState.show() } // Optional: Show tooltip on long press
                    )
                },
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected)
                    category.color.copy(alpha = 0.2f) // Slightly more pronounced selection
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f) // Consistent with other cards
            ),
            shape = RoundedCornerShape(12.dp),
            border = if (isSelected)
                BorderStroke(2.dp, category.color)
            else
                BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)) // Subtle border for unselected
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp, horizontal = 4.dp), // Adjust padding
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = category.title, // Good for accessibility
                    tint = category.color,
                    modifier = Modifier.size(28.dp) // Slightly larger icon
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = category.title,
                    style = MaterialTheme.typography.labelMedium, // Adjusted style
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormatSelectionCard(
    format: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), label = "formatSelectionCardScale"
    )

    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = { PlainTooltip { Text("Convert from .$format") } },
        state = rememberTooltipState()
    ) {
        Card(
            modifier = Modifier
                // .widthIn(min = 80.dp) // Allow flexible width
                .wrapContentWidth()
                .height(50.dp) // Reduced height
                .padding(horizontal = 4.dp) // Add padding for spacing in FlowRow
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
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            ),
            shape = RoundedCornerShape(10.dp), // Slightly smaller radius
            border = if (isSelected)
                BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            else
                BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Box( // Use Box for centering text
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 6.dp), // Adjust padding
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = format.uppercase(),
                    style = MaterialTheme.typography.labelLarge, // More prominent
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
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
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), label = "conversionTypeCardScale"
    )

    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = { PlainTooltip { Text(conversion.tooltip) } },
        state = rememberTooltipState()
    ) {
        Card(
            modifier = Modifier
                // .widthIn(min = 100.dp)
                .wrapContentWidth()
                .height(60.dp) // Adjusted height
                .padding(horizontal = 4.dp)
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
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            ),
            shape = RoundedCornerShape(10.dp),
            border = if (isSelected)
                BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
            else
                BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Column( // Kept Column for structure if needed, but could be Box for simple cases
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${conversion.fromFormat.uppercase()} to ${conversion.toFormat.uppercase()}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}