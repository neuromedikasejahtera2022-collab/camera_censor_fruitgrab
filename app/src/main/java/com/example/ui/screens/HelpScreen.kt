package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
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
            HelpCard(title = "🍎 FRUITS & POINTS") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    FruitValueItem("🍎", "+100", Color(0xFFFF334B))
                    FruitValueItem("🍌", "+200", Color(0xFFFFD600))
                    FruitValueItem("🍊", "+300", Color(0xFFFF8800))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    FruitValueItem("🍉", "+400", Color(0xFF00E676))
                    FruitValueItem("⭐", "+500 GOLD", Color(0xFFFFD700))
                    FruitValueItem("💀", "ZONK!", Color(0xFFBA68C8))
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
private fun FruitValueItem(emoji: String, points: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 28.sp)
        Text(points, color = color, fontSize = 12.sp, fontWeight = FontWeight.Black)
    }
}
