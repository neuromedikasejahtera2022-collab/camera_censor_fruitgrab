package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.models.GestureState

/**
 * Shared arcade hand cursor overlay.
 * Follows normalized hand coordinates and visually transforms during FIST (click).
 */
@Composable
fun SharedHandCursor(
    gestureState: GestureState,
    modifier: Modifier = Modifier
) {
    if (!gestureState.isHandDetected) return

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        val cursorX = screenWidth * gestureState.cursorX
        val cursorY = screenHeight * gestureState.cursorY

        val scale by animateFloatAsState(
            targetValue = if (gestureState.isFist) 0.85f else 1.15f,
            animationSpec = tween(durationMillis = 150),
            label = "cursor_scale"
        )

        val cursorColor = if (gestureState.isFist) Color(0xFFFF5252) else Color(0xFF00E5FF)

        Box(
            modifier = Modifier
                .offset(x = cursorX - 24.dp, y = cursorY - 24.dp)
                .size(48.dp)
                .scale(scale),
            contentAlignment = Alignment.Center
        ) {
            // Pulsing background ring
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(cursorColor.copy(alpha = 0.25f), CircleShape)
                    .border(2.5.dp, cursorColor, CircleShape)
            )

            // Center icon
            Icon(
                imageVector = if (gestureState.isFist) Icons.Default.TouchApp else Icons.Default.PanTool,
                contentDescription = if (gestureState.isFist) "Fist Click" else "Open Palm Cursor",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
