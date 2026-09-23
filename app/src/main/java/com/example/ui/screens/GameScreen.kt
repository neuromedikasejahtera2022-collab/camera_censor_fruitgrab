package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.camera.CameraManager
import com.example.config.GameConfig
import com.example.game.GameEngine
import com.example.models.FloatingScore
import com.example.models.Fruit
import com.example.models.Player
import com.example.models.PlayerId
import com.example.ui.components.ArcadeButton
import com.example.ui.components.ArcadeIconButton
import com.example.ui.components.FloatingScoreText
import com.example.ui.components.FruitItem
import com.example.ui.components.PlayerBasket

@Composable
fun GameScreen(
    cameraManager: CameraManager,
    players: List<Player>,
    fruits: List<Fruit>,
    floatingScores: List<FloatingScore>,
    remainingTimeMs: Long,
    isPaused: Boolean,
    isSoundOn: Boolean,
    isDebugMode: Boolean,
    showExitDialog: Boolean,
    onPauseToggle: () -> Unit,
    onSoundToggle: () -> Unit,
    onRequestExit: () -> Unit,
    onConfirmExit: () -> Unit,
    onCancelExit: () -> Unit,
    onSimulateMovement: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    // Bind Camera for invisible body sensor processing during gameplay
    DisposableEffect(Unit) {
        cameraManager.startCamera(
            lifecycleOwner = lifecycleOwner,
            previewSurface = null // Invisible background analyzer
        )
        onDispose {
            cameraManager.stopCamera()
        }
    }

    val p1 = players.firstOrNull { it.id == PlayerId.PLAYER_1 } ?: Player(PlayerId.PLAYER_1)
    val p2 = players.firstOrNull { it.id == PlayerId.PLAYER_2 } ?: Player(PlayerId.PLAYER_2)

    var simulatedP1X by remember { mutableFloatStateOf(p1.currentX) }
    var simulatedP2X by remember { mutableFloatStateOf(p2.currentX) }

    val isUrgent = remainingTimeMs < 10_000L

    val infiniteTransition = rememberInfiniteTransition(label = "urgent_timer")
    val timerScale by infiniteTransition.animateFloat(
        initialValue = if (isUrgent) 1.0f else 1.0f,
        targetValue = if (isUrgent) 1.15f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "timer_pulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0F041F),
                        Color(0xFF1B0A33),
                        Color(0xFF0C031A)
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // TOP HUD: P1 Score, Arcade Timer, P2 Score & Action Controls
            TopGameBar(
                p1Score = p1.score,
                p2Score = p2.score,
                timeString = GameEngine.formatTimeRemaining(remainingTimeMs),
                timerScale = timerScale,
                isUrgent = isUrgent,
                isSoundOn = isSoundOn,
                onPauseToggle = onPauseToggle,
                onSoundToggle = onSoundToggle,
                onRequestExit = onRequestExit,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            )

            // MAIN GAMEPLAY ARENA
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Transparent)
            ) {
                val arenaWidth = maxWidth
                val arenaHeight = maxHeight

                // Catch Zone subtle neon marker line
                Box(
                    modifier = Modifier
                        .offset(y = arenaHeight * GameConfig.CATCH_ZONE_Y)
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color(0xFF00E5FF).copy(alpha = 0.4f),
                                    Color(0xFFFF007A).copy(alpha = 0.4f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Render Falling Fruits
                fruits.forEach { fruit ->
                    val fruitX = arenaWidth * fruit.x - (fruit.sizeDp / 2).dp
                    val fruitY = arenaHeight * fruit.y

                    FruitItem(
                        fruit = fruit,
                        modifier = Modifier.offset(x = fruitX, y = fruitY)
                    )
                }

                // Render Floating Score Popups
                floatingScores.forEach { score ->
                    val scoreX = arenaWidth * score.x - 24.dp
                    val scoreY = arenaHeight * score.y

                    FloatingScoreText(
                        floatingScore = score,
                        modifier = Modifier.offset(x = scoreX, y = scoreY)
                    )
                }

                // Render Player 1 Basket (Cyan / Blue)
                val p1PixelX = arenaWidth * p1.currentX - 41.dp
                val basketY = arenaHeight * GameConfig.CATCH_ZONE_Y - 14.dp

                PlayerBasket(
                    player = p1,
                    modifier = Modifier.offset(x = p1PixelX, y = basketY)
                )

                // Render Player 2 Basket (Coral / Orange)
                val p2PixelX = arenaWidth * p2.currentX - 41.dp

                PlayerBasket(
                    player = p2,
                    modifier = Modifier.offset(x = p2PixelX, y = basketY)
                )

                // Body Sensor Tracking Status Chip (Corner PIP)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF140824).copy(alpha = 0.75f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SensorStatusDot("P1", !p1.isLost, Color(0xFF00E5FF))
                    SensorStatusDot("P2", !p2.isLost, Color(0xFFFF5252))
                }
            }

            // SIMULATION / DEBUG CONTROLS: Touch Sliders (active when debugSensorMode = true)
            if (isDebugMode) {
                SimulationControlBar(
                    p1X = simulatedP1X,
                    p2X = simulatedP2X,
                    onUpdate = { newP1, newP2 ->
                        simulatedP1X = newP1
                        simulatedP2X = newP2
                        onSimulateMovement(newP1, newP2)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }

        // PAUSE OVERLAY
        if (isPaused) {
            PauseOverlay(
                onResume = onPauseToggle,
                onExit = onRequestExit,
                modifier = Modifier.fillMaxSize()
            )
        }

        // EXIT CONFIRMATION DIALOG
        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = onCancelExit,
                title = {
                    Text(
                        text = "EXIT MATCH?",
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                },
                text = {
                    Text(
                        text = "Your current 60s fruit match will be lost.",
                        color = Color.LightGray
                    )
                },
                confirmButton = {
                    TextButton(onClick = onConfirmExit) {
                        Text("EXIT", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = onCancelExit) {
                        Text("CANCEL", color = Color.White)
                    }
                },
                containerColor = Color(0xFF1E1035),
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
private fun TopGameBar(
    p1Score: Int,
    p2Score: Int,
    timeString: String,
    timerScale: Float,
    isUrgent: Boolean,
    isSoundOn: Boolean,
    onPauseToggle: () -> Unit,
    onSoundToggle: () -> Unit,
    onRequestExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Controls Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ArcadeIconButton(
                icon = Icons.Default.Close,
                onClick = onRequestExit,
                contentDescription = "Exit Match",
                borderColor = Color(0xFFFF5252),
                iconTint = Color(0xFFFF5252),
                testTag = "game_exit_button"
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ArcadeIconButton(
                    icon = if (isSoundOn) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                    onClick = onSoundToggle,
                    contentDescription = "Sound Toggle",
                    borderColor = Color(0xFF00E5FF),
                    testTag = "game_sound_button"
                )

                ArcadeIconButton(
                    icon = Icons.Default.Pause,
                    onClick = onPauseToggle,
                    contentDescription = "Pause Match",
                    borderColor = Color(0xFFFFD600),
                    testTag = "game_pause_button"
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Scores and Central Timer Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1A0A33).copy(alpha = 0.90f))
                .border(2.dp, Color(0xFF381A5E), RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player 1 Score Card
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = "P1 CYAN",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF00E5FF)
                )
                Text(
                    text = "$p1Score",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    modifier = Modifier.testTag("p1_score_text")
                )
            }

            // Central Timer Badge
            Box(
                modifier = Modifier
                    .scale(timerScale)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isUrgent) Color(0xFFFF1744) else Color(0xFF2C1052))
                    .border(2.dp, if (isUrgent) Color.White else Color(0xFFFF007A), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = timeString,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 2.sp,
                    modifier = Modifier.testTag("match_timer_text")
                )
            }

            // Player 2 Score Card
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "P2 CORAL",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFF5252)
                )
                Text(
                    text = "$p2Score",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    modifier = Modifier.testTag("p2_score_text")
                )
            }
        }
    }
}

