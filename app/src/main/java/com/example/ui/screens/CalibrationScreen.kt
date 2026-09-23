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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.models.CalibrationState
import com.example.models.CalibrationStatus
import com.example.models.PlayerCalibration
import com.example.ui.components.ArcadeButton
import com.example.ui.components.ArcadeIconButton

@Composable
fun CalibrationScreen(
    cameraManager: CameraManager,
    calibrationState: CalibrationState,
    playerCalibration: PlayerCalibration,
    onContinue: () -> Unit,
    onForceCalibrationForDebug: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val isComplete = playerCalibration.isCalibrated || calibrationState.status == CalibrationStatus.CALIBRATION_COMPLETE

    val infiniteTransition = rememberInfiniteTransition(label = "calib_pulse")
    val silhouettePulse by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "silhouette_pulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F061F))
    ) {
        // Camera Preview Feed
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
            modifier = Modifier
                .fillMaxSize()
                .testTag("camera_preview_view")
        )

        DisposableEffect(Unit) {
            onDispose {
                cameraManager.stopCamera()
            }
        }

        // Translucent overlay scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0C051A).copy(alpha = 0.55f))
        )

        // Main Calibration UI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ArcadeIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    onClick = onBack,
                    contentDescription = "Back to Home",
                    testTag = "calibration_back_button"
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "PLAYER CALIBRATION",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 2.sp
                )
            }

            // Dual Player Silhouettes / Target Boxes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Player 1 Silhouette (Left)
                PlayerSilhouetteBox(
                    label = "PLAYER 1",
                    isDetected = calibrationState.p1Detected,
                    accentColor = Color(0xFF00E5FF),
                    modifier = Modifier.scale(silhouettePulse)
                )

                // Player 2 Silhouette (Right)
                PlayerSilhouetteBox(
                    label = "PLAYER 2",
                    isDetected = calibrationState.p2Detected,
                    accentColor = Color(0xFFFF5252),
                    modifier = Modifier.scale(silhouettePulse)
                )
            }

            // Middle Status Banner / Instruction
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1E1035).copy(alpha = 0.90f))
                    .border(2.dp, if (isComplete) Color(0xFF00E676) else Color(0xFFFF007A), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = if (isComplete) "CALIBRATION COMPLETE!" else calibrationState.instructionText.uppercase(),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isComplete) Color(0xFF00E676) else Color(0xFFFFEE58),
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.sp,
                    modifier = Modifier.testTag("calibration_instruction_text")
                )

                if (calibrationState.status == CalibrationStatus.COUNTDOWN && !isComplete) {
                    Text(
                        text = "${calibrationState.countdownNumber}",
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFF007A),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // Checkmarks
                Row(
                    modifier = Modifier.padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    PlayerReadyBadge("PLAYER 1", calibrationState.p1Detected || isComplete, Color(0xFF00E5FF))
                    PlayerReadyBadge("PLAYER 2", calibrationState.p2Detected || isComplete, Color(0xFFFF5252))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isComplete) {
                    ArcadeButton(
                        text = "CONTINUE TO GAME",
                        onClick = onContinue,
                        icon = Icons.Default.CheckCircle,
                        gradientColors = listOf(Color(0xFF00E676), Color(0xFF00B0FF)),
                        testTag = "calibration_continue_button"
                    )
                } else {
                    // Quick Calibration / Fallback for testing & simulation
                    ArcadeButton(
                        text = "QUICK CALIBRATE (SIMULATION)",
                        onClick = onForceCalibrationForDebug,
                        icon = Icons.Default.FlashOn,
                        gradientColors = listOf(Color(0xFFFF9100), Color(0xFFFF3D00)),
                        testTag = "force_calibration_button"
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerSilhouetteBox(
    label: String,
    isDetected: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isDetected) accentColor else Color.Gray.copy(alpha = 0.5f)

    Box(
        modifier = modifier
            .width(135.dp)
            .height(230.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF140727).copy(alpha = 0.65f))
            .border(2.5.dp, borderColor, RoundedCornerShape(24.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = if (isDetected) accentColor else Color.Gray,
                letterSpacing = 1.sp
            )

            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = if (isDetected) accentColor else Color.DarkGray,
                modifier = Modifier.size(80.dp)
            )

            Text(
                text = if (isDetected) "READY ✓" else "STAND HERE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDetected) Color(0xFF00E676) else Color.LightGray
            )
        }
    }
}

@Composable
private fun PlayerReadyBadge(label: String, isReady: Boolean, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(if (isReady) color else Color.Gray)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = if (isReady) "$label ✓" else "$label ...",
            color = if (isReady) Color.White else Color.Gray,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
