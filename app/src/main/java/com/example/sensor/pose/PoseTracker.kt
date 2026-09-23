package com.example.sensor.pose

import com.example.config.GameConfig
import com.example.models.PlayerCalibration
import com.example.models.PlayerId
import kotlin.math.abs

/**
 * Raw detected body landmark center representation.
 */
data class RawBodyPose(
    val bodyCenterX: Float, // Normalized 0..1 (already mirrored for natural screen movement)
    val leftShoulderX: Float,
    val rightShoulderX: Float,
    val leftHipX: Float,
    val rightHipX: Float,
    val confidence: Float = 1.0f
)

/**
 * Output player tracking frame.
 */
data class PlayerTrackResult(
    val p1ScreenX: Float,
    val p2ScreenX: Float,
    val p1Detected: Boolean,
    val p2Detected: Boolean,
    val p1BodyCenter: Float,
    val p2BodyCenter: Float,
    val rawPosesCount: Int
)

/**
 * High-performance 2-player body motion tracker.
 * Calculates bodyCenterX = average(leftShoulderX, rightShoulderX, leftHipX, rightHipX).
 * Preserves player identities across frames via continuity matching & exponential smoothing.
 */
class PoseTracker {

    // Previous smoothed body centers
    private var lastP1RawCenter: Float = 0.25f
    private var lastP2RawCenter: Float = 0.75f

    // Current smoothed screen positions (0..1)
    private var smoothedP1ScreenX: Float = 0.25f
    private var smoothedP2ScreenX: Float = 0.75f

    // Lost player timers
    private var lastP1SeenTimestamp: Long = System.currentTimeMillis()
    private var lastP2SeenTimestamp: Long = System.currentTimeMillis()

    fun reset(calibration: PlayerCalibration) {
        lastP1RawCenter = calibration.p1BaselineX
        lastP2RawCenter = calibration.p2BaselineX
        smoothedP1ScreenX = 0.25f
        smoothedP2ScreenX = 0.75f
        lastP1SeenTimestamp = System.currentTimeMillis()
        lastP2SeenTimestamp = System.currentTimeMillis()
    }

    /**
     * Updates tracking with newly detected raw body poses.
     * Raw poses should have X in [0..1] mirrored for front camera.
     */
    fun processPoses(
        detectedPoses: List<RawBodyPose>,
        calibration: PlayerCalibration,
        currentTimeMs: Long = System.currentTimeMillis()
    ): PlayerTrackResult {
        if (detectedPoses.isEmpty()) {
            val p1Lost = (currentTimeMs - lastP1SeenTimestamp) > GameConfig.PLAYER_LOST_GRACE_PERIOD_MS
            val p2Lost = (currentTimeMs - lastP2SeenTimestamp) > GameConfig.PLAYER_LOST_GRACE_PERIOD_MS
            return PlayerTrackResult(
                p1ScreenX = smoothedP1ScreenX,
                p2ScreenX = smoothedP2ScreenX,
                p1Detected = !p1Lost,
                p2Detected = !p2Lost,
                p1BodyCenter = lastP1RawCenter,
                p2BodyCenter = lastP2RawCenter,
                rawPosesCount = 0
            )
        }

        var matchedP1: RawBodyPose? = null
        var matchedP2: RawBodyPose? = null

        if (detectedPoses.size == 1) {
            val pose = detectedPoses[0]
            val distToP1 = abs(pose.bodyCenterX - lastP1RawCenter)
            val distToP2 = abs(pose.bodyCenterX - lastP2RawCenter)
            if (distToP1 <= distToP2) {
                matchedP1 = pose
            } else {
                matchedP2 = pose
            }
        } else {
            // 2 or more poses: match using continuity to previous centers
            val sortedByX = detectedPoses.sortedBy { it.bodyCenterX }
            val poseA = sortedByX[0]
            val poseB = sortedByX[sortedByX.size - 1]

            // Calculate cost matrix for (poseA->P1 + poseB->P2) vs (poseA->P2 + poseB->P1)
            val costStandard = abs(poseA.bodyCenterX - lastP1RawCenter) + abs(poseB.bodyCenterX - lastP2RawCenter)
            val costSwapped = abs(poseA.bodyCenterX - lastP2RawCenter) + abs(poseB.bodyCenterX - lastP1RawCenter)

            if (costStandard <= costSwapped) {
                matchedP1 = poseA
                matchedP2 = poseB
            } else {
                matchedP1 = poseB
                matchedP2 = poseA
            }
        }

        // Process Player 1
        val p1Detected: Boolean
        if (matchedP1 != null) {
            lastP1SeenTimestamp = currentTimeMs
            p1Detected = true
            lastP1RawCenter = matchedP1.bodyCenterX

            // Calculate movement delta from calibrated baseline
            val delta = matchedP1.bodyCenterX - calibration.p1BaselineX
            val appliedDelta = if (abs(delta) < GameConfig.MOVEMENT_DEADZONE) 0f else delta

            // Target screen position: baseline 0.25 + appliedDelta * sensitivity
            val targetScreenX = (0.25f + appliedDelta * GameConfig.MOVEMENT_SENSITIVITY).coerceIn(0.06f, 0.94f)
            // Exponential smoothing
            smoothedP1ScreenX += (targetScreenX - smoothedP1ScreenX) * GameConfig.MOVEMENT_SMOOTHING
        } else {
            p1Detected = (currentTimeMs - lastP1SeenTimestamp) <= GameConfig.PLAYER_LOST_GRACE_PERIOD_MS
        }

        // Process Player 2
        val p2Detected: Boolean
        if (matchedP2 != null) {
            lastP2SeenTimestamp = currentTimeMs
            p2Detected = true
            lastP2RawCenter = matchedP2.bodyCenterX

            val delta = matchedP2.bodyCenterX - calibration.p2BaselineX
            val appliedDelta = if (abs(delta) < GameConfig.MOVEMENT_DEADZONE) 0f else delta

            // Target screen position: baseline 0.75 + appliedDelta * sensitivity
            val targetScreenX = (0.75f + appliedDelta * GameConfig.MOVEMENT_SENSITIVITY).coerceIn(0.06f, 0.94f)
            smoothedP2ScreenX += (targetScreenX - smoothedP2ScreenX) * GameConfig.MOVEMENT_SMOOTHING
        } else {
            p2Detected = (currentTimeMs - lastP2SeenTimestamp) <= GameConfig.PLAYER_LOST_GRACE_PERIOD_MS
        }

        return PlayerTrackResult(
            p1ScreenX = smoothedP1ScreenX,
            p2ScreenX = smoothedP2ScreenX,
            p1Detected = p1Detected,
            p2Detected = p2Detected,
            p1BodyCenter = lastP1RawCenter,
            p2BodyCenter = lastP2RawCenter,
            rawPosesCount = detectedPoses.size
        )
    }

    /**
     * Helper to compute bodyCenterX from shoulder and hip coordinates.
     */
    companion object {
        fun computeBodyCenter(
            leftShoulderX: Float,
            rightShoulderX: Float,
            leftHipX: Float,
            rightHipX: Float
        ): Float {
            return (leftShoulderX + rightShoulderX + leftHipX + rightHipX) / 4.0f
        }
    }
}
