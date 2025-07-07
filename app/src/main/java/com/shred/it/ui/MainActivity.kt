package com.shred.it.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
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
        //supportActionBar?.hide()
        enableEdgeToEdge()
        setContent {
            ShredItTheme {
                MainAppScreen()
            }
        }
    }
}

// Bottom tabs only, no header bar
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
    val screens = listOf(Screen.Home, Screen.About, Screen.FAQ, Screen.Support)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.height(70.dp),
                tonalElevation = 0.dp
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
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                screen.title,
                                fontSize = 12.sp,
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
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                        ),
                        alwaysShowLabel = true
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
                        onBackClick = { navController.navigateUp() },
                        onRateApp = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = "market://details?id=${context.packageName}".toUri()
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = "https://play.google.com/store/apps/details?id=${context.packageName}".toUri()
                                }
                                context.startActivity(intent)
                            }
                        },
                        onSupportTier = { /* handle support tier */ },
                        onContactSupport = { contactType ->
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = "mailto:support@shred-it.com".toUri()
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
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedScreen(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(320)) +
                    scaleIn(initialScale = 0.97f, animationSpec = androidx.compose.animation.core.tween(320)),
            exit = fadeOut(animationSpec = androidx.compose.animation.core.tween(180))
        ) {
            content()
        }
    }
}