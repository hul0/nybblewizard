package com.republicwing.bufferwing.app.reusable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.delay

enum class ToastType {
    SUCCESS,
    WARNING,
    ERROR,
    INFO
}

data class ToastData(
    val heading: String,
    val description: String,
    val duration: Long = 3000L,
    val type: ToastType = ToastType.INFO
)

@Composable
fun CustomToast(
    toastData: ToastData?,
    onDismiss: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(toastData) {
        if (toastData != null) {
            isVisible = true
            delay(toastData.duration)
            isVisible = false
            delay(300) // Wait for exit animation
            onDismiss()
        }
    }
    
    toastData?.let { data ->
        Popup(
            alignment = Alignment.TopCenter,
            properties = PopupProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            ),
            onDismissRequest = {
                isVisible = false
                onDismiss()
            }
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(
                    animationSpec = tween(300)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { -it },
                    animationSpec = tween(300)
                ) + fadeOut(
                    animationSpec = tween(300)
                )
            ) {
                ToastContent(
                    data = data,
                    onClose = {
                        isVisible = false
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
private fun ToastContent(
    data: ToastData,
    onClose: () -> Unit
) {
    val (backgroundColor, contentColor, icon) = when (data.type) {
        ToastType.SUCCESS -> Triple(
            Color(0xFF4CAF50),
            Color.White,
            Icons.Default.CheckCircle
        )
        ToastType.WARNING -> Triple(
            Color(0xFFFF9800),
            Color.White,
            Icons.Default.Warning
        )
        ToastType.ERROR -> Triple(
            Color(0xFFF44336),
            Color.White,
            Icons.Default.Person
        )
        ToastType.INFO -> Triple(
            Color(0xFF2196F3),
            Color.White,
            Icons.Default.Info
        )
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = contentColor
                )
                
                // Content
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = data.heading,
                        color = contentColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 20.sp
                    )
                    
                    Text(
                        text = data.description,
                        color = contentColor.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        lineHeight = 18.sp
                    )
                }
            }
            
            // Close button
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(32.dp)
                    .offset(x = 8.dp, y = (-8).dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = contentColor.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// Toast Manager - Use this to show toasts from your screens
class ToastManager {
    private val _toastData = mutableStateOf<ToastData?>(null)
    val toastData: State<ToastData?> = _toastData
    
    fun showToast(
        heading: String,
        description: String,
        duration: Long = 3000L,
        type: ToastType = ToastType.INFO
    ) {
        _toastData.value = ToastData(
            heading = heading,
            description = description,
            duration = duration,
            type = type
        )
    }
    
    fun hideToast() {
        _toastData.value = null
    }
}

// Extension function for easy toast creation
fun ToastManager.showSuccessToast(heading: String, description: String, duration: Long = 3000L) {
    showToast(heading, description, duration, ToastType.SUCCESS)
}

fun ToastManager.showWarningToast(heading: String, description: String, duration: Long = 3000L) {
    showToast(heading, description, duration, ToastType.WARNING)
}

fun ToastManager.showErrorToast(heading: String, description: String, duration: Long = 3000L) {
    showToast(heading, description, duration, ToastType.ERROR)
}

fun ToastManager.showInfoToast(heading: String, description: String, duration: Long = 3000L) {
    showToast(heading, description, duration, ToastType.INFO)
}