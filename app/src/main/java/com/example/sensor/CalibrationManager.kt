package com.example.sensor

import com.example.config.GameConfig
import com.example.models.CalibrationState
import com.example.models.CalibrationStatus
import com.example.models.PlayerCalibration
import com.example.sensor.pose.RawBodyPose
import kotlin.math.abs

/**
 * Manages the multi-player calibration state machine.
 */
class CalibrationManager {

    private var stableFrames = 0
    private var countdownValue = 3
    private var lastCountdownTime = 0L

    private var candidateP1Baseline = 0.25f
    private var candidateP2Baseline = 0.75f

    var currentState: CalibrationState = CalibrationState()
        private set

    fun reset() {
        stableFrames = 0
        countdownValue = 3
        lastCountdownTime = 0L
        currentState = CalibrationState(
            status = CalibrationStatus.WAITING_FOR_2_PLAYERS,
            instructionText = "Stand side-by-side inside the camera area"
        )
    }

    /**
     * Feeds detected body poses into the calibration engine.
     * Returns true if calibration has just finished successfully.
     */
    fun processFrame(
        poses: List<RawBodyPose>,
        currentTimeMs: Long = System.currentTimeMillis()
    ): Pair<CalibrationState, PlayerCalibration?> {
        if (currentState.status == CalibrationStatus.CALIBRATION_COMPLETE) {
            return Pair(
                currentState,
                PlayerCalibration(candidateP1Baseline, candidateP2Baseline, isCalibrated = true)
            )
        }

        if (poses.size < 2) {
            stableFrames = 0
            currentState = CalibrationState(
                status = CalibrationStatus.WAITING_FOR_2_PLAYERS,
                p1Detected = poses.isNotEmpty(),
                p2Detected = false,
                instructionText = if (poses.size == 1) "Need 2 players! Second player please step into the frame." else "Stand side-by-side inside the camera area"
            )
            return Pair(currentState, null)
        }

        // Two or more poses detected. Sort by X to determine left vs right player
        val sortedPoses = poses.sortedBy { it.bodyCenterX }
        val leftPose = sortedPoses.first()
        val rightPose = sortedPoses.last()

        val separation = rightPose.bodyCenterX - leftPose.bodyCenterX
        if (separation < 0.18f) {
            stableFrames = 0
            currentState = CalibrationState(
                status = CalibrationStatus.PLEASE_STAND_SIDE_BY_SIDE,
                p1Detected = true,
                p2Detected = true,
                instructionText = "Please stand side-by-side with some space in between"
            )
            return Pair(currentState, null)
        }

        // Both players are clearly placed side-by-side
        stableFrames++
        candidateP1Baseline = leftPose.bodyCenterX
        candidateP2Baseline = rightPose.bodyCenterX

        if (stableFrames < 10) {
            currentState = CalibrationState(
                status = CalibrationStatus.HOLD_STILL,
                p1Detected = true,
                p2Detected = true,
                stableFramesCount = stableFrames,
                instructionText = "Players detected! Hold still..."
            )
            return Pair(currentState, null)
        }

        // Countdown phase
        if (lastCountdownTime == 0L) {
            lastCountdownTime = currentTimeMs
            countdownValue = 3
        } else if (currentTimeMs - lastCountdownTime >= 800L) {
            countdownValue--
            lastCountdownTime = currentTimeMs
        }

        if (countdownValue > 0) {
            currentState = CalibrationState(
                status = CalibrationStatus.COUNTDOWN,
                p1Detected = true,
                p2Detected = true,
                countdownNumber = countdownValue,
                stableFramesCount = stableFrames,
                instructionText = "Calibrating in $countdownValue..."
            )
            return Pair(currentState, null)
        }

        // Calibration complete!
        currentState = CalibrationState(
            status = CalibrationStatus.CALIBRATION_COMPLETE,
            p1Detected = true,
            p2Detected = true,
            countdownNumber = 0,
            instructionText = "Calibration complete!"
        )

        val calibration = PlayerCalibration(
            p1BaselineX = candidateP1Baseline,
            p2BaselineX = candidateP2Baseline,
            isCalibrated = true
        )

        return Pair(currentState, calibration)
    }

    fun forceCalibration(p1X: Float = 0.25f, p2X: Float = 0.75f): PlayerCalibration {
        candidateP1Baseline = p1X
        candidateP2Baseline = p2X
        currentState = CalibrationState(
            status = CalibrationStatus.CALIBRATION_COMPLETE,
            p1Detected = true,
            p2Detected = true,
            instructionText = "Calibration complete!"
        )
        return PlayerCalibration(p1X, p2X, isCalibrated = true)
    }
}
