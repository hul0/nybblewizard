package com.shred.it.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Favorite

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.shred.it.ui.screens.AboutScreen
import com.shred.it.ui.screens.FAQScreen
import com.shred.it.ui.screens.FileShredderScreen
import com.shred.it.ui.screens.SupportScreen
import com.shred.it.ui.theme.ShredItTheme
import androidx.core.net.toUri

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShredItTheme {
                MainAppScreen()
            }
        }
    }
}

// Updated Screen sealed class with Support tab
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Shredder", Icons.Default.Delete)
    object About : Screen("about", "About", Icons.Default.Info)
    object FAQ : Screen("faq", "FAQ", Icons.AutoMirrored.Filled.List)
    object Support : Screen("support", "Support", Icons.Default.Favorite)
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainAppScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Updated screens list to include Support
    val screens = listOf(Screen.Home, Screen.About, Screen.FAQ, Screen.Support)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background, // Flat background
        bottomBar = {
            // Flat Navigation Bar - remove Surface shadow
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface, // Or surfaceVariant for subtle difference
                modifier = Modifier.height(70.dp), // Slightly adjusted height
                tonalElevation = 0.dp // Key for flat look
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                screens.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = {
                            Icon(
                                screen.icon,
                                contentDescription = screen.title,
                                modifier = Modifier.size(24.dp) // Slightly larger icon
                            )
                        },
                        label = {
                            Text(
                                screen.title,
                                fontSize = 12.sp, // Slightly smaller label
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        selected = selected,
                        onClick = {
                            if (navController.currentDestination?.route != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f) // More subtle indicator
                        ),
                        alwaysShowLabel = true // Ensure labels are always visible
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            enterTransition = { EnterTransition.None }, // Disable default NavHost animations
            exitTransition = { ExitTransition.None }
        ) {
            composable(Screen.Home.route) {
                AnimatedScreen { FileShredderScreen() }
            }
            composable(Screen.About.route) {
                AnimatedScreen { AboutScreen() }
            }
            composable(Screen.FAQ.route) {
                AnimatedScreen { FAQScreen() }
            }
            composable(Screen.Support.route) {
                AnimatedScreen {
                    SupportScreen(
                        onBackClick = {
                            // Navigate back to home or previous screen
                            navController.navigateUp()
                        },
                        onRateApp = {
                            // Open Play Store for rating
                            try {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = "market://details?id=${context.packageName}".toUri()
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                // Fallback to browser if Play Store is not available
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data =
                                        "https://play.google.com/store/apps/details?id=${context.packageName}".toUri()
                                }
                                context.startActivity(intent)
                            }
                        },
                        onSupportTier = { tierName ->
                            // Handle support tier selection
                            // You can implement payment processing here
                            // For now, let's show a simple action
                            when (tierName) {
                                "Coffee Supporter" -> {
                                    // Handle coffee tier payment
                                    // Example: Launch billing flow for $2.99
                                }
                                "Premium Supporter" -> {
                                    // Handle premium tier payment
                                    // Example: Launch billing flow for $9.99
                                }
                                "Ultimate Supporter" -> {
                                    // Handle ultimate tier payment
                                    // Example: Launch billing flow for $24.99
                                }
                            }
                        },
                        onContactSupport = { contactType ->
                            // Handle contact support actions
                            when (contactType) {
                                "bug_report" -> {
                                    // Open email client for bug report
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = "mailto:support@shred-it.com".toUri()
                                        putExtra(Intent.EXTRA_SUBJECT, "Bug Report - Shred It App")
                                        putExtra(Intent.EXTRA_TEXT, "Please describe the bug you encountered:\n\n")
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Send Bug Report"))
                                }
                                "feature_request" -> {
                                    // Open email client for feature request
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = "mailto:support@shred-it.com".toUri()
                                        putExtra(Intent.EXTRA_SUBJECT, "Feature Request - Shred It App")
                                        putExtra(Intent.EXTRA_TEXT, "Please describe the feature you'd like to see:\n\n")
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Send Feature Request"))
                                }
                                "general_support" -> {
                                    // Open email client for general support
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = "mailto:support@shred-it.com".toUri()
                                        putExtra(Intent.EXTRA_SUBJECT, "General Support - Shred It App")
                                        putExtra(Intent.EXTRA_TEXT, "How can we help you?\n\n")
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Contact Support"))
                                }
                                "feedback" -> {
                                    // Open email client for feedback
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = "mailto:support@shred-it.com".toUri()
                                        putExtra(Intent.EXTRA_SUBJECT, "Feedback - Shred It App")
                                        putExtra(Intent.EXTRA_TEXT, "We'd love to hear your thoughts:\n\n")
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Send Feedback"))
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedScreen(content: @Composable () -> Unit) {
    // Using AnimatedContent for screen transitions might be complex with NavHost's own transitions.
    // A simpler approach is to animate visibility of content within each screen,
    // or use NavHost's built-in transition capabilities if you configure them.
    // For now, let's make each screen fade in.
    // If you want more complex NavHost transitions, research `enterTransition` and `exitTransition`
    // parameters of the `composable` function in NavHost.

    Box(modifier = Modifier.fillMaxSize()) { // Ensure content fills the space
        // Simple Fade-in for the content of each screen
        AnimatedVisibility(
            visible = true, // Content is always visible once composed
            enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(durationMillis = 300)) + scaleIn(initialScale = 0.98f, animationSpec = androidx.compose.animation.core.tween(durationMillis = 300)),
            exit = fadeOut(animationSpec = androidx.compose.animation.core.tween(durationMillis = 150))
        ) {
            content()
        }
    }
}