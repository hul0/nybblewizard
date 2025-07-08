package com.shred.it.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
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

// Sealed class for screen routes and metadata
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Shredder", Icons.Default.Delete)
    object About : Screen("about", "About", Icons.Default.Info)
    object FAQ : Screen("faq", "FAQ", Icons.AutoMirrored.Filled.List)
    object Support : Screen("support", "Support", Icons.Default.Favorite)
}

@Composable
fun MainAppScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val screens = listOf(Screen.Home, Screen.About, Screen.FAQ, Screen.Support)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                screens.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = {
                            Icon(screen.icon, contentDescription = screen.title)
                        },
                        label = {
                            Text(screen.title)
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
                        }
                        // By removing NavigationBarItemDefaults.colors, the item will use
                        // the default colors provided by the MaterialTheme for selected and unselected states.
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
                .fillMaxSize()
        ) {
            composable(Screen.Home.route) {
                FileShredderScreen()
            }
            composable(Screen.About.route) {
                AboutScreen()
            }
            composable(Screen.FAQ.route) {
                FAQScreen()
            }
            composable(Screen.Support.route) {
                SupportScreen(
                    onBackClick = { navController.navigateUp() },
                    onRateApp = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, "market://details?id=${context.packageName}".toUri())
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            val intent = Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=${context.packageName}".toUri())
                            context.startActivity(intent)
                        }
                    },
                    onSupportTier = { /* handle support tier */ },
                    onContactSupport = { contactType ->
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = "mailto:nmrupam@proton.me".toUri()
                            putExtra(Intent.EXTRA_SUBJECT, when (contactType) {
                                "bug_report" -> "Bug Report - Shred It App"
                                "feature_request" -> "Feature Request - Shred It App"
                                "general_support" -> "General Support - Shred It App"
                                "feedback" -> "Feedback - Shred It App"
                                else -> "Support - Shred It App"
                            })
                            putExtra(Intent.EXTRA_TEXT, when (contactType) {
                                "bug_report" -> "Please describe the bug you encountered:\n\n"
                                "feature_request" -> "Please describe the feature you'd like to see:\n\n"
                                "general_support" -> "How can we help you?\n\n"
                                "feedback" -> "We'd love to hear your thoughts:\n\n"
                                else -> ""
                            })
                        }
                        context.startActivity(Intent.createChooser(intent, "Contact Support"))
                    }
                )
            }
        }
    }
}