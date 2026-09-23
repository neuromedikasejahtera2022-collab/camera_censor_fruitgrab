package com.example.sensor.hand

import com.example.config.GameConfig
import com.example.models.GestureState

/**
 * Hand tracking landmark representation.
 */
data class RawHandLandmarks(
    val palmCenterX: Float, // Normalized 0..1 (already mirrored for front camera)
    val palmCenterY: Float, // Normalized 0..1
    val isFingersFolded: Boolean,
    val confidence: Float = 1.0f
)

/**
 * Hand gesture detector and cursor mapper.
 * Smooths motion and debounces fist clicks.
 */
class HandGestureDetector {

    private var smoothedX: Float = 0.5f
    private var smoothedY: Float = 0.5f
    private var lastClickTimestamp: Long = 0L
    private var wasFistLastFrame: Boolean = false

    var currentGesture: GestureState = GestureState()
        private set

    fun reset() {
        smoothedX = 0.5f
        smoothedY = 0.5f
        lastClickTimestamp = 0L
        wasFistLastFrame = false
        currentGesture = GestureState()
    }

    /**
     * Updates cursor position and detects FIST click event.
     */
    fun processHand(
        hand: RawHandLandmarks?,
        currentTimeMs: Long = System.currentTimeMillis()
    ): GestureState {
        if (hand == null) {
            currentGesture = currentGesture.copy(isHandDetected = false)
            wasFistLastFrame = false
            return currentGesture
        }

        // Apply exponential smoothing to cursor
        smoothedX += (hand.palmCenterX - smoothedX) * GameConfig.CURSOR_SMOOTHING
        smoothedY += (hand.palmCenterY - smoothedY) * GameConfig.CURSOR_SMOOTHING

        val isFist = hand.isFingersFolded
        val isOpenPalm = !isFist

        var triggerClick = 0L
        // Trigger click on rising edge of fist gesture with debounce
        if (isFist && !wasFistLastFrame) {
            if (currentTimeMs - lastClickTimestamp >= GameConfig.GESTURE_DEBOUNCE_MS) {
                lastClickTimestamp = currentTimeMs
                triggerClick = currentTimeMs
            }
        }
        wasFistLastFrame = isFist

        currentGesture = GestureState(
            cursorX = smoothedX.coerceIn(0.02f, 0.98f),
            cursorY = smoothedY.coerceIn(0.02f, 0.98f),
            isOpenPalm = isOpenPalm,
            isFist = isFist,
            isHandDetected = true,
            clickEventTrigger = triggerClick
        )

        return currentGesture
    }

    /**
     * Simulated hand input (for debug mode or touch interaction).
     */
    fun updateSimulated(x: Float, y: Float, isClick: Boolean, currentTimeMs: Long = System.currentTimeMillis()): GestureState {
        val clickTrigger = if (isClick && (currentTimeMs - lastClickTimestamp >= GameConfig.GESTURE_DEBOUNCE_MS)) {
            lastClickTimestamp = currentTimeMs
            currentTimeMs
        } else 0L

        currentGesture = GestureState(
            cursorX = x.coerceIn(0.02f, 0.98f),
            cursorY = y.coerceIn(0.02f, 0.98f),
            isOpenPalm = !isClick,
            isFist = isClick,
            isHandDetected = true,
            clickEventTrigger = clickTrigger
        )
        return currentGesture
    }
}
