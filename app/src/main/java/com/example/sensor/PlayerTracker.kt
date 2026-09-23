package com.example.sensor

import com.example.config.GameConfig
import com.example.models.CalibrationState
import com.example.models.GestureState
import com.example.models.Player
import com.example.models.PlayerCalibration
import com.example.models.PlayerId
import com.example.sensor.face.DetectedFaceResult
import com.example.sensor.face.FaceSmileDetector
import com.example.sensor.face.FaceSmileState
import com.example.sensor.hand.HandGestureDetector
import com.example.sensor.hand.RawHandLandmarks
import com.example.sensor.pose.PlayerTrackResult
import com.example.sensor.pose.PoseTracker
import com.example.sensor.pose.RawBodyPose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Unified sensor coordinator for body pose, hand gesture cursor, and face smile analysis.
 */
class PlayerTracker {

    val poseTracker = PoseTracker()
    val calibrationManager = CalibrationManager()
    val handGestureDetector = HandGestureDetector()
    val faceSmileDetector = FaceSmileDetector()

    private val _calibration = MutableStateFlow(PlayerCalibration())
    val calibration: StateFlow<PlayerCalibration> = _calibration.asStateFlow()

    private val _players = MutableStateFlow(
        listOf(
            Player(PlayerId.PLAYER_1, currentX = 0.25f, calibratedX = 0.25f),
            Player(PlayerId.PLAYER_2, currentX = 0.75f, calibratedX = 0.75f)
        )
    )
    val players: StateFlow<List<Player>> = _players.asStateFlow()

    private val _gestureState = MutableStateFlow(GestureState())
    val gestureState: StateFlow<GestureState> = _gestureState.asStateFlow()

    private val _faceSmileState = MutableStateFlow(FaceSmileState())
    val faceSmileState: StateFlow<FaceSmileState> = _faceSmileState.asStateFlow()

    fun resetForMatch() {
        poseTracker.reset(_calibration.value)
        _players.value = listOf(
            Player(PlayerId.PLAYER_1, currentX = 0.25f, calibratedX = _calibration.value.p1BaselineX),
            Player(PlayerId.PLAYER_2, currentX = 0.75f, calibratedX = _calibration.value.p2BaselineX)
        )
    }

    fun setCalibration(newCalibration: PlayerCalibration) {
        _calibration.value = newCalibration
        poseTracker.reset(newCalibration)
        _players.value = listOf(
            Player(PlayerId.PLAYER_1, currentX = 0.25f, calibratedX = newCalibration.p1BaselineX, isReady = true),
            Player(PlayerId.PLAYER_2, currentX = 0.75f, calibratedX = newCalibration.p2BaselineX, isReady = true)
        )
    }

    /**
     * Ingests camera analysis results: body poses, hand landmarks, and faces.
     */
    fun onCameraFrame(
        poses: List<RawBodyPose>,
        hand: RawHandLandmarks?,
        faces: List<DetectedFaceResult>,
        currentTimeMs: Long = System.currentTimeMillis()
    ) {
        // If in calibration, feed to calibration manager
        if (!_calibration.value.isCalibrated) {
            val (calibState, completedCalibration) = calibrationManager.processFrame(poses, currentTimeMs)
            if (completedCalibration != null) {
                setCalibration(completedCalibration)
            }
        } else {
            // Process continuous gameplay body motion
            val trackResult = poseTracker.processPoses(poses, _calibration.value, currentTimeMs)
            val currentList = _players.value
            val p1 = currentList.firstOrNull { it.id == PlayerId.PLAYER_1 } ?: Player(PlayerId.PLAYER_1)
            val p2 = currentList.firstOrNull { it.id == PlayerId.PLAYER_2 } ?: Player(PlayerId.PLAYER_2)

            _players.value = listOf(
                p1.copy(currentX = trackResult.p1ScreenX, isLost = !trackResult.p1Detected),
                p2.copy(currentX = trackResult.p2ScreenX, isLost = !trackResult.p2Detected)
            )
        }

        // Process hand gestures
        _gestureState.value = handGestureDetector.processHand(hand, currentTimeMs)

        // Process faces for selfie
        _faceSmileState.value = faceSmileDetector.processFaces(faces, currentTimeMs)
    }

    /**
     * Manual / simulated player position update (for touch / debug / tests).
     */
    fun updatePlayerPositionsManual(p1X: Float, p2X: Float) {
        val currentList = _players.value
        val p1 = currentList.firstOrNull { it.id == PlayerId.PLAYER_1 } ?: Player(PlayerId.PLAYER_1)
        val p2 = currentList.firstOrNull { it.id == PlayerId.PLAYER_2 } ?: Player(PlayerId.PLAYER_2)

        _players.value = listOf(
            p1.copy(currentX = p1X.coerceIn(0.06f, 0.94f), isLost = false),
            p2.copy(currentX = p2X.coerceIn(0.06f, 0.94f), isLost = false)
        )
    }

    fun updateScore(p1Delta: Int, p2Delta: Int) {
        val currentList = _players.value
        val p1 = currentList.firstOrNull { it.id == PlayerId.PLAYER_1 } ?: Player(PlayerId.PLAYER_1)
        val p2 = currentList.firstOrNull { it.id == PlayerId.PLAYER_2 } ?: Player(PlayerId.PLAYER_2)

        val newP1Score = (p1.score + p1Delta).coerceAtLeast(0)
        val newP2Score = (p2.score + p2Delta).coerceAtLeast(0)

        _players.value = listOf(
            p1.copy(score = newP1Score, catchAnimationTimestamp = if (p1Delta != 0) System.currentTimeMillis() else p1.catchAnimationTimestamp),
            p2.copy(score = newP2Score, catchAnimationTimestamp = if (p2Delta != 0) System.currentTimeMillis() else p2.catchAnimationTimestamp)
        )
    }

    fun resetScores() {
        val currentList = _players.value
        _players.value = currentList.map { it.copy(score = 0) }
    }
}
