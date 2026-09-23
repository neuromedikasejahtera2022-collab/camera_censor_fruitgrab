package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.FloatingScore
import com.example.models.Fruit
import com.example.models.Player
import com.example.models.PlayerId

@Composable
fun FruitItem(
    fruit: Fruit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "fruit_anim")
    val goldenPulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "golden_pulse"
    )

    Box(
        modifier = modifier
            .size(fruit.sizeDp.dp)
            .rotate(fruit.rotationAngle),
        contentAlignment = Alignment.Center
    ) {
        if (fruit.type.isGolden) {
            // Radiant golden aura
            Box(
                modifier = Modifier
                    .size((fruit.sizeDp + 18f).dp)
                    .scale(goldenPulse)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFFFFEE58).copy(alpha = 0.8f), Color(0xFFFFD700).copy(alpha = 0.3f), Color.Transparent)
                        )
                    )
            )
        }

        // Fruit body container
        Box(
            modifier = Modifier
                .size(fruit.sizeDp.dp)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    if (fruit.type.isZonk) {
                        Brush.radialGradient(listOf(Color(0xFFBA68C8), Color(0xFF4A148C)))
                    } else if (fruit.type.isGolden) {
                        Brush.radialGradient(listOf(Color(0xFFFFF9C4), Color(0xFFFFD700), Color(0xFFFFA000)))
                    } else {
                        Brush.radialGradient(listOf(fruit.type.baseColor.copy(alpha = 0.9f), fruit.type.baseColor))
                    }
                )
                .border(
                    width = if (fruit.type.isGolden) 3.dp else 2.dp,
                    color = if (fruit.type.isGolden) Color.White else Color.White.copy(alpha = 0.7f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = fruit.type.emoji,
                fontSize = (fruit.sizeDp * 0.52f).sp
            )
        }
    }
}

@Composable
fun PlayerBasket(
    player: Player,
    modifier: Modifier = Modifier
) {
    val isP1 = player.id == PlayerId.PLAYER_1
    val isLost = player.isLost

    val basketColor = if (isLost) Color(0xFF616161) else player.id.primaryColor
    val accentColor = if (isLost) Color(0xFF9E9E9E) else player.id.accentColor

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Player identity pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF150A26).copy(alpha = 0.85f))
                .border(1.5.dp, basketColor, RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = if (isLost) "${player.id.displayName} (LOST)" else player.id.displayName,
                color = if (isLost) Color(0xFFFF5252) else basketColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Basket catcher bowl
        Box(
            modifier = Modifier
                .size(width = 82.dp, height = 48.dp)
                .shadow(8.dp, RoundedCornerShape(bottomStart = 26.dp, bottomEnd = 26.dp))
                .clip(RoundedCornerShape(bottomStart = 26.dp, bottomEnd = 26.dp, topStart = 6.dp, topEnd = 6.dp))
                .background(Brush.verticalGradient(listOf(basketColor, accentColor)))
                .border(2.5.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(bottomStart = 26.dp, bottomEnd = 26.dp, topStart = 6.dp, topEnd = 6.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Inner basket pattern
            Canvas(modifier = Modifier.size(width = 70.dp, height = 30.dp)) {
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.25f),
                    topLeft = Offset(0f, 0f),
                    size = Size(size.width, size.height),
                    cornerRadius = CornerRadius(10f, 10f),
                    style = Stroke(width = 3f)
                )
            }
            Text(
                text = if (isP1) "🧺 P1" else "🧺 P2",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun FloatingScoreText(
    floatingScore: FloatingScore,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.65f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = floatingScore.text,
            color = if (floatingScore.isZonk) Color(0xFFFF5252) else Color(0xFFFFEB3B),
            fontSize = if (floatingScore.isZonk) 18.sp else 16.sp,
            fontWeight = FontWeight.Black
        )
    }
}
