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
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
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
import com.example.models.GameScreenState
import com.example.models.GestureState
import com.example.ui.components.ArcadeButton
import com.example.ui.components.ArcadeIconButton
import com.example.ui.components.SharedHandCursor

@Composable
fun HomeScreen(
    isCalibrated: Boolean,
    isSoundOn: Boolean,
    isDebugMode: Boolean,
    gestureState: GestureState,
    onNavigate: (GameScreenState) -> Unit,
    onToggleSound: () -> Unit,
    onToggleDebug: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "home_anim")
    val titleFloat by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "title_float"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF140727),
                        Color(0xFF280B4A),
                        Color(0xFF0F061F)
                    )
                )
            )
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Status Bar: Sound Toggle & Sensor indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sensor Status Chip
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1F1138))
                        .border(1.5.dp, if (isCalibrated) Color(0xFF00E676) else Color(0xFFFFB300), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isCalibrated) Color(0xFF00E676) else Color(0xFFFFB300))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isCalibrated) "DUAL SENSOR READY" else "NEEDS CALIBRATION",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                // Controls: Debug & Sound
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ArcadeIconButton(
                        icon = Icons.Default.BugReport,
                        onClick = onToggleDebug,
                        contentDescription = "Toggle Debug Mode",
                        borderColor = if (isDebugMode) Color(0xFFFF3D00) else Color(0xFF4A3266),
                        iconTint = if (isDebugMode) Color(0xFFFF3D00) else Color.Gray,
                        testTag = "debug_mode_button"
                    )

                    ArcadeIconButton(
                        icon = if (isSoundOn) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                        onClick = onToggleSound,
                        contentDescription = "Toggle Sound",
                        borderColor = Color(0xFF00E5FF),
                        testTag = "sound_toggle_button"
                    )
                }
            }

            // Central Branding / Arcade Title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .scale(1.0f)
            ) {
                // Falling Fruit Illustration Header
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Text("🍎", fontSize = 34.sp)
                    Text("🍌", fontSize = 34.sp)
                    Text("⭐", fontSize = 38.sp)
                    Text("🍊", fontSize = 34.sp)
                    Text("🍉", fontSize = 34.sp)
                }

                Text(
                    text = "FRUIT",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFFD600),
                    letterSpacing = 4.sp,
                    modifier = Modifier.testTag("app_logo_fruit")
                )
                Text(
                    text = "CATCHER",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFF007A),
                    letterSpacing = 3.sp
                )
                Text(
                    text = "DUO",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF00E5FF),
                    letterSpacing = 6.sp
                )

                Text(
                    text = "2-Player Body-Motion Arcade",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )

                if (isDebugMode) {
                    Text(
                        text = "⚡ SENSOR SIMULATION ACTIVE",
                        fontSize = 11.sp,
                        color = Color(0xFFFF3D00),
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Action Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Play Button (Disabled if not calibrated)
                ArcadeButton(
                    text = if (isCalibrated) "PLAY MATCH (60s)" else "CALIBRATE FIRST TO PLAY",
                    onClick = { onNavigate(GameScreenState.PLAYING) },
                    enabled = isCalibrated,
                    icon = Icons.Default.PlayArrow,
                    gradientColors = listOf(Color(0xFF00E5FF), Color(0xFF0077FF)),
                    testTag = "play_match_button"
                )

                // Calibration Button
                ArcadeButton(
                    text = if (isCalibrated) "RE-CALIBRATE PLAYERS" else "START CALIBRATION",
                    onClick = { onNavigate(GameScreenState.CALIBRATION) },
                    icon = Icons.Default.CenterFocusStrong,
                    gradientColors = listOf(Color(0xFFFF007A), Color(0xFF7928CA)),
                    testTag = "calibration_button"
                )

                // High Scores & Help Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        ArcadeButton(
                            text = "SCORES",
                            onClick = { onNavigate(GameScreenState.SCOREBOARD) },
                            icon = Icons.Default.EmojiEvents,
                            gradientColors = listOf(Color(0xFFFF9100), Color(0xFFFF3D00)),
                            testTag = "high_scores_button"
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        ArcadeButton(
                            text = "HELP",
                            onClick = { onNavigate(GameScreenState.HELP) },
                            icon = Icons.Default.Help,
                            gradientColors = listOf(Color(0xFF651FFF), Color(0xFF3D5AFE)),
                            testTag = "help_button"
                        )
                    }
                }
            }
        }

        // Shared Hand Gesture Cursor Overlay
        SharedHandCursor(gestureState = gestureState)
    }
}
