package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.FruitType
import com.example.ui.components.ArcadeIconButton

@Composable
fun HelpScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF140727),
                        Color(0xFF220A40),
                        Color(0xFF0F041F)
                    )
                )
            )
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ArcadeIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    onClick = onBack,
                    contentDescription = "Back",
                    testTag = "help_back_button"
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "HOW TO PLAY",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 2.sp
                )
            }

            // Step by Step Instructions
            HelpCard(title = "🎮 GAMEPLAY FLOW") {
                InstructionStep(1, "Stand side-by-side in front of the front camera.")
                InstructionStep(2, "Complete the initial 2-player body calibration.")
                InstructionStep(3, "Move your body left and right to control your basket.")
                InstructionStep(4, "Catch falling fruits to score maximum points.")
                InstructionStep(5, "Both players compete for fruits in a 60-second match.")
                InstructionStep(6, "Highest score wins the match and trophy!")
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Fruit Values Table
            HelpCard(title = "🎯 GAME OBJECTS & POINTS") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    FruitValueItem(FruitType.APPLE_100)
                    FruitValueItem(FruitType.BANANA_200)
                    FruitValueItem(FruitType.ORANGE_300)
                }
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    FruitValueItem(FruitType.WATERMELON_400)
                    FruitValueItem(FruitType.GOLDEN_FRUIT_500)
                    FruitValueItem(FruitType.ROTTEN_FRUIT_ZONK)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Hand Cursor & Selfie Gesture
            HelpCard(title = "✋ SENSOR GESTURES") {
                InstructionStep(null, "✋ OPEN PALM: Moves the shared floating cursor.")
                InstructionStep(null, "✊ FIST: Triggers a button click or selection.")
                InstructionStep(null, "🙂 DUAL SMILE: Both players smile at the selfie camera to trigger auto-capture!")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HelpCard(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1E0E38))
            .border(2.dp, Color(0xFF4A1F85), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFFFFD600),
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        content()
    }
}

@Composable
private fun InstructionStep(stepNumber: Int?, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        if (stepNumber != null) {
            Text(
                text = "$stepNumber. ",
                color = Color(0xFF00E5FF),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
        Text(
            text = text,
            color = Color.White,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun FruitValueItem(fruitType: FruitType) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .border(
                    width = if (fruitType.isGolden) 2.5.dp else 1.5.dp,
                    color = if (fruitType.isGolden) Color(0xFFFFD700) else Color.White.copy(alpha = 0.85f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = fruitType.drawableRes),
                contentDescription = fruitType.label,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = fruitType.label,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = if (fruitType.points > 0) "+${fruitType.points}" else "${fruitType.points}",
            color = if (fruitType.isZonk) Color(0xFFFF5252) else if (fruitType.isGolden) Color(0xFFFFD700) else Color(0xFF00E5FF),
            fontSize = 12.sp,
            fontWeight = FontWeight.Black
        )
    }
}
