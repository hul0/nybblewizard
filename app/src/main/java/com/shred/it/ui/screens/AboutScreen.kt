package com.shred.it.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// The legally optimized infoList (remains unchanged)
val infoList = listOf(
    "How It Works" to
            "This app uses AES-256 encryption and multi-pass secure overwriting (e.g., DoD 5220.22-M inspired patterns) to irreversibly destroy data. It performs real-time buffer randomization, journal flushing, and overwrite verification to maximize shredding reliability within the physical limits of your device.",

    "Overwrite Verification" to
            "After each overwrite round, a configurable sampling strategy checks random file regions for conformity to the expected pattern (e.g., zeros in final pass), improving user assurance without requiring full data re-reading.",

    "Local-Only Operation" to
            "All operations happen 100% locally on your device. There is no server, cloud, telemetry, or external sync. The app does not request the internet permission at all.",

    "Buffer Management" to
            "The shredder uses adaptive dual-buffer logic to reduce memory fragmentation and ensure back-pressure aware writes, minimizing performance spikes and improving predictability.",

    "Security Guarantees" to
            "While designed using best-practice methods for secure deletion, no mobile storage (especially SSDs) can offer perfect, irreversible erasure. This app guarantees 'best-effort' destruction—not absolute forensic immunity.",

    "File Validation & Pre-Checks" to
            "Before shredding, each file is checked for writability, correct access flags, and reasonable size vs memory usage ratios. Unsafe operations are aborted and reported.",

    "Device-Aware Logic" to
            "Storage types (SSD, HDD, removable, network) are detected heuristically. SSD-specific warnings are shown due to wear-leveling limitations beyond app control.",

    "Overwrite Patterns" to
            "The app uses cryptographically secure random data, zeros, ones, and alternating byte sequences (e.g., 0xAA/0x55) to defeat forensic recovery techniques.",

    "Secure Rename Before Deletion" to
            "Before deletion, filenames are randomized using a cryptographically secure PRNG to prevent post-mortem metadata correlation. Some file systems may limit this.",

    "Clipboard Clearing" to
            "If enabled in settings, the clipboard is securely cleared after deletion to prevent residual data leaks from copied filenames or URIs.",

    "Background & Foreground Modes" to
            "The app supports both background shredding via WorkManager and foreground mode with real-time progress. Critical operations stay foreground to prevent memory death.",

    "Symlink Protection" to
            "To avoid accidental system damage, symbolic links are detected and users are warned. Shredding is disabled for such files.",

    "Crash Resilience" to
            "Buffers are zeroed and flushed on cancel/error/exit to avoid sensitive residue in memory. Java's GC is explicitly triggered after cleanup.",

    "Open Design Invitation" to
            "Although not open-source, we invite audits and reproducible builds. Community testing and responsible disclosure of flaws is strongly encouraged.",

    "Performance Adaptive" to
            "Buffer size adapts to file size and device RAM constraints. Large files are streamed with rate-limited I/O to prevent UI or system throttling.",

    "No Analytics, No Ads" to
            "This app does not collect analytics, show ads, or use crash reporters. It is a fully offline, privacy-respecting utility with no monetization layer.",

    "Source of Truth" to
            "All actual behavior is dictated by the installed codebase. Documentation, marketing, or screenshots cannot override what the app does internally.",

    "Recovery Impossibility Disclaimer" to
            "No software can fully erase data on certain hardware (e.g., SSDs with overprovisioning). This tool reduces recoverability, but absolute erasure is not guaranteed.",

    "Data Lifecycle Control" to
            "You can pause, resume, or cancel shredding at any time. Cancelled operations are still followed by buffer and state cleanup to maintain privacy.",

    "Build Integrity" to
            "This app is signed and built using reproducible configurations. If the build is tampered with, the signature will not match the official one.",

    "Platform Limitations" to
            "Due to Android's scoped storage model, shredding access is limited to files selected via SAF or within granted sandbox scope.",

    "Update Policy" to
            "Updates may introduce new patterns, optimizations, or stricter validation. No automatic telemetry or remote disabling is implemented or planned.",

    "Fallback Handling" to
            "If shredding fails mid-process, the app leaves behind a partially-overwritten file and logs an error. Manual deletion may be necessary.",

    "Version" to
            "1.0.0 (Core: AES-256, Dual-Buffer, Verify Rate: 10%, Final Flush Enabled, Adaptive Write)"
)


@Composable
fun AboutScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Removed Spacer at the top for a more direct look,
        // and animated visibility/alpha modifiers.

        Text(
            text = "🔒",
            fontSize = 50.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Shred.it",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Check our Terms and Privacy Policy on our website : impom.github.io/shreditapp",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Dynamically generate InfoSections from the infoList
        infoList.forEach { (title, content) ->
            // Replaced AnimatedAboutCard with a direct Card usage
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp), // Reduced vertical padding to match original intent
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant, // Use surfaceVariant for card background
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                InfoSection(title = title, content = content)
            }
            Spacer(modifier = Modifier.height(8.dp)) // Standard small spacer between cards
        }

        Spacer(modifier = Modifier.height(16.dp)) // Standard spacer at the bottom
    }
}

// InfoSection remains mostly the same, now directly used within a Card
@Composable
fun InfoSection(title: String, content: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 20.dp) // Original padding maintained
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 22.sp
        )
    }
}