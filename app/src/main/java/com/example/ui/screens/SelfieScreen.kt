package com.example.ui.screens

import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.camera.CameraManager
import com.example.models.GameResult
import com.example.models.MatchWinner
import com.example.sensor.face.FaceSmileState
import com.example.ui.components.ArcadeButton
import com.example.ui.components.ArcadeIconButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SelfieScreen(
    cameraManager: CameraManager,
    faceSmileState: FaceSmileState,
    gameResult: GameResult?,
    onManualCapture: () -> Unit,
    onContinue: () -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val hasCaptured = gameResult?.selfieBitmapPath != null

    val infiniteTransition = rememberInfiniteTransition(label = "countdown_anim")
    val countdownPulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0C031A))
    ) {
        // Front Camera Preview
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    cameraManager.startCamera(
                        lifecycleOwner = lifecycleOwner,
                        previewSurface = surfaceProvider
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        DisposableEffect(Unit) {
            onDispose {
                cameraManager.stopCamera()
            }
        }

        // Dark gradient scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.7f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        // Main Overlay UI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Back & Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ArcadeIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    onClick = onHome,
                    contentDescription = "Back",
                    testTag = "selfie_back_button"
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "VICTORY SMILE SELFIE",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.5.sp
                )
            }

            // Central Area: Countdown or Captured Frame
            if (hasCaptured) {
                // Post-Capture Trophy Card
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF1E0B38).copy(alpha = 0.95f))
                        .border(3.dp, Color(0xFF00E676), RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "SELFIE CAPTURED!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF00E676)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "WINNER: ${gameResult?.winner?.name?.replace('_', ' ')}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD600)
                    )
                    Text(
                        text = "P1: ${gameResult?.p1Score} pts  |  P2: ${gameResult?.p2Score} pts",
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Text(
                        text = SimpleDateFormat("dd MMM yyyy • HH:mm:ss", Locale.getDefault()).format(Date(gameResult?.timestamp ?: System.currentTimeMillis())),
                        fontSize = 11.sp,
                        color = Color.LightGray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            } else if (faceSmileState.isBothSmiling && faceSmileState.consecutiveSmileFrames > 4) {
                // Auto Countdown Active
                Box(
                    modifier = Modifier
                        .scale(countdownPulse)
                        .clip(CircleShape)
                        .background(Color(0xFFFF007A))
                        .border(4.dp, Color.White, CircleShape)
                        .size(110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${faceSmileState.countdownNumber}",
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            } else {
                // Dual Smile Status Meters
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF140824).copy(alpha = 0.85f))
                        .border(2.dp, Color(0xFF9C27B0), RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Text(
                        text = "STAND TOGETHER AND SMILE 🙂",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFEE58),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        SmileMeter(
                            playerLabel = "PLAYER 1",
                            smileProb = faceSmileState.p1SmileScore,
                            isDetected = faceSmileState.p1FaceDetected,
                            accentColor = Color(0xFF00E5FF)
                        )

                        SmileMeter(
                            playerLabel = "PLAYER 2",
                            smileProb = faceSmileState.p2SmileScore,
                            isDetected = faceSmileState.p2FaceDetected,
                            accentColor = Color(0xFFFF5252)
                        )
                    }
                }
            }

            // Bottom Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (hasCaptured) {
                    ArcadeButton(
                        text = "VIEW HIGH SCORES",
                        onClick = onContinue,
                        icon = Icons.Default.EmojiEvents,
                        gradientColors = listOf(Color(0xFFFFD600), Color(0xFFFF8800)),
                        testTag = "selfie_continue_button"
                    )
                } else {
                    // Manual capture shortcut (also for simulator/debug test)
                    ArcadeButton(
                        text = "SNAP PHOTO NOW",
                        onClick = onManualCapture,
                        icon = Icons.Default.CameraAlt,
                        gradientColors = listOf(Color(0xFFFF007A), Color(0xFF7928CA)),
                        testTag = "manual_snap_button"
                    )
                }

                ArcadeButton(
                    text = "BACK TO HOME",
                    onClick = onHome,
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    gradientColors = listOf(Color(0xFF424242), Color(0xFF212121)),
                    testTag = "selfie_home_button"
                )
            }
        }
    }
}

@Composable
private fun SmileMeter(
    playerLabel: String,
    smileProb: Float,
    isDetected: Boolean,
    accentColor: Color
) {
    val isSmiling = smileProb >= 0.55f

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = playerLabel,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor
        )
        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(if (isSmiling) Color(0xFF00E676).copy(alpha = 0.3f) else Color.DarkGray.copy(alpha = 0.5f))
                .border(2.dp, if (isSmiling) Color(0xFF00E676) else Color.Gray, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SentimentSatisfiedAlt,
                contentDescription = null,
                tint = if (isSmiling) Color(0xFF00E676) else Color.LightGray,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (isSmiling) "SMILE! ✓" else if (isDetected) "${(smileProb * 100).toInt()}%" else "NO FACE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSmiling) Color(0xFF00E676) else Color.LightGray
        )
    }
}
