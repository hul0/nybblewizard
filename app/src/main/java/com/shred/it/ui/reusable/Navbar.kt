package com.shred.it.ui.reusable.Navbar

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState



class CurvedNavBarShape(
    private val currentIndicatorOffset: Float,
    private val curveWidthDp: Dp = 65.dp, // Exposed for easier customization from the call site
    private val curveDepthDp: Dp = 45.dp  // Exposed: Control the depth independently
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Generic(Path().apply {
            val cornerRadius = with(density) { 14.dp.toPx() } // Or your preferred corner radius
            val curveWidthPx = with(density) { curveWidthDp.toPx() }
            val curveDepthPx = with(density) { curveDepthDp.toPx() }

            // The horizontal radius for the curve's bounding box
            val curveHorizontalRadiusPx = curveWidthPx / 2f

            moveTo(cornerRadius, 0f)

            // Line before the curve
            lineTo(currentIndicatorOffset - curveHorizontalRadiusPx, 0f)

            // Draw the elliptical arc for the dip
            arcTo(
                rect = Rect(
                    left = currentIndicatorOffset - curveHorizontalRadiusPx,
                    // To make the ellipse dip downwards by curveDepthPx,
                    // its bounding box's top needs to be -curveDepthPx if we consider
                    // the center of the top edge of the ellipse to be at y=0.
                    // However, arcTo draws a segment of the ellipse.
                    // For a 180-degree sweep to form the bottom half of an ellipse:
                    top = 0f - curveDepthPx, // Top of the bounding ellipse for the dip
                    right = currentIndicatorOffset + curveHorizontalRadiusPx,
                    bottom = 0f + curveDepthPx // Bottom of the bounding ellipse for the dip
                ),
                startAngleDegrees = 180f, // Start from the left-middle of the ellipse
                sweepAngleDegrees = -180f, // Sweep downwards to form the bottom half
                forceMoveTo = false
            )

            // Line after the curve
            lineTo(size.width - cornerRadius, 0f)

            // --- Rest of the rounded rectangle corners (same as before) ---
            // Top-right arc
            arcTo(
                rect = Rect(
                    size.width - cornerRadius * 2, 0f,
                    size.width, cornerRadius * 2
                ),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            // Right line
            lineTo(size.width, size.height - cornerRadius)
            // Bottom-right arc
            arcTo(
                rect = Rect(
                    size.width - cornerRadius * 2, size.height - cornerRadius * 2,
                    size.width, size.height
                ),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            // Bottom line
            lineTo(cornerRadius, size.height)
            // Bottom-left arc
            arcTo(
                rect = Rect(
                    0f, size.height - cornerRadius * 2,
                    cornerRadius * 2, size.height
                ),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            // Left line
            lineTo(0f, cornerRadius)
            // Top-left arc
            arcTo(
                rect = Rect(
                    0f, 0f,
                    cornerRadius * 2, cornerRadius * 2
                ),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            close()
        })
    }
}
@Composable
fun CustomBottomNavBar(
    navController: NavController,
    items: List<NavItem>,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF212121),
    contentColor: Color = Color.White,
    selectedIndicatorColor: Color = Color(0xFFE55050)
) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    var selectedItem by remember { mutableStateOf(currentRoute ?: items.first().route) }

    LaunchedEffect(currentRoute) {
        currentRoute?.let {
            if (items.any { item -> item.route == it }) {
                selectedItem = it
            }
        }
    }

    var rowWidthPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current

    val itemWidthDp = if (items.isNotEmpty() && rowWidthPx > 0) {
        with(density) { (rowWidthPx / items.size).toDp() }
    } else {
        0.dp
    }

    val selectedIndex = items.indexOfFirst { it.route == selectedItem }

    val indicatorCenterOffsetPx by animateFloatAsState(
        targetValue = if (selectedIndex != -1) {
            with(density) { (itemWidthDp * selectedIndex + itemWidthDp / 2).toPx() }
        } else {
            0f
        },
        animationSpec = tween(durationMillis = 100), label = "indicatorCenterOffsetAnimation"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(10.dp),
        color = Color.Transparent,
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor, CurvedNavBarShape(indicatorCenterOffsetPx))
                .onGloballyPositioned { coordinates ->
                    rowWidthPx = coordinates.size.width
                }
        ) {
            if (itemWidthDp > 0.dp) {
                Box(
                    modifier = Modifier
                        .offset(
                            x = with(density) { indicatorCenterOffsetPx.toDp() } - (60.dp / 2),
                            y = (-20).dp
                        )
                        .size(60.dp)
                        .background(selectedIndicatorColor, RoundedCornerShape(50))
                        .align(Alignment.TopStart)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = selectedItem == item.route
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                if (selectedItem != item.route) {
                                    selectedItem = item.route
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        restoreState = true
                                        launchSingleTop = true
                                    }
                                }
                            }
                            .padding(vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.name,
                            tint = if (isSelected) contentColor else contentColor.copy(alpha = 0.6f),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.name,
                            color = if (isSelected) contentColor else contentColor.copy(alpha = 0.6f),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
data class NavItem(
    val icon: ImageVector,
    val name: String,
    val route: String,
    val color: Color = Color(0x00FFFFFF)
)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navItems = listOf(
        NavItem(Icons.Default.Home, "Home", "home_screen"),
        NavItem(Icons.Default.Refresh, "History", "history_screen"),
        NavItem(Icons.Default.Schedule, "Schedule", "schedule_screen"),
        NavItem(Icons.Default.Notifications, "Alerts", "alerts_screen")
    )

    Scaffold(
        bottomBar = {
            CustomBottomNavBar(navController = navController, items = navItems)
        }
    ) { paddingValues ->
        NavHost(navController = navController, startDestination = "home_screen", modifier = Modifier.padding(paddingValues)) {
            composable("home_screen") { HomeScreen() }
            composable("history_screen") { HistoryScreen() }
            composable("schedule_screen") { ScheduleScreen() }
            composable("alerts_screen") { AlertsScreen() }
        }
    }
}

@Composable
fun HomeScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Home Screen", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun HistoryScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("History Screen", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun ScheduleScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Schedule Screen", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun AlertsScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Alerts Screen", style = MaterialTheme.typography.headlineMedium)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCustomBottomNavBar() {
    MainScreen()
}
