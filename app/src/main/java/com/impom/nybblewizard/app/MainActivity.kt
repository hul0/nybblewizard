package com.impom.nybblewizard.app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.SwapHoriz // Import for ConversionScreen icon
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.exyte.animatednavbar.AnimatedNavigationBar
import com.impom.nybblewizard.app.reusable.SettingsDialog
import com.impom.nybblewizard.app.reusable.TopNavBar
import com.impom.nybblewizard.app.screens.AboutScreen
import com.impom.nybblewizard.app.screens.ConversionScreen // Import ConversionScreen
import com.impom.nybblewizard.app.screens.FAQScreen
import com.impom.nybblewizard.app.screens.FileShredderScreen
import com.impom.nybblewizard.app.screens.SupportScreen
import com.impom.nybblewizard.app.theme.ShredItTheme
import com.impom.nybblewizard.app.theme.PaletteManager
import com.impom.nybblewizard.app.viewmodel.FileShredderViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShredItTheme { // ShredItTheme already uses PaletteManager internally
                MainAppScreen()
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Shredder", Icons.Filled.CleaningServices)
    object Converter : Screen("converter", "Converter", Icons.Filled.SwapHoriz) // New Converter Screen
    object About : Screen("about", "About", Icons.Filled.Policy)
    object FAQ : Screen("faq", "FAQ", Icons.AutoMirrored.Filled.HelpOutline)
    object Support : Screen("support", "Support", Icons.Filled.SupportAgent)
}

