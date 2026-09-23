package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ArcadeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    gradientColors: List<Color> = listOf(Color(0xFFFF007A), Color(0xFF7928CA)),
    testTag: String = "arcade_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.94f else 1.0f,
        label = "btn_scale"
    )

    val currentBrush = if (enabled) {
        Brush.horizontalGradient(gradientColors)
    } else {
        Brush.horizontalGradient(listOf(Color(0xFF424242), Color(0xFF2E2E2E)))
    }

    val borderColor = if (enabled) Color.White.copy(alpha = 0.5f) else Color.Gray.copy(alpha = 0.2f)
    val textColor = if (enabled) Color.White else Color.LightGray.copy(alpha = 0.5f)

    Box(
        modifier = modifier
            .testTag(testTag)
            .scale(scale)
            .fillMaxWidth()
            .height(58.dp)
            .defaultMinSize(minHeight = 48.dp)
            .shadow(if (enabled) 8.dp else 0.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(currentBrush)
            .border(2.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(
                text = text.uppercase(),
                color = textColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )
        }
    }
}

@Composable
fun ArcadeIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF1E1035),
    borderColor: Color = Color(0xFF00E5FF),
    iconTint: Color = Color.White,
    testTag: String = "arcade_icon_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        label = "icon_btn_scale"
    )

    Box(
        modifier = modifier
            .testTag(testTag)
            .scale(scale)
            .size(50.dp)
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(2.dp, borderColor, CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(26.dp)
        )
    }
}
