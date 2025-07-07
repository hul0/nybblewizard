package com.shred.it.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// The legally optimized infoList
val infoList = listOf(
    "How It Works" to
            "This app is engineered to render file recovery extremely difficult. It employs AES-256 encryption followed by multiple overwrite passes using industry-recognized secure data patterns. While these methods are aligned with data destruction best practices, due to the complexities of modern storage hardware, file systems, and forensic techniques, we cannot guarantee absolute, irreversible data irrecoverability in every conceivable scenario. The app aims for best-effort destruction.",

    "Privacy Commitment" to
            "All file shredding and related operations are performed exclusively locally on your device. This app is designed with a strict no-data-out policy: it does not access the internet, transmit any data, collect analytics, generate crash reports, include telemetry, or track user activity. Your digital privacy remains entirely on your device and under your control.",

    "100% Free. No Ads. No Strings." to
            "This application is provided completely free of charge. It contains no advertisements, no in-app purchases, no subscription models, and no hidden monetization layers. There are no financial costs or behavioral data collection associated with its use, now or in the future.",

    "Transparency & Security Posture" to
            "While the application's source code is not publicly open, its architecture and design principles prioritize user privacy and data security. We encourage cybersecurity researchers and independent testers to responsibly examine the app's behavior, report any potential vulnerabilities, and verify its operational claims. Our commitment is to trust through verifiable design.",

    "End User License Agreement (EULA)" to
            "By installing or using this application, you agree to a limited, non-exclusive, non-transferable, and non-commercial license to use it solely for your lawful, personal purposes. This End User License Agreement (EULA) constitutes the entire and primary agreement between you and the developers concerning the app's use and supersedes any prior or contemporaneous oral or written communications, marketing materials, or other representations.",

    "Eligibility & Capacity" to
            "To use this app, you must be at least 18 years of age and possess the full legal capacity to enter into a binding contract in your jurisdiction. By using this app, you affirm that you meet these eligibility requirements. If you do not meet these conditions, you must immediately cease all use and uninstall the app.",

    "Prohibited Uses" to
            "This app is strictly prohibited from being used for any unlawful purpose, including but not limited to the destruction of evidence, obstruction of justice, evasion of law enforcement, circumvention of data retention policies, or any activity related to criminal conduct. You bear sole and full responsibility for your actions and the consequences of using this app.",

    "No Law Enforcement or Government Affiliation" to
            "This application is developed independently and is neither affiliated with, endorsed by, nor intended for use by any law enforcement, military, intelligence, or governmental agency. It is designed exclusively for individual civilian use for personal data privacy enhancement.",

    "No Certifications or Official Approval" to
            "This app has not undergone formal certification by, nor received official approval from, any regulatory bodies or compliance frameworks, including but not limited to GDPR, HIPAA, FIPS, ISO, NIST, CISA, or NSA. It is not suitable for use in regulated environments, government operations, or situations requiring certified data sanitization.",

    "Intellectual Property & Licensing" to
            "All rights, title, and interest in and to the application, including its design, branding (names, logos, icons), user experience (UX) patterns, underlying logic, and all associated intellectual property, are exclusively owned by the original developers. You are prohibited from reverse engineering, decompiling, disassembling, modifying, copying, redistributing, reselling, sublicensing, repackaging, or creating derivative works from this app in whole or in part without explicit written permission.",

    "Export Control Compliance" to
            "This application utilizes strong cryptographic functions (AES-256) which may be subject to export, import, or re-export control laws and regulations in various jurisdictions, including but not limited to the United States and India. By using or downloading this app, you represent and warrant that you are not located in, under the control of, or a national or resident of any country to which the export, import, or use of encryption software is restricted or prohibited. You further agree to comply strictly with all applicable local, national, and international export and import control laws and regulations.",

    "Disclaimer of Results" to
            "While this app is developed based on secure deletion principles and best-effort destruction methods, we provide no guarantee or warranty that data shredded using this application is absolutely, permanently, and irreversibly irrecoverable in all scenarios. The effectiveness of data destruction can be influenced by various external factors, including but not limited to the type of storage hardware (e.g., SSDs, HDDs, flash memory), underlying file system behaviors, operating system caching, wear-leveling algorithms, and advanced forensic recovery techniques. It is your sole responsibility to independently verify the complete and secure removal of your files.",

    "Force Majeure" to
            "The developers shall not be held liable for any failure, delay, or interruption in the performance or availability of the app arising from causes beyond our reasonable control, including but not limited to acts of God, natural disasters, war, terrorism, civil unrest, governmental acts or restrictions, legal restrictions, app store policy changes or removals, pandemics, or widespread system failures.",

    "Limitation of Liability" to
            "TO THE MAXIMUM EXTENT PERMITTED BY APPLICABLE LAW, IN NO EVENT SHALL THE DEVELOPERS, THEIR AFFILIATES, OR THEIR RESPECTIVE LICENSORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, CONSEQUENTIAL, PUNITIVE, OR EXEMPLARY DAMAGES WHATSOEVER, INCLUDING BUT NOT LIMITED TO DAMAGES FOR LOSS OF PROFITS, DATA, GOODWILL, BUSINESS INTERRUPTION, OR ANY OTHER INTANGIBLE LOSSES, ARISING OUT OF OR IN CONNECTION WITH YOUR USE OR INABILITY TO USE THE APP, EVEN IF THE DEVELOPERS HAVE BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES. YOUR SOLE AND EXCLUSIVE REMEDY FOR ANY DISSATISFACTION WITH THE APP IS TO STOP USING IT AND UNINSTALL IT.",

    "Indemnification" to
            "You agree to defend, indemnify, and hold harmless the developers, their affiliates, officers, directors, employees, and agents from and against any and all claims, liabilities, damages, losses, costs, expenses (including reasonable legal fees), and government fines arising out of or in any way connected with your access to or use of the app, your violation of these terms, or your infringement of any intellectual property or other right of any person or entity.",

    "No Warranty Disclaimer" to
            "THIS APP IS PROVIDED \"AS IS\" AND \"AS AVAILABLE,\" WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS, IMPLIED, STATUTORY, OR OTHERWISE. THE DEVELOPERS EXPRESSLY DISCLAIM ALL WARRANTIES, INCLUDING, BUT NOT LIMITED TO, IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, ACCURACY, RELIABILITY, PERFORMANCE, SECURITY OUTCOMES, OR NON-INFRINGEMENT. THE ENTIRE RISK AS TO THE QUALITY AND PERFORMANCE OF THE APP IS WITH YOU. NO ORAL OR WRITTEN INFORMATION OR ADVICE GIVEN BY THE DEVELOPERS SHALL CREATE A WARRANTY.",

    "Dispute Resolution & Arbitration" to
            "TO THE FULLEST EXTENT PERMITTED BY APPLICABLE LAW, ANY CLAIM, DISPUTE, OR CONTROVERSY ARISING OUT OF OR RELATING TO THIS AGREEMENT OR THE APP SHALL BE FINALLY RESOLVED BY BINDING ARBITRATION ADMINISTERED BY THE AMERICAN ARBITRATION ASSOCIATION (AAA) UNDER ITS CONSUMER ARBITRATION RULES. THE ARBITRATION SHALL TAKE PLACE IN THE STATE OF DELAWARE, USA. YOU HEREBY WAIVE ANY RIGHT TO A TRIAL BY JURY OR TO PARTICIPATE AS A PLAINTIFF OR CLASS MEMBER IN ANY CLASS ACTION OR REPRESENTATIVE PROCEEDING. NOTWITHSTANDING THE FOREGOING, USERS RESIDING OUTSIDE THE UNITED STATES MAY RETAIN CERTAIN STATUTORY RIGHTS UNDER THEIR LOCAL CONSUMER PROTECTION LAWS WHICH MAY NOT BE WAIVABLE.",

    "Severability Clause" to
            "If any provision or part of a provision of these terms is found by a court of competent jurisdiction or arbitrator to be invalid, illegal, or unenforceable, that provision or part thereof shall be deemed severed from these terms, and the remaining provisions or parts thereof shall continue in full force and effect as if the invalid or unenforceable provision had never been part of the agreement.",

    "Governing Law & Jurisdiction" to
            "These terms shall be governed by and construed in accordance with the laws of the State of Delaware, United States, without regard to its conflict of laws principles. However, nothing in this clause shall limit any mandatory consumer protection rights that you may have under the laws of your country of residence that cannot be waived by agreement.",

    "Termination of Access" to
            "We reserve the right to terminate your license and access to this app at any time, without prior notice, if we determine, in our sole discretion, that you have violated any of these terms or engaged in any conduct we deem harmful to the app or other users. Upon termination, you must immediately cease all use of the app and uninstall it from all your devices.",

    "Entire Agreement" to
            "This document, encompassing all the above clauses, constitutes the entire and sole agreement between you and the developers concerning the app and supersedes all prior or contemporaneous understandings, agreements, proposals, or communications, whether oral, written, or implied. No external statements, marketing claims, or representations made outside this document shall override these terms unless formally amended in writing by the developers.",

    "Version" to
           "1.0.0 "
)

