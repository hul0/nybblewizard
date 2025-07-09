package com.republicwing.bufferwing.app.screens

import android.content.* // Kept for LocalContext, can be pruned if context isn't used for Toasts etc.
 import android.net.Uri // Kept for placeholder types, can be pruned if not used.
import android.os.Build // Kept for completeness, not actively used in static version
import android.widget.Toast // Kept for "Coming Soon" or disabled action feedback
import androidx.activity.compose.rememberLauncherForActivityResult
// import androidx.activity.compose.rememberLauncherForActivityResult // Kept for structure, but launcher is inert
// import androidx.activity.result.contract.ActivityResultContracts // Kept for structure
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
// import androidx.compose.runtime.getValue // Not strictly needed if not using delegates for all states
// import androidx.compose.runtime.setValue // Not strictly needed if not using delegates for all states
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// import kotlinx.coroutines.* // Can be removed if no coroutines are launched
// Import core types for data structures even if logic is static
import com.republicwing.bufferwing.core.FileCategory
import com.republicwing.bufferwing.core.ConversionType
import com.republicwing.bufferwing.core.fileCategories
// import com.republicwing.bufferwing.core.ImgConv // Kept for structure if ConversionTypeCard is restored


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ConversionScreen(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    // val contentResolver = context.contentResolver // Not used in static version
    // val coroutineScope = rememberCoroutineScope() // Not used for active conversion

    var isVisible by remember { mutableStateOf(false) } // For initial animation
    // Static placeholders or default values, not driven by user interaction
    val staticSelectedCategory: FileCategory? = fileCategories.firstOrNull { it.title == "Images" } // Default to images for display
    val staticSelectedFromFormat: String? = staticSelectedCategory?.supportedConversions?.firstOrNull()?.fromFormat // Default
    val staticSelectedConversion: ConversionType? = staticSelectedCategory?.supportedConversions?.firstOrNull() // Default
    val isLoading by remember { mutableStateOf(false) } // Kept for button state, but won't change

    val staticSelectedFileUri: android.net.Uri? = null // Placeholder
    var outputFileName by remember { mutableStateOf("example_converted.png") } // Placeholder
    val staticOutputFolderUri: android.net.Uri? = null // Placeholder
    var showConfirmationDialog by remember { mutableStateOf(false) } // Can be triggered for UI display
    var conversionQuality by remember { mutableFloatStateOf(90f) } // Static display

    val headerScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f), label = "headerScale"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(1000, delayMillis = 200), label = "contentAlpha"
    )

    // Data for UI display, not reactive to interactions in this static version
    val uniqueFromFormats = staticSelectedCategory?.supportedConversions?.map { it.fromFormat }?.distinct()?.sorted() ?: emptyList()
    val filteredConversions = staticSelectedCategory?.supportedConversions?.filter { it.fromFormat == staticSelectedFromFormat } ?: emptyList()

    // Launchers are defined but their launch calls will be disabled or lead to no action
    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { _ ->
        // No-op for static UI
        Toast.makeText(context, "File selection is disabled in static mode.", Toast.LENGTH_SHORT).show()
    }

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree()
    ) { _ ->
        // No-op for static UI
        Toast.makeText(context, "Folder selection is disabled in static mode.", Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(Unit) {
        // delay(100) // Original delay
        isVisible = true
    }

    val performConversion: () -> Unit = {
        Toast.makeText(context, "Conversion is disabled in static mode.", Toast.LENGTH_SHORT).show()
        showConfirmationDialog = false // Close dialog if it was opened
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
                            val isEnabled = category.title == "Images" // Only "Images" visually enabled
                            CategoryCard(
                                category = category,
                                isSelected = staticSelectedCategory == category,
                                isEnabled = isEnabled,
                                onClick = {
                                    if (!isEnabled) {
                                        Toast.makeText(context, "${category.title} conversion is coming soon!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Category selection is disabled in static mode.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Always show subsequent sections as if "Images" category is selected for static display
            // item { Spacer(modifier = Modifier.height(8.dp)) } // Add space if needed

            if (uniqueFromFormats.isNotEmpty()) {
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
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            uniqueFromFormats.forEach { fromFormat ->
                                FormatSelectionCard(
                                    format = fromFormat,
                                    isSelected = staticSelectedFromFormat == fromFormat,
                                    onClick = {
                                        Toast.makeText(context, "Format selection is disabled in static mode.", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (filteredConversions.isNotEmpty()) {
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
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            filteredConversions.forEach { conversion ->
                                ConversionTypeCard( // Assuming ConversionTypeCard is implemented or TODO
                                    conversion = conversion,
                                    isSelected = staticSelectedConversion == conversion,
                                    onClick = {
                                        Toast.makeText(context, "Conversion type selection is disabled in static mode.", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item { // Input File Section - always shown
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
                                // pickFileLauncher.launch("image/*") // Disabled for static
                                Toast.makeText(
                                    context,
                                    "File selection is disabled in static mode.",
                                    Toast.LENGTH_SHORT
                                ).show()
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
                                    text = staticSelectedFileUri?.lastPathSegment ?: "No file selected (static)",
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

            item { // Output Settings - always shown
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
                        onValueChange = { outputFileName = it }, // Kept for visual interaction if typed
                        label = { Text("Output File Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = true, // Visually enabled
                        colors = OutlinedTextFieldDefaults.colors(
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
                            .clickable {
                                // folderPickerLauncher.launch(null) // Disabled for static
                                Toast.makeText(
                                    context,
                                    "Folder selection is disabled in static mode.",
                                    Toast.LENGTH_SHORT
                                ).show()
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
                                    text = "Save Location",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = staticOutputFolderUri?.path ?: "Select folder (static)",
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

                    // Show quality slider statically if it's for JPG/WEBP
                    if (staticSelectedConversion?.toFormat?.let { it.equals("JPG", ignoreCase = true) || it.equals("JPEG", ignoreCase = true) || it.equals("WEBP", ignoreCase = true) } == true) {
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
                                onValueChange = { conversionQuality = it }, // Kept for visual interaction
                                valueRange = 0f..100f,
                                steps = 99,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = true, // Visually enabled
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

            item { // Convert Button
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            // Show confirmation dialog for UI display purposes
                            showConfirmationDialog = true
                        },
                        enabled = !isLoading, // Button is visually enabled
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        )
                    ) {
                        if (isLoading) { // This state won't change to true in static mode
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
                    Spacer(modifier = Modifier.height(20.dp)) // Bottom padding
                }
            }
        }

        if (showConfirmationDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmationDialog = false },
                title = { Text("Confirm Conversion") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Input: ${staticSelectedFileUri?.lastPathSegment ?: "N/A (static)"}")
                        Text("Convert: ${staticSelectedConversion?.fromFormat ?: "N/A"} to ${staticSelectedConversion?.toFormat ?: "N/A"}")
                        Text("Output: $outputFileName")
                        Text("Save To: ${staticOutputFolderUri?.path ?: "N/A (static)"}")
                        if (staticSelectedCategory?.title == "Images" && staticSelectedConversion?.toFormat?.let { it.equals("JPG", ignoreCase = true) || it.equals("JPEG", ignoreCase = true) || it.equals("WEBP", ignoreCase = true) } == true) {
                            Text("Quality: ${conversionQuality.toInt()}%")
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = performConversion, // Will show "disabled in static mode" toast
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

@Composable
fun ConversionTypeCard(conversion: ConversionType, isSelected: Boolean, onClick: () -> Unit) {
    // Using the original TODO and commented out implementation
    TODO("Not yet implemented")
    /*
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
                    .wrapContentWidth()
                    .height(60.dp)
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
                Column(
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
                }
            }
        }
    */
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryCard(
    category: FileCategory,
    isSelected: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed && isEnabled) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), label = "categoryCardScale"
    )
    val cardAlpha = if (isEnabled) 1f else 0.5f

    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = { PlainTooltip { Text(if (isEnabled) category.tooltip else "${category.title} (Coming Soon)") } },
        state = rememberTooltipState()
    ) {
        Card(
            modifier = Modifier
                .width(100.dp)
                .height(80.dp)
                .scale(scale)
                .alpha(cardAlpha)
                .pointerInput(isEnabled) {
                    if (isEnabled) {
                        detectTapGestures(
                            onPress = {
                                isPressed = true
                                tryAwaitRelease()
                                isPressed = false
                            },
                            onTap = { onClick() }
                        )
                    } else {
                        detectTapGestures(onTap = { onClick() })
                    }
                },
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected && isEnabled)
                    category.color.copy(alpha = 0.2f)
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isEnabled) 0.7f else 0.3f)
            ),
            shape = RoundedCornerShape(12.dp),
            border = if (isSelected && isEnabled)
                BorderStroke(2.dp, category.color)
            else
                BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = if (isEnabled) 0.5f else 0.2f))
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = category.title,
                        tint = if (isEnabled) category.color else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = category.title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isEnabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
                if (!isEnabled) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Black.copy(alpha = 0.0f))
                    )
                    Text(
                        text = "Coming\nSoon",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
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
                .wrapContentWidth()
                .height(50.dp)
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
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            ),
            shape = RoundedCornerShape(10.dp),
            border = if (isSelected)
                BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            else
                BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = format.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
