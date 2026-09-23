package com.example

import com.example.config.GameConfig
import com.example.game.CollisionManager
import com.example.game.FruitManager
import com.example.game.GameEngine
import com.example.models.CalibrationStatus
import com.example.models.Fruit
import com.example.models.FruitType
import com.example.models.MatchWinner
import com.example.models.Player
import com.example.models.PlayerCalibration
import com.example.models.PlayerId
import com.example.sensor.CalibrationManager
import com.example.sensor.hand.HandGestureDetector
import com.example.sensor.hand.RawHandLandmarks
import com.example.sensor.pose.PoseTracker
import com.example.sensor.pose.RawBodyPose
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FruitCatcherGameTest {

    @Test
    fun testFruitProbabilitySelection() {
        // According to weights: 0..49 = Apple, 50..74 = Banana, 75..87 = Orange, 88..94 = Watermelon, 95 = Golden, 96..99 = Zonk
        assertEquals(FruitType.APPLE_100, FruitType.selectWeightedRandom(0))
        assertEquals(FruitType.APPLE_100, FruitType.selectWeightedRandom(49))
        assertEquals(FruitType.BANANA_200, FruitType.selectWeightedRandom(50))
        assertEquals(FruitType.BANANA_200, FruitType.selectWeightedRandom(74))
        assertEquals(FruitType.ORANGE_300, FruitType.selectWeightedRandom(75))
        assertEquals(FruitType.ORANGE_300, FruitType.selectWeightedRandom(87))
        assertEquals(FruitType.WATERMELON_400, FruitType.selectWeightedRandom(88))
        assertEquals(FruitType.WATERMELON_400, FruitType.selectWeightedRandom(94))
        assertEquals(FruitType.GOLDEN_FRUIT_500, FruitType.selectWeightedRandom(95))
        assertEquals(FruitType.ROTTEN_FRUIT_ZONK, FruitType.selectWeightedRandom(96))
        assertEquals(FruitType.ROTTEN_FRUIT_ZONK, FruitType.selectWeightedRandom(99))
    }

    @Test
    fun testScoreCalculation() {
        var p1Score = 0
        p1Score += FruitType.APPLE_100.points
        assertEquals(100, p1Score)

        p1Score += FruitType.GOLDEN_FRUIT_500.points
        assertEquals(600, p1Score)

        p1Score = (p1Score + FruitType.ROTTEN_FRUIT_ZONK.points).coerceAtLeast(0)
        assertEquals(450, p1Score)
    }

    @Test
    fun testCollisionDetectionSuccess() {
        val collisionManager = CollisionManager()
        val p1 = Player(PlayerId.PLAYER_1, currentX = 0.30f)
        val p2 = Player(PlayerId.PLAYER_2, currentX = 0.70f)

        // Fruit at X=0.31, inside catch zone Y=0.82
        val fruit = Fruit(
            id = 1L,
            type = FruitType.APPLE_100,
            x = 0.31f,
            y = GameConfig.CATCH_ZONE_Y,
            fallSpeed = 0.3f
        )

        val result = collisionManager.checkCatch(fruit, p1, p2)
        assertNotNull(result)
        assertEquals(PlayerId.PLAYER_1, result?.caughtByPlayer?.id)
        assertEquals(100, result?.pointsAwarded)
        assertTrue(fruit.isCaught)
    }

    @Test
    fun testCollisionMissWhenOutsideZone() {
        val collisionManager = CollisionManager()
        val p1 = Player(PlayerId.PLAYER_1, currentX = 0.30f)
        val p2 = Player(PlayerId.PLAYER_2, currentX = 0.70f)

        // Fruit too high (Y=0.50)
        val fruitHigh = Fruit(
            id = 2L,
            type = FruitType.BANANA_200,
            x = 0.30f,
            y = 0.50f,
            fallSpeed = 0.3f
        )

        val resultHigh = collisionManager.checkCatch(fruitHigh, p1, p2)
        assertNull(resultHigh)

        // Fruit in Y catch zone but too far horizontally
        val fruitFar = Fruit(
            id = 3L,
            type = FruitType.ORANGE_300,
            x = 0.50f, // midway between 0.30 and 0.70 (> CATCH_DISTANCE away)
            y = GameConfig.CATCH_ZONE_Y,
            fallSpeed = 0.3f
        )

        val resultFar = collisionManager.checkCatch(fruitFar, p1, p2)
        assertNull(resultFar)
    }

    @Test
    fun testWinnerCalculation() {
        fun decideWinner(p1: Int, p2: Int): MatchWinner {
            return when {
                p1 > p2 -> MatchWinner.PLAYER_1
                p2 > p1 -> MatchWinner.PLAYER_2
                else -> MatchWinner.DRAW
            }
        }

        assertEquals(MatchWinner.PLAYER_1, decideWinner(1500, 1200))
        assertEquals(MatchWinner.PLAYER_2, decideWinner(800, 1100))
        assertEquals(MatchWinner.DRAW, decideWinner(2000, 2000))
    }

    @Test
    fun testTimerFormatting() {
        assertEquals("01:00", GameEngine.formatTimeRemaining(60_000L))
        assertEquals("00:42", GameEngine.formatTimeRemaining(42_000L))
        assertEquals("00:09", GameEngine.formatTimeRemaining(9_000L))
        assertEquals("00:00", GameEngine.formatTimeRemaining(0L))
    }

    @Test
    fun testPlayerMovementNormalization() {
        val bodyCenter = PoseTracker.computeBodyCenter(
            leftShoulderX = 0.40f,
            rightShoulderX = 0.60f,
            leftHipX = 0.42f,
            rightHipX = 0.58f
        )
        assertEquals(0.50f, bodyCenter, 0.001f)
    }

    @Test
    fun testCalibrationValidationRequiresTwoPlayers() {
        val calibrationManager = CalibrationManager()
        calibrationManager.reset()

        // Single pose should keep waiting for 2 players
        val singlePose = listOf(
            RawBodyPose(bodyCenterX = 0.30f, leftShoulderX = 0.2f, rightShoulderX = 0.4f, leftHipX = 0.25f, rightHipX = 0.35f)
        )
        val (state, calib) = calibrationManager.processFrame(singlePose)
        assertNull(calib)
        assertEquals(CalibrationStatus.WAITING_FOR_2_PLAYERS, state.status)
        assertTrue(state.p1Detected)
        assertFalse(state.p2Detected)
    }

    @Test
    fun testGestureClassification() {
        val gestureDetector = HandGestureDetector()

        // Open palm
        val openHand = RawHandLandmarks(palmCenterX = 0.5f, palmCenterY = 0.5f, isFingersFolded = false)
        val stateOpen = gestureDetector.processHand(openHand, currentTimeMs = 1000L)
        assertTrue(stateOpen.isOpenPalm)
        assertFalse(stateOpen.isFist)
        assertEquals(0L, stateOpen.clickEventTrigger)

        // Fist (rising edge triggers click)
        val fistHand = RawHandLandmarks(palmCenterX = 0.5f, palmCenterY = 0.5f, isFingersFolded = true)
        val stateFist = gestureDetector.processHand(fistHand, currentTimeMs = 1100L)
        assertTrue(stateFist.isFist)
        assertFalse(stateFist.isOpenPalm)
        assertEquals(1100L, stateFist.clickEventTrigger)

        // Sustained fist within debounce period does not trigger second click
        val stateFistAgain = gestureDetector.processHand(fistHand, currentTimeMs = 1200L)
        assertEquals(0L, stateFistAgain.clickEventTrigger)
    }
}
