package com.example.sensor.face

import com.example.config.GameConfig

data class DetectedFaceResult(
    val normalizedX: Float, // 0..1 from left to right (already mirrored)
    val smilingProbability: Float
)

data class FaceSmileState(
    val p1FaceDetected: Boolean = false,
    val p2FaceDetected: Boolean = false,
    val p1SmileScore: Float = 0f,
    val p2SmileScore: Float = 0f,
    val isBothSmiling: Boolean = false,
    val consecutiveSmileFrames: Int = 0,
    val countdownNumber: Int = 3,
    val shouldCaptureSelfie: Boolean = false
)

/**
 * Detects dual faces and smiles for simultaneous selfie capture trigger.
 */
class FaceSmileDetector {

    private var consecutiveFramesBothSmiling = 0
    private var countdownValue = 3
    private var lastCountdownTime = 0L
    private var capturedAlready = false

    var state: FaceSmileState = FaceSmileState()
        private set

    fun reset() {
        consecutiveFramesBothSmiling = 0
        countdownValue = 3
        lastCountdownTime = 0L
        capturedAlready = false
        state = FaceSmileState()
    }

    /**
     * Process list of detected faces. Matches left face to Player 1, right face to Player 2.
     */
    fun processFaces(
        faces: List<DetectedFaceResult>,
        currentTimeMs: Long = System.currentTimeMillis()
    ): FaceSmileState {
        if (capturedAlready) {
            return state.copy(shouldCaptureSelfie = false)
        }

        if (faces.size < 2) {
            consecutiveFramesBothSmiling = 0
            countdownValue = 3
            lastCountdownTime = 0L
            val p1Face = faces.firstOrNull()
            state = FaceSmileState(
                p1FaceDetected = p1Face != null,
                p2FaceDetected = false,
                p1SmileScore = p1Face?.smilingProbability ?: 0f,
                p2SmileScore = 0f,
                isBothSmiling = false,
                consecutiveSmileFrames = 0,
                countdownNumber = 3,
                shouldCaptureSelfie = false
            )
            return state
        }

        // Two or more faces. Sort by X (left to right)
        val sortedFaces = faces.sortedBy { it.normalizedX }
        val p1Face = sortedFaces.first()
        val p2Face = sortedFaces.last()

        val p1Smiling = p1Face.smilingProbability >= GameConfig.SMILE_THRESHOLD
        val p2Smiling = p2Face.smilingProbability >= GameConfig.SMILE_THRESHOLD
        val bothSmiling = p1Smiling && p2Smiling

        if (bothSmiling) {
            consecutiveFramesBothSmiling++
        } else {
            consecutiveFramesBothSmiling = 0
            countdownValue = 3
            lastCountdownTime = 0L
        }

        var shouldCapture = false
        if (consecutiveFramesBothSmiling >= GameConfig.SMILE_CONSECUTIVE_FRAMES) {
            if (lastCountdownTime == 0L) {
                lastCountdownTime = currentTimeMs
                countdownValue = 3
            } else if (currentTimeMs - lastCountdownTime >= 850L) {
                countdownValue--
                lastCountdownTime = currentTimeMs
            }

            if (countdownValue <= 0) {
                shouldCapture = true
                capturedAlready = true
            }
        }

        state = FaceSmileState(
            p1FaceDetected = true,
            p2FaceDetected = true,
            p1SmileScore = p1Face.smilingProbability,
            p2SmileScore = p2Face.smilingProbability,
            isBothSmiling = bothSmiling,
            consecutiveSmileFrames = consecutiveFramesBothSmiling,
            countdownNumber = countdownValue,
            shouldCaptureSelfie = shouldCapture
        )

        return state
    }

    /**
     * Simulation method for debug mode.
     */
    fun simulateSmile(p1Smile: Float, p2Smile: Float, currentTimeMs: Long = System.currentTimeMillis()): FaceSmileState {
        val faces = listOf(
            DetectedFaceResult(0.25f, p1Smile),
            DetectedFaceResult(0.75f, p2Smile)
        )
        return processFaces(faces, currentTimeMs)
    }
}