@SuppressLint("InvalidColorHexValue")
@Preview
@Composable
fun MainAppScreen(
    fileShredderViewModel: FileShredderViewModel = viewModel() // Inject or get ViewModel
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    // Add Converter to the list of screens
    val screens = listOf(Screen.Home, Screen.Converter, Screen.About, Screen.FAQ, Screen.Support)

    val settings by fileShredderViewModel.settings.collectAsState() // Collect settings state
    val snackbarHostState = remember { SnackbarHostState() } // Create SnackbarHostState
    val scope = rememberCoroutineScope() // Create CoroutineScope

    // Remember the selected index for AnimatedNavigationBar
    var selectedIndex by remember { mutableStateOf(0) }
    // State to control the visibility of the settings dialog
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Get current colors from MaterialTheme
    val colors = MaterialTheme.colorScheme

    // Dynamic colors based on selection from the current theme's palette
    val screenSpecificPrimaryColors = listOf(
        PaletteManager.currentPalette.getColors(PaletteManager.isDarkMode).primary, // For Home
        PaletteManager.currentPalette.getColors(PaletteManager.isDarkMode).tertiary, // For Converter (using tertiary as an example)
        PaletteManager.currentPalette.getColors(PaletteManager.isDarkMode).secondary, // For About (using secondary as an example)
        PaletteManager.currentPalette.getColors(PaletteManager.isDarkMode).tertiary, // For FAQ (using tertiary as an example)
        PaletteManager.currentPalette.getColors(PaletteManager.isDarkMode).error // For Support (using error as an example, choose appropriate)
    )

    val currentScreenPrimaryColor = screenSpecificPrimaryColors[selectedIndex]

    val gradientColors = if (PaletteManager.isDarkMode) {
        listOf(
            colors.surface.copy(alpha = 0.8f),
            colors.background.copy(alpha = 0.6f),
            currentScreenPrimaryColor.copy(alpha = 0.5f)
        )
    } else {
        listOf(
            colors.primaryContainer.copy(alpha = 0.8f),
            colors.secondaryContainer.copy(alpha = 0.6f),
            currentScreenPrimaryColor.copy(alpha = 0.5f)
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = gradientColors,
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            ),
        topBar = {
            TopNavBar(onSettingsClick = { showSettingsDialog = true })
        },
        bottomBar = {
            AnimatedNavigationBar(
                selectedIndex = selectedIndex,
                barColor = if (PaletteManager.isDarkMode) {
                    colors.surfaceVariant.copy(alpha = 0.9f)
                } else {
                    colors.surfaceVariant.copy(alpha = 0.95f)
                },
                ballColor = colors.secondary,
                modifier = Modifier.background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            colors.primary.copy(alpha = 0.3f),
                            colors.secondary.copy(alpha = 0.3f),
                            colors.tertiary.copy(alpha = 0.3f)
                        )
                    )
                )
            ) {
                // Home Button
                IconButton(
                    onClick = {
                        selectedIndex = 0
                        navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (selectedIndex == 0) {
                                Brush.radialGradient(
                                    colors = listOf(
                                        screenSpecificPrimaryColors[0].copy(alpha = 0.3f),
                                        screenSpecificPrimaryColors[0].copy(alpha = 0.1f)
                                    )
                                )
                            } else {
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Transparent
                                    )
                                )
                            }
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.CleaningServices,
                        contentDescription = "Shredder",
                        tint = if (selectedIndex == 0) screenSpecificPrimaryColors[0] else colors.onSurface
                    )
                }

                // Converter Button (New)
                IconButton(
                    onClick = {
                        selectedIndex = 1
                        navController.navigate(Screen.Converter.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (selectedIndex == 1) {
                                Brush.radialGradient(
                                    colors = listOf(
                                        screenSpecificPrimaryColors[1].copy(alpha = 0.3f),
                                        screenSpecificPrimaryColors[1].copy(alpha = 0.1f)
                                    )
                                )
                            } else {
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Transparent
                                    )
                                )
                            }
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.SwapHoriz,
                        contentDescription = "Converter",
                        tint = if (selectedIndex == 1) screenSpecificPrimaryColors[1] else colors.onSurface
                    )
                }

                // About Button
                IconButton(
                    onClick = {
                        selectedIndex = 2 // Adjusted index due to new Converter screen
                        navController.navigate(Screen.About.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (selectedIndex == 2) {
                                Brush.radialGradient(
                                    colors = listOf(
                                        screenSpecificPrimaryColors[2].copy(alpha = 0.3f),
                                        screenSpecificPrimaryColors[2].copy(alpha = 0.1f)
                                    )
                                )
                            } else {
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Transparent
                                    )
                                )
                            }
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Policy,
                        contentDescription = "About",
                        tint = if (selectedIndex == 2) screenSpecificPrimaryColors[2] else colors.onSurface
                    )
                }

                // FAQ Button
                IconButton(
                    onClick = {
                        selectedIndex = 3 // Adjusted index
                        navController.navigate(Screen.FAQ.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (selectedIndex == 3) {
                                Brush.radialGradient(
                                    colors = listOf(
                                        screenSpecificPrimaryColors[3].copy(alpha = 0.3f),
                                        screenSpecificPrimaryColors[3].copy(alpha = 0.1f)
                                    )
                                )
                            } else {
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Transparent
                                    )
                                )
                            }
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                        contentDescription = "FAQ",
                        tint = if (selectedIndex == 3) screenSpecificPrimaryColors[3] else colors.onSurface
                    )
                }

                // Support Button
                IconButton(
                    onClick = {
                        selectedIndex = 4 // Adjusted index
                        navController.navigate(Screen.Support.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (selectedIndex == 4) {
                                Brush.radialGradient(
                                    colors = listOf(
                                        screenSpecificPrimaryColors[4].copy(alpha = 0.3f),
                                        screenSpecificPrimaryColors[4].copy(alpha = 0.1f)
                                    )
                                )
                            } else {
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Transparent
                                    )
                                )
                            }
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.SupportAgent,
                        contentDescription = "Support",
                        tint = if (selectedIndex == 4) screenSpecificPrimaryColors[4] else colors.onSurface
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding) // This padding is essential and correctly placed.
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colors.background.copy(alpha = 0.8f),
                            currentScreenPrimaryColor.copy(alpha = 0.1f),
                            colors.surface.copy(alpha = 0.9f)
                        )
                    )
                )
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.Home.route) {
                    // **FIX**: Do NOT pass innerPadding here anymore.
                    FileShredderScreen(fileShredderViewModel)
                }
                composable(Screen.Converter.route) {
                    ConversionScreen(
                        onBackClick = { navController.navigateUp() }
                    )
                }

                composable(Screen.About.route) {
                    AboutScreen()
                }
                composable(Screen.FAQ.route) {
                    // **FIX**: Do NOT pass innerPadding here either.
                    FAQScreen() // You will need to update FAQScreen to remove the innerPadding parameter as well.
                }

                composable(Screen.Support.route) {
                    SupportScreen(
                        onBackClick = { navController.navigateUp() },
                        onRateApp = {
                            try {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    "market://details?id=${context.packageName}".toUri()
                                )
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    "https://play.google.com/store/apps/details?id=${context.packageName}".toUri()
                                )
                                context.startActivity(intent)
                            }
                        },
                        onSupportTier = { /* handle support tier */ },
                        onContactSupport = { contactType ->
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = "mailto:nmrupam@proton.me".toUri()
                                putExtra(
                                    Intent.EXTRA_SUBJECT, when (contactType) {
                                        "bug_report" -> "🐛 Bug Report - Shred It App"
                                        "feature_request" -> "✨ Feature Request - Shred It App"
                                        "general_support" -> "🆘 General Support - Shred It App"
                                        "feedback" -> "💬 Feedback - Shred It App"
                                        else -> "📧 Support - Shred It App"
                                    }
                                )
                                putExtra(
                                    Intent.EXTRA_TEXT, when (contactType) {
                                        "bug_report" -> "🔍 Please describe the bug you encountered:\n\n" +
                                                "📱 Device: \n" +
                                                "🎯 Steps to reproduce:\n" +
                                                "❌ Expected behavior:\n" +
                                                "⚠️ Actual behavior:\n\n"

                                        "feature_request" -> "💡 Please describe the feature you'd like to see:\n\n" +
                                                "🎯 Feature description:\n" +
                                                "📊 Why would this be useful:\n" +
                                                "🔧 How should it work:\n\n"

                                        "general_support" -> "🤝 How can we help you?\n\n" +
                                                "❓ Your question:\n" +
                                                "📋 Additional context:\n\n"

                                        "feedback" -> "🌟 We'd love to hear your thoughts:\n\n" +
                                                "👍 What you like:\n" +
                                                "👎 What could be improved:\n" +
                                                "💡 Suggestions:\n\n"

                                        else -> "📝 How can we assist you?\n\n"
                                    }
                                )
                            }
                            context.startActivity(Intent.createChooser(intent, "Contact Support"))
                        }
                    )
                }
            }
        }


        // Settings Dialog controlled by MainActivity
        if (showSettingsDialog) {
            SettingsDialog(
                settings = settings,
                onUpdateRounds = fileShredderViewModel::updateRounds,
                onToggleRename = fileShredderViewModel::toggleRename,
                onToggleVerify = fileShredderViewModel::toggleVerify,
                snackbarHostState = snackbarHostState, // Pass the snackbarHostState
                onDismiss = { showSettingsDialog = false }
            )
        }
    }
}
