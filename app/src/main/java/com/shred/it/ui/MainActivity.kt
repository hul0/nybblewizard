package com.shred.it.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.shred.it.ui.reusable.Navbar.CustomBottomNavBar// Assuming this is the path to your CustomBottomNavBar
import com.shred.it.ui.reusable.Navbar.NavItem // Assuming this is the path to your NavItem data class
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

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Shredder", Icons.Filled.CleaningServices)
    object About : Screen("about", "About", Icons.Filled.Policy)
    object FAQ : Screen("faq", "FAQ", Icons.AutoMirrored.Filled.HelpOutline)
    object Support : Screen("support", "Support", Icons.Filled.SupportAgent)
}

@Composable
fun MainAppScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val screens = listOf(Screen.Home, Screen.About, Screen.FAQ, Screen.Support)

    val navItems = screens.map { screen ->
        NavItem(
            icon = screen.icon,
            name = screen.title,
            route = screen.route
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            CustomBottomNavBar(navController = navController, items = navItems)
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
                FileShredderScreen(innerPadding)
            }
            composable(Screen.About.route) {
                AboutScreen()
            }
            composable(Screen.FAQ.route) {
                FAQScreen(innerPadding = innerPadding)
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