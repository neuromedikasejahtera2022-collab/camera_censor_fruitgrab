package com.example.config

/**
 * Central game configuration for Fruit Catcher Duo.
 * All gameplay parameters, sensor tuning, spawn rates, and physics are defined here.
 */
object GameConfig {
    // Game Match Settings
    const val GAME_DURATION_MS: Long = 60_000L
    const val TIMER_TICK_INTERVAL_MS: Long = 50L

    // Fruit Spawning Configuration
    const val MIN_SPAWN_INTERVAL_MS: Long = 700L
    const val MAX_SPAWN_INTERVAL_MS: Long = 1300L
    const val DIFFICULTY_SCALING_FACTOR: Float = 0.35f // Fruits fall faster as timer progresses

    // Fruit Physics
    const val MIN_FALL_SPEED: Float = 0.22f // Normalized screen height per second
    const val MAX_FALL_SPEED: Float = 0.52f
    const val CATCH_ZONE_Y: Float = 0.82f // Vertical line where baskets catch fruits
    const val CATCH_DISTANCE: Float = 0.11f // Horizontal collision tolerance (0..1)
    const val FRUIT_DESPAWN_Y: Float = 1.05f

    // Fruit Type Probability Weights (must sum to 100)
    const val WEIGHT_APPLE_100: Int = 50
    const val WEIGHT_BANANA_200: Int = 25
    const val WEIGHT_ORANGE_300: Int = 13
    const val WEIGHT_WATERMELON_400: Int = 7
    const val WEIGHT_GOLDEN_500: Int = 1
    const val WEIGHT_ROTTEN_ZONK: Int = 4

    // Body Motion Sensor Parameters
    const val MAX_PLAYERS: Int = 2
    const val MOVEMENT_SENSITIVITY: Float = 2.4f // Multiplier for body delta offset
    const val MOVEMENT_SMOOTHING: Float = 0.30f // Exponential smoothing alpha (0..1)
    const val MOVEMENT_DEADZONE: Float = 0.015f // Minimum horizontal body shift to trigger move
    const val MAX_PLAYER_SPEED: Float = 1.5f

    // Calibration
    const val REQUIRED_CALIBRATION_FRAMES: Int = 25 // Frames of stable detection required
    const val CALIBRATION_COUNTDOWN_SECONDS: Int = 3
    const val PLAYER_LOST_GRACE_PERIOD_MS: Long = 1500L

    // Hand Gesture Cursor
    const val CURSOR_SENSITIVITY: Float = 1.35f
    const val CURSOR_SMOOTHING: Float = 0.35f
    const val GESTURE_DEBOUNCE_MS: Long = 350L

    // Smile / Selfie Detection
    const val SMILE_THRESHOLD: Float = 0.55f // Minimum smile probability for both players
    const val SMILE_CONSECUTIVE_FRAMES: Int = 15 // Frames of concurrent smiling
    const val SELFIE_COUNTDOWN_SECONDS: Int = 3

    // Debug Mode & Simulation
    // When true or when camera is not available, touch/keys simulate player movement
    var debugSensorMode: Boolean = false
}
