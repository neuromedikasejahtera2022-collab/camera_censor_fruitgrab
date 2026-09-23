package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.models.CalibrationStatus
import com.example.models.GameScreenState
import com.example.ui.components.ArcadeButton
import com.example.ui.screens.CalibrationScreen
import com.example.ui.screens.GameOverScreen
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HelpScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ScoreScreen
import com.example.ui.screens.SelfieScreen
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.ScoreViewModel

@Composable
fun FruitCatcherApp(
    viewModel: GameViewModel = viewModel(),
    scoreViewModel: ScoreViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val isCalibrated by viewModel.isCalibrated.collectAsStateWithLifecycle()
    val isSoundOn by viewModel.soundManager.isSoundEnabled.collectAsStateWithLifecycle()
    val isDebugMode by viewModel.isDebugMode.collectAsStateWithLifecycle()
    val players by viewModel.players.collectAsStateWithLifecycle()
    val fruits by viewModel.gameEngine.fruits.collectAsStateWithLifecycle()
    val floatingScores by viewModel.gameEngine.floatingScores.collectAsStateWithLifecycle()
    val remainingTimeMs by viewModel.gameEngine.remainingTimeMs.collectAsStateWithLifecycle()
    val isPaused by viewModel.gameEngine.isPaused.collectAsStateWithLifecycle()
    val showExitDialog by viewModel.showExitConfirmation.collectAsStateWithLifecycle()
    val gestureState by viewModel.gestureState.collectAsStateWithLifecycle()
    val faceSmileState by viewModel.faceSmileState.collectAsStateWithLifecycle()
    val lastResult by viewModel.lastResult.collectAsStateWithLifecycle()
    val calibrationState = viewModel.playerTracker.calibrationManager.currentState
    val playerCalibration by viewModel.playerTracker.calibration.collectAsStateWithLifecycle()

    val allScores by scoreViewModel.allScores.collectAsStateWithLifecycle()
    val bestScore by scoreViewModel.bestScore.collectAsStateWithLifecycle()
    val todayBest by scoreViewModel.todayBestScore.collectAsStateWithLifecycle()

    if (!hasCameraPermission && !isDebugMode) {
        CameraPermissionPrompt(
            onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
            onContinueInDebugMode = {
                viewModel.toggleDebugMode()
            },
            modifier = modifier
        )
        return
    }

    when (currentScreen) {
        GameScreenState.HOME -> {
            HomeScreen(
                isCalibrated = isCalibrated,
                isSoundOn = isSoundOn,
                isDebugMode = isDebugMode,
                gestureState = gestureState,
                onNavigate = { viewModel.navigateTo(it) },
                onToggleSound = { viewModel.soundManager.toggleSound() },
                onToggleDebug = { viewModel.toggleDebugMode() },
                modifier = modifier
            )
        }
        GameScreenState.CALIBRATION -> {
            CalibrationScreen(
                cameraManager = viewModel.cameraManager,
                calibrationState = calibrationState,
                playerCalibration = playerCalibration,
                onContinue = { viewModel.onCalibrationContinue() },
                onForceCalibrationForDebug = { viewModel.forceCalibrationForDebug() },
                onBack = { viewModel.navigateTo(GameScreenState.HOME) },
                modifier = modifier
            )
        }
        GameScreenState.PLAYING -> {
            GameScreen(
                cameraManager = viewModel.cameraManager,
                players = players,
                fruits = fruits,
                floatingScores = floatingScores,
                remainingTimeMs = remainingTimeMs,
                isPaused = isPaused,
                isSoundOn = isSoundOn,
                isDebugMode = isDebugMode,
                showExitDialog = showExitDialog,
                onPauseToggle = { viewModel.gameEngine.togglePause() },
                onSoundToggle = { viewModel.soundManager.toggleSound() },
                onRequestExit = { viewModel.requestExitGame() },
                onConfirmExit = { viewModel.confirmExitGame() },
                onCancelExit = { viewModel.cancelExitGame() },
                onSimulateMovement = { p1, p2 -> viewModel.updateSimulatedMovement(p1, p2) },
                modifier = modifier
            )
        }
        GameScreenState.GAME_OVER -> {
            GameOverScreen(
                gameResult = lastResult,
                onEndWithSelfie = { viewModel.startSelfieMode() },
                onPlayAgain = { viewModel.startGame() },
                onViewScores = { viewModel.navigateTo(GameScreenState.SCOREBOARD) },
                onHome = { viewModel.navigateTo(GameScreenState.HOME) },
                modifier = modifier
            )
        }
        GameScreenState.SELFIE -> {
            SelfieScreen(
                cameraManager = viewModel.cameraManager,
                faceSmileState = faceSmileState,
                gameResult = lastResult,
                onManualCapture = { viewModel.captureSelfie() },
                onContinue = { viewModel.navigateTo(GameScreenState.SCOREBOARD) },
                onHome = { viewModel.navigateTo(GameScreenState.HOME) },
                modifier = modifier
            )
        }
        GameScreenState.SCOREBOARD -> {
            ScoreScreen(
                scores = allScores,
                bestScore = bestScore,
                todayBest = todayBest,
                isSoundOn = isSoundOn,
                gestureState = gestureState,
                onHome = { viewModel.navigateTo(GameScreenState.HOME) },
                onToggleSound = { viewModel.soundManager.toggleSound() },
                modifier = modifier
            )
        }
        GameScreenState.HELP -> {
            HelpScreen(
                onBack = { viewModel.navigateTo(GameScreenState.HOME) },
                modifier = modifier
            )
        }
        else -> {
            HomeScreen(
                isCalibrated = isCalibrated,
                isSoundOn = isSoundOn,
                isDebugMode = isDebugMode,
                gestureState = gestureState,
                onNavigate = { viewModel.navigateTo(it) },
                onToggleSound = { viewModel.soundManager.toggleSound() },
                onToggleDebug = { viewModel.toggleDebugMode() },
                modifier = modifier
            )
        }
    }
}

@Composable
private fun CameraPermissionPrompt(
    onRequestPermission: () -> Unit,
    onContinueInDebugMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF140727))
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF1E0E38))
                .padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Videocam,
                contentDescription = null,
                tint = Color(0xFF00E5FF),
                modifier = Modifier.size(64.dp)
            )

            Text(
                text = "CAMERA PERMISSION REQUIRED",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Fruit Catcher Duo tracks your body movements using the front camera so both players can catch fruits simultaneously without touching the screen.",
                fontSize = 14.sp,
                color = Color.LightGray,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            ArcadeButton(
                text = "ALLOW CAMERA",
                onClick = onRequestPermission,
                gradientColors = listOf(Color(0xFF00E5FF), Color(0xFF0077FF))
            )

            ArcadeButton(
                text = "USE SIMULATION MODE",
                onClick = onContinueInDebugMode,
                gradientColors = listOf(Color(0xFFFF9100), Color(0xFFFF3D00))
            )
        }
    }
}