@Composable
fun AboutScreen() {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(50) // Slight delay for the animation to be noticeable
        visible = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Flat background
            .padding(16.dp) // Consistent padding
            .verticalScroll(rememberScrollState())
            .alpha(if (visible) 1f else 0f), // Apply alpha for fade-in
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(30.dp))

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(delayMillis = 100)) + slideInVertically(initialOffsetY = { it / 2 }, animationSpec = androidx.compose.animation.core.tween(delayMillis = 100))
        ) {
            Text(
                text = "🔒",
                fontSize = 50.sp, // Slightly smaller emoji for a cleaner look
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(delayMillis = 200)) + slideInVertically(initialOffsetY = { it / 2 }, animationSpec = androidx.compose.animation.core.tween(delayMillis = 200))
        ) {
            Text(
                text = "Shred It",
                style = MaterialTheme.typography.headlineLarge, // Use MaterialTheme typography
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(delayMillis = 300)) + slideInVertically(initialOffsetY = { it / 2 }, animationSpec = androidx.compose.animation.core.tween(delayMillis = 300))
        ) {
            Text(
                text = "Secure File Shredder",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }

        // Dynamically generate InfoSections from the infoList
        infoList.forEachIndexed { index, (title, content) ->
            // Stagger the animation delay for each card
            val delayMillis = 400 + (index * 100) // Start at 400ms, add 100ms for each subsequent card
            AnimatedAboutCard(delay = delayMillis, visible = visible) {
                InfoSection(title = title, content = content)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun AnimatedAboutCard(delay: Int, visible: Boolean, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(delayMillis = delay)) +
                slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = androidx.compose.animation.core.tween(delayMillis = delay)),
        exit = fadeOut()
    ) {
        FlatCard { // Use the new FlatCard
            content()
        }
    }
}


@Composable
fun InfoSection(title: String, content: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 20.dp) // Adjusted padding
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

// Reusable FlatCard (can be moved to a common ui components file)
@Composable
fun FlatCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant, // Flatter color
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp), // Less rounded for a flatter feel
        color = backgroundColor,
        tonalElevation = 0.dp, // No shadow for flat look
        // border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)) // Optional: subtle border
    ) {
        content()
    }
}
