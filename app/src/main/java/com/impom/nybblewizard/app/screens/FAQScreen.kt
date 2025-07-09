package com.impom.nybblewizard.app.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// Assuming FlatCard is defined elsewhere in your project, e.g.:

data class FAQItem(
    val id: Int, // Add an ID for stable keys in LazyColumn
    val question: String,
    val answer: String
)

@Composable
fun FAQScreen() {
    val faqItems = remember {
        listOf(
            FAQItem(1, "What algorithm is used for overwriting?", "A sequence of random data, alternating patterns (0xAA/0x55, 0x55/0xAA), zeros, and ones. The final pass is always zeros with a flush."),
            FAQItem(2, "Is DoD 5220.22-M supported?", "No, not explicitly. It uses a custom multi-pass overwriting scheme designed for secure data destruction."),
            FAQItem(3, "How many overwrite passes are used?", "Default is 7, but it is configurable in settings."),
            FAQItem(4, "Does it rename files before deletion?", "Yes, it can rename files to a cryptographically secure random name before deletion if enabled in settings."),
            FAQItem(5, "Are temporary files shredded?", "This functionality is not directly implemented in the core shredder. It only shreds files explicitly selected by the user."),
            FAQItem(6, "Can it wipe app-specific folders?", "It can shred individual files within app-specific folders, provided the necessary permissions are granted via Android's Storage Access Framework (SAF)."),
            FAQItem(7, "Does it work on SSDs?", "Yes, it performs overwrites on SSDs. However, due to the nature of SSD wear-leveling and TRIM commands, complete data erasure cannot be guaranteed without hardware-level secure erase features. It operates on a best-effort basis."),
            FAQItem(8, "Is overwrite integrity verified?", "No, this feature is listed in settings but not currently implemented in the core shredding logic."),
            FAQItem(9, "What random number generator (RNG) is used?", "It uses `java.security.SecureRandom`, which is cryptographically strong and relies on system-provided randomness."),
            // Removed Q10, "Can it shred in background?", as WorkManager integration is not in the core class. This is an app-level feature.
            FAQItem(10, "Are multiple files supported?", "The current core logic processes one file at a time. Batch processing would be an external implementation."),
            FAQItem(11, "Is file metadata removed?", "The original filename is obfuscated during the rename step. Other file metadata (like timestamps) are not specifically targeted for wiping beyond the overwriting of the file's content."),
            FAQItem(12, "Does it bypass the recycle bin/trash?", "Yes, files are directly and securely overwritten, then deleted, bypassing the recycle bin."),
            FAQItem(13, "Can forensic tools recover shredded files?", "After the encryption, multiple overwriting passes, and deletion, it is cryptographically impossible for standard forensic tools to recover the original data."),
            FAQItem(14, "Is TRIM issued?", "The core shredder does not explicitly issue TRIM commands. Its behavior depends on how Android's ContentResolver and underlying file systems handle file deletion and overwrites."),
            FAQItem(15, "Is it FIPS certified?", "No, it is not officially FIPS certified."),
            FAQItem(16, "Is the File Allocation Table (FAT) or equivalent wiped?", "The shredder focuses on overwriting the file's content. While file deletion attempts to remove entries from the file system's allocation tables, direct wiping of these structures is not a core feature beyond what the OS provides for file deletion."),
            FAQItem(17, "Does it overwrite at byte or block level?", "It overwrites data at the byte level, grouped into blocks defined by the buffer size."),
            FAQItem(18, "Can system files be shredded?", "The shredder operates on user-selectable files via Android's Storage Access Framework. It cannot access or shred protected system files unless the device is rooted and specific permissions are granted outside the scope of this core class."),
            // Removed Q20 "Open source?" as it's a future plan, not current feature.
            FAQItem(19, "Is external storage supported?", "Yes, it supports files on external storage (like SD cards or USB drives) via Android's Storage Access Framework (SAF), provided the user grants the necessary permissions."),
            FAQItem(20, "What file types are supported?", "It operates on the raw data of any file type, so all file types are technically supported."),
            FAQItem(21, "Is progress shown?", "Yes, progress updates are provided for each shredding round and overall completion."),
            FAQItem(22, "Can shredding be paused?", "No, the current implementation does not support pausing a shredding operation."),
            FAQItem(23, "Are logs available?", "Yes, internal logs of the shredding process are maintained."),
            FAQItem(24, "Is an internet connection required?", "No, the shredder operates entirely offline."),
            // Removed Q27 "Ads?", as it's an app-level detail, not core feature.
            FAQItem(25, "Can logs be exported?", "The core shredder does not include functionality to export logs. This would be an app-level feature."),
            FAQItem(26, "Is it beginner friendly?", "As a core library, it focuses on functionality. Ease of use depends on the app's user interface built on top of it."),
            FAQItem(27, "Are file names saved?", "No, selected file names are processed for the current shredding session but not permanently stored within the core shredder."),
            FAQItem(28, "Is slack space overwritten?", "The shredder overwrites the *entire content* of the selected file. It does not specifically target or overwrite unallocated slack space beyond the file's defined length."),
            // Removed Q32 "Automated shredding supported?", as it's an app-level feature like WorkManager.
            // Removed Q33 "Dark mode?", as it's a UI/app-level feature.
            // Removed Q34 "Android 14 support?", as it's a compatibility statement, not a feature of the core logic.
            FAQItem(29, "Does it require root access?", "No, it does not require root access. It operates using Android's standard Storage Access Framework permissions."),
            FAQItem(30, "Can empty folders be shredded?", "Empty folders are deleted, not overwritten, as they contain no data to shred."),
            FAQItem(31, "What happens if shredding fails?", "If an error occurs during shredding (e.g., permission issues, I/O errors), the process will stop, and the file may be left in a partially shredded or encrypted state. An error log will be provided."),
            FAQItem(32, "Are compressed files supported?", "Yes, it processes compressed files like any other file by overwriting their raw data content."),
            FAQItem(33, "Are symbolic links handled?", "The `FileShredderCore` operates on `Uri` objects obtained via SAF, which typically resolves to the actual file content. Explicit handling or warnings for symbolic links are not present in this core logic."),
            FAQItem(34, "Are custom overwrite patterns supported?", "No, not in the current version. The overwrite patterns are predefined within the code."),
            // Removed Q41 "Exclude files?", as it's a UI/app-level feature for selection.
            FAQItem(35, "Does it integrate with file managers?", "Yes, it integrates via Android's Storage Access Framework (SAF), allowing users to select files from their preferred file manager."),
            // Removed Q43 "CLI interface?", as it's for app integration.
            FAQItem(36, "How is file access granted?", "File access is granted by the user via Android's Storage Access Framework (SAF) dialogs, and the core attempts to take persistent URI permissions."),
            FAQItem(37, "Is native (C/C++) code used?", "No, the entire core shredder is written in Kotlin."),
            FAQItem(38, "What is the default buffer size?", "The default buffer size for read/write operations is 8192 bytes (8KB)."),
            FAQItem(39, "Does it use atomic overwriting?", "No, it uses standard Java/Kotlin I/O streams and channels with explicit `force(true)` calls to ensure data is written to disk, which is not strictly 'atomic' in the hardware sense."),
            FAQItem(40, "What happens if the app crashes during shredding?", "If the application crashes during shredding, the file may be left in a partially shredded or encrypted state, as the process was interrupted. Recovery may be possible for some data."),
            FAQItem(41, "Can shredding be cancelled?", "Not gracefully. Currently, an ongoing shredding operation cannot be explicitly cancelled through the provided API. It would require force-stopping the application."),
            FAQItem(42, "Is it enterprise-ready?", "No, it is not officially certified or designed for enterprise compliance requirements.")
        )
    }

    var screenContentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100) // Small initial delay for screen composition
        screenContentVisible = true
    }

    val listState = rememberLazyListState()
    val showScrollDownHint by remember {
        derivedStateOf {
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index != faqItems.lastIndex
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        AnimatedVisibility(
            visible = screenContentVisible,
            enter = fadeIn(animationSpec = tween(durationMillis = 400, delayMillis = 100)) +
                    slideInVertically(initialOffsetY = { -it / 4 }, animationSpec = tween(durationMillis = 400, delayMillis = 100))
        ) {
            Text(
                text = "Frequently Asked Questions",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 20.dp, start = 4.dp)
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f), // Occupy remaining space
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            itemsIndexed(faqItems, key = { _, item -> item.id }) { index, faqItem ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = spring(dampingRatio = 0.7f, stiffness = 200f)) +
                            slideInVertically(
                                initialOffsetY = { it / 2 },
                                animationSpec = spring(dampingRatio = 0.7f, stiffness = 200f)
                            ),
                    exit = fadeOut(animationSpec = tween(durationMillis = 200))
                ) {
                    FAQCardItem(faqItem = faqItem)
                }
            }
        }

        // Scroll down hint
        AnimatedVisibility(
            visible = screenContentVisible && showScrollDownHint,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + shrinkVertically()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Scroll Down for more",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Scroll Down",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun FAQCardItem(faqItem: FAQItem) {
    var isExpanded by remember { mutableStateOf(false) }

    FlatCard(
        modifier = Modifier.clickable { isExpanded = !isExpanded }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = faqItem.question,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.ArrowDropDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    expandFrom = Alignment.Top,
                    animationSpec = spring(dampingRatio = 0.7f, stiffness = 250f)
                ) + fadeIn(animationSpec = tween(durationMillis = 200)),
                exit = shrinkVertically(
                    shrinkTowards = Alignment.Top,
                    animationSpec = tween(durationMillis = 250)
                ) + fadeOut(animationSpec = tween(durationMillis = 150))
            ) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = faqItem.answer,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

// Assuming FlatCard is a composable you have defined elsewhere, e.g.:
@Composable
fun FlatCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            )
            .padding(1.dp)
    ) {
        content()
    }
}