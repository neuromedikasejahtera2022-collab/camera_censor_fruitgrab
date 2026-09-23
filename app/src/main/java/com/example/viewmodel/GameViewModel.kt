package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundManager
import com.example.camera.CameraManager
import com.example.config.GameConfig
import com.example.data.AppDatabase
import com.example.data.HighScoreRepository
import com.example.game.GameEngine
import com.example.models.CalibrationState
import com.example.models.CalibrationStatus
import com.example.models.GameResult
import com.example.models.GameScreenState
import com.example.models.GestureState
import com.example.models.MatchWinner
import com.example.models.Player
import com.example.models.PlayerCalibration
import com.example.models.PlayerId
import com.example.sensor.PlayerTracker
import com.example.sensor.face.FaceSmileState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {

    val soundManager = SoundManager(application)
    val playerTracker = PlayerTracker()
    val cameraManager = CameraManager(application)
    private val database = AppDatabase.getDatabase(application)
    val highScoreRepo = HighScoreRepository(database.scoreDao())

    val gameEngine: GameEngine = GameEngine(
        onSoundCatch = { points -> soundManager.playCatch(points) },
        onSoundSpawn = { soundManager.playFruitSpawn() },
        onSoundGameOver = {
            soundManager.playGameOver()
            soundManager.playWinner()
        }
    )

    private val _currentScreen = MutableStateFlow(GameScreenState.HOME)
    val currentScreen: StateFlow<GameScreenState> = _currentScreen.asStateFlow()

    private val _isCalibrated = MutableStateFlow(false)
    val isCalibrated: StateFlow<Boolean> = _isCalibrated.asStateFlow()

    private val _lastResult = MutableStateFlow<GameResult?>(null)
    val lastResult: StateFlow<GameResult?> = _lastResult.asStateFlow()

    private val _showExitConfirmation = MutableStateFlow(false)
    val showExitConfirmation: StateFlow<Boolean> = _showExitConfirmation.asStateFlow()

    private val _isDebugMode = MutableStateFlow(GameConfig.debugSensorMode)
    val isDebugMode: StateFlow<Boolean> = _isDebugMode.asStateFlow()

    val players: StateFlow<List<Player>> = playerTracker.players
    val gestureState: StateFlow<GestureState> = playerTracker.gestureState
    val faceSmileState: StateFlow<FaceSmileState> = playerTracker.faceSmileState

    init {
        // Connect Camera frame analysis to PlayerTracker
        cameraManager.onFrameAnalyzed = { poses, hand, faces ->
            playerTracker.onCameraFrame(poses, hand, faces)

            // Auto-check calibration in calibration screen
            if (_currentScreen.value == GameScreenState.CALIBRATION) {
                val calib = playerTracker.calibration.value
                if (calib.isCalibrated && !_isCalibrated.value) {
                    _isCalibrated.value = true
                    soundManager.playCountdownBeep(isFinal = true)
                }
            }

            // Auto-check selfie trigger in selfie screen
            if (_currentScreen.value == GameScreenState.SELFIE) {
                val smileState = playerTracker.faceSmileState.value
                if (smileState.shouldCaptureSelfie) {
                    captureSelfie()
                }
            }
        }

        // Connect GameEngine score mutations to PlayerTracker
        gameEngine.onScoreChanged = { playerId, points ->
            if (playerId == PlayerId.PLAYER_1) {
                playerTracker.updateScore(points, 0)
            } else {
                playerTracker.updateScore(0, points)
            }
        }

        // Connect Match completion
        gameEngine.onMatchFinished = { result ->
            _lastResult.value = result
            _currentScreen.value = GameScreenState.GAME_OVER
            viewModelScope.launch {
                highScoreRepo.saveGameResult(
                    p1Score = result.p1Score,
                    p2Score = result.p2Score,
                    winner = result.winner,
                    selfieUri = null
                )
            }
        }
    }

    fun navigateTo(screen: GameScreenState) {
        soundManager.playButtonClick()
        _showExitConfirmation.value = false

        if (screen == GameScreenState.CALIBRATION) {
            playerTracker.calibrationManager.reset()
            playerTracker.setCalibration(PlayerCalibration(isCalibrated = false))
            _isCalibrated.value = false
        } else if (screen == GameScreenState.PLAYING) {
            startGame()
            return
        }

        _currentScreen.value = screen
    }

    fun onCalibrationContinue() {
        soundManager.playButtonClick()
        _isCalibrated.value = true
        _currentScreen.value = GameScreenState.HOME
    }

    fun forceCalibrationForDebug() {
        val calib = playerTracker.calibrationManager.forceCalibration()
        playerTracker.setCalibration(calib)
        _isCalibrated.value = true
        soundManager.playButtonClick()
        _currentScreen.value = GameScreenState.HOME
    }

    fun startGame() {
        soundManager.playGameStart()
        playerTracker.resetScores()
        playerTracker.resetForMatch()
        val p1 = players.value.first { it.id == PlayerId.PLAYER_1 }
        val p2 = players.value.first { it.id == PlayerId.PLAYER_2 }
        gameEngine.startMatch(p1, p2)
        _currentScreen.value = GameScreenState.PLAYING
    }

    fun pauseGame() {
        soundManager.playButtonClick()
        gameEngine.pause()
    }

    fun resumeGame() {
        soundManager.playButtonClick()
        gameEngine.resume()
    }

    fun requestExitGame() {
        soundManager.playButtonClick()
        gameEngine.pause()
        _showExitConfirmation.value = true
    }

    fun cancelExitGame() {
        soundManager.playButtonClick()
        _showExitConfirmation.value = false
        gameEngine.resume()
    }

    fun confirmExitGame() {
        soundManager.playButtonClick()
        _showExitConfirmation.value = false
        gameEngine.stopMatch()
        _currentScreen.value = GameScreenState.HOME
    }

    fun startSelfieMode() {
        soundManager.playButtonClick()
        playerTracker.faceSmileDetector.reset()
        _currentScreen.value = GameScreenState.SELFIE
    }

    fun captureSelfie() {
        soundManager.playCameraShutter()
        // Save selfie event with timestamp
        val currentResult = _lastResult.value
        if (currentResult != null) {
            _lastResult.value = currentResult.copy(
                selfieBitmapPath = "selfie_${System.currentTimeMillis()}"
            )
        }
    }

    fun toggleDebugMode(): Boolean {
        soundManager.playButtonClick()
        val next = !_isDebugMode.value
        _isDebugMode.value = next
        GameConfig.debugSensorMode = next
        return next
    }

    fun updateSimulatedMovement(p1X: Float, p2X: Float) {
        playerTracker.updatePlayerPositionsManual(p1X, p2X)
    }

    override fun onCleared() {
        super.onCleared()
        gameEngine.stopMatch()
        cameraManager.release()
    }
}
