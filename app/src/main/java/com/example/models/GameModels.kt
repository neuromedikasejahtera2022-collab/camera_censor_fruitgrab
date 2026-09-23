package com.example.models

import androidx.compose.ui.graphics.Color
import com.example.config.GameConfig

enum class PlayerId(val displayName: String, val primaryColor: Color, val accentColor: Color) {
    PLAYER_1("Player 1", Color(0xFF00D2FF), Color(0xFF0077FF)), // Cyan / Royal Blue
    PLAYER_2("Player 2", Color(0xFFFF5252), Color(0xFFFF7A00))  // Coral / Vivid Amber
}

data class Player(
    val id: PlayerId,
    val currentX: Float = if (id == PlayerId.PLAYER_1) 0.25f else 0.75f, // Normalized 0..1
    val calibratedX: Float = if (id == PlayerId.PLAYER_1) 0.25f else 0.75f,
    val score: Int = 0,
    val isLost: Boolean = false,
    val isReady: Boolean = false,
    val catchAnimationTimestamp: Long = 0L,
    val smileScore: Float = 0f
)

data class PlayerCalibration(
    val p1BaselineX: Float = 0.25f,
    val p2BaselineX: Float = 0.75f,
    val isCalibrated: Boolean = false
)

enum class FruitType(
    val points: Int,
    val label: String,
    val emoji: String,
    val baseColor: Color,
    val isGolden: Boolean = false,
    val isZonk: Boolean = false
) {
    APPLE_100(100, "Apple", "🍎", Color(0xFFFF334B)),
    BANANA_200(200, "Banana", "🍌", Color(0xFFFFD600)),
    ORANGE_300(300, "Orange", "🍊", Color(0xFFFF8800)),
    WATERMELON_400(400, "Melon", "🍉", Color(0xFF00E676)),
    GOLDEN_FRUIT_500(500, "Golden Fruit", "⭐", Color(0xFFFFD700), isGolden = true),
    ROTTEN_FRUIT_ZONK(-150, "ZONK!", "💀", Color(0xFF9C27B0), isZonk = true);

    companion object {
        fun selectWeightedRandom(randomValue: Int = (0 until 100).random()): FruitType {
            var cumulative = 0
            if (randomValue < (cumulative + GameConfig.WEIGHT_APPLE_100)) return APPLE_100
            cumulative += GameConfig.WEIGHT_APPLE_100
            if (randomValue < (cumulative + GameConfig.WEIGHT_BANANA_200)) return BANANA_200
            cumulative += GameConfig.WEIGHT_BANANA_200
            if (randomValue < (cumulative + GameConfig.WEIGHT_ORANGE_300)) return ORANGE_300
            cumulative += GameConfig.WEIGHT_ORANGE_300
            if (randomValue < (cumulative + GameConfig.WEIGHT_WATERMELON_400)) return WATERMELON_400
            cumulative += GameConfig.WEIGHT_WATERMELON_400
            if (randomValue < (cumulative + GameConfig.WEIGHT_GOLDEN_500)) return GOLDEN_FRUIT_500
            return ROTTEN_FRUIT_ZONK
        }
    }
}

data class Fruit(
    val id: Long,
    val type: FruitType,
    var x: Float, // Normalized 0..1
    var y: Float, // Normalized 0..1
    val fallSpeed: Float,
    val sizeDp: Float = if (type.isGolden) 56f else 46f,
    var rotationAngle: Float = 0f,
    var isCaught: Boolean = false,
    var caughtBy: PlayerId? = null
)

data class FloatingScore(
    val id: Long,
    val text: String,
    val x: Float,
    val y: Float,
    val color: Color,
    val isZonk: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

enum class GameScreenState {
    HOME,
    CALIBRATION,
    READY,
    PLAYING,
    PAUSED,
    GAME_OVER,
    SELFIE,
    SCOREBOARD,
    HELP
}

enum class MatchWinner {
    PLAYER_1,
    PLAYER_2,
    DRAW
}

data class GameResult(
    val p1Score: Int,
    val p2Score: Int,
    val winner: MatchWinner,
    val timestamp: Long = System.currentTimeMillis(),
    val selfieBitmapPath: String? = null
)

data class GestureState(
    val cursorX: Float = 0.5f,
    val cursorY: Float = 0.5f,
    val isOpenPalm: Boolean = false,
    val isFist: Boolean = false,
    val isHandDetected: Boolean = false,
    val clickEventTrigger: Long = 0L
)

enum class CalibrationStatus {
    WAITING_FOR_2_PLAYERS,
    PLEASE_STAND_SIDE_BY_SIDE,
    HOLD_STILL,
    COUNTDOWN,
    CALIBRATING,
    CALIBRATION_COMPLETE,
    FAILED
}

data class CalibrationState(
    val status: CalibrationStatus = CalibrationStatus.WAITING_FOR_2_PLAYERS,
    val p1Detected: Boolean = false,
    val p2Detected: Boolean = false,
    val countdownNumber: Int = 3,
    val stableFramesCount: Int = 0,
    val instructionText: String = "Stand side-by-side inside the camera area"
)

data class CameraStatus(
    val isInitialized: Boolean = false,
    val hasPermission: Boolean = false,
    val errorMessage: String? = null
)
