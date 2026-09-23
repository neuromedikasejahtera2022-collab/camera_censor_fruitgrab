package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.GameResult
import com.example.models.MatchWinner
import com.example.ui.components.ArcadeButton

@Composable
fun GameOverScreen(
    gameResult: GameResult?,
    onEndWithSelfie: () -> Unit,
    onPlayAgain: () -> Unit,
    onViewScores: () -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val p1Score = gameResult?.p1Score ?: 0
    val p2Score = gameResult?.p2Score ?: 0
    val winner = gameResult?.winner ?: MatchWinner.DRAW

    val winnerTitle = when (winner) {
        MatchWinner.PLAYER_1 -> "PLAYER 1 WINS!"
        MatchWinner.PLAYER_2 -> "PLAYER 2 WINS!"
        MatchWinner.DRAW -> "IT'S A DRAW!"
    }

    val winnerColor = when (winner) {
        MatchWinner.PLAYER_1 -> Color(0xFF00E5FF)
        MatchWinner.PLAYER_2 -> Color(0xFFFF5252)
        MatchWinner.DRAW -> Color(0xFFFFD600)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "winner_anim")
    val winnerScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "winner_pulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF19062E),
                        Color(0xFF2C0A52),
                        Color(0xFF10041F)
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Text(
                text = "MATCH COMPLETED!",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = Color.LightGray,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 16.dp)
            )

            // Winner Celebration Card
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(winnerScale)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF1C0A33))
                    .border(3.dp, winnerColor, RoundedCornerShape(28.dp))
                    .padding(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Trophy",
                    tint = winnerColor,
                    modifier = Modifier.size(68.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = winnerTitle,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = winnerColor,
                    letterSpacing = 1.5.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("winner_title_text")
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Score Comparison Box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF120524))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("P1 SCORE", color = Color(0xFF00E5FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("$p1Score", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                    }

                    Text("VS", color = Color.Gray, fontSize = 18.sp, fontWeight = FontWeight.Black)

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("P2 SCORE", color = Color(0xFFFF5252), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("$p2Score", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            // Action Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // End with Selfie Button
                ArcadeButton(
                    text = "END WITH SELFIE",
                    onClick = onEndWithSelfie,
                    icon = Icons.Default.CameraAlt,
                    gradientColors = listOf(Color(0xFFFF007A), Color(0xFFFF7A00)),
                    testTag = "end_with_selfie_button"
                )

                // Play Again
                ArcadeButton(
                    text = "PLAY AGAIN",
                    onClick = onPlayAgain,
                    icon = Icons.Default.Replay,
                    gradientColors = listOf(Color(0xFF00E5FF), Color(0xFF0077FF)),
                    testTag = "play_again_button"
                )

                // Scores & Home Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        ArcadeButton(
                            text = "LEADERBOARD",
                            onClick = onViewScores,
                            icon = Icons.Default.EmojiEvents,
                            gradientColors = listOf(Color(0xFFFFD600), Color(0xFFFF8800)),
                            testTag = "game_over_leaderboard_button"
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        ArcadeButton(
                            text = "HOME",
                            onClick = onHome,
                            icon = Icons.Default.Home,
                            gradientColors = listOf(Color(0xFF651FFF), Color(0xFF3D5AFE)),
                            testTag = "game_over_home_button"
                        )
                    }
                }
            }
        }
    }
}