@Composable
private fun SensorStatusDot(label: String, isOk: Boolean, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (isOk) color else Color.Gray)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = if (isOk) "$label OK" else "$label LOST",
            color = if (isOk) Color.White else Color.Gray,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PauseOverlay(
    onResume: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.78f))
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF1E1035))
                .border(2.5.dp, Color(0xFF00E5FF), RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Text(
                text = "GAME PAUSED",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFFFEE58),
                letterSpacing = 2.sp
            )

            ArcadeButton(
                text = "RESUME",
                onClick = onResume,
                icon = Icons.Default.PlayArrow,
                gradientColors = listOf(Color(0xFF00E5FF), Color(0xFF0077FF)),
                testTag = "resume_button"
            )

            ArcadeButton(
                text = "EXIT MATCH",
                onClick = onExit,
                icon = Icons.Default.Close,
                gradientColors = listOf(Color(0xFFFF5252), Color(0xFFD50000)),
                testTag = "pause_exit_button"
            )
        }
    }
}

@Composable
private fun SimulationControlBar(
    p1X: Float,
    p2X: Float,
    onUpdate: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1A0A2E).copy(alpha = 0.95f))
            .border(1.dp, Color(0xFFFF3D00), RoundedCornerShape(16.dp))
            .padding(10.dp)
    ) {
        Text(
            text = "⚡ TOUCH SIMULATION (DRAG TO MOVE BASKETS)",
            color = Color(0xFFFF3D00),
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // P1 Touch Drag Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF00E5FF).copy(alpha = 0.25f))
                    .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(8.dp))
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val next = (p1X + dragAmount.x / 400f).coerceIn(0.06f, 0.94f)
                            onUpdate(next, p2X)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("DRAG P1 (CYAN)", color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            // P2 Touch Drag Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFF5252).copy(alpha = 0.25f))
                    .border(1.dp, Color(0xFFFF5252), RoundedCornerShape(8.dp))
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val next = (p2X + dragAmount.x / 400f).coerceIn(0.06f, 0.94f)
                            onUpdate(p1X, next)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("DRAG P2 (CORAL)", color = Color(0xFFFF5252), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
