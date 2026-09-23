package com.example.game

import com.example.config.GameConfig
import com.example.models.Fruit
import com.example.models.Player
import com.example.models.PlayerId
import kotlin.math.abs

data class CatchResult(
    val fruit: Fruit,
    val caughtByPlayer: Player,
    val pointsAwarded: Int,
    val isZonk: Boolean
)

/**
 * Handles catch-zone collisions between falling fruits and the two players' baskets.
 */
class CollisionManager {

    /**
     * Checks if a fruit in the catch zone is captured by Player 1 or Player 2.
     * Returns CatchResult if caught, null otherwise.
     */
    fun checkCatch(fruit: Fruit, p1: Player, p2: Player): CatchResult? {
        if (fruit.isCaught) return null

        // Check vertical catch zone window
        val isInCatchZone = fruit.y in (GameConfig.CATCH_ZONE_Y - 0.04f)..(GameConfig.CATCH_ZONE_Y + 0.06f)
        if (!isInCatchZone) return null

        val distToP1 = abs(fruit.x - p1.currentX)
        val distToP2 = abs(fruit.x - p2.currentX)

        val p1InRange = distToP1 <= GameConfig.CATCH_DISTANCE
        val p2InRange = distToP2 <= GameConfig.CATCH_DISTANCE

        if (!p1InRange && !p2InRange) return null

        // Determine closest player
        val winnerPlayer = when {
            p1InRange && !p2InRange -> p1
            !p1InRange && p2InRange -> p2
            else -> if (distToP1 <= distToP2) p1 else p2
        }

        fruit.isCaught = true
        fruit.caughtBy = winnerPlayer.id

        return CatchResult(
            fruit = fruit,
            caughtByPlayer = winnerPlayer,
            pointsAwarded = fruit.type.points,
            isZonk = fruit.type.isZonk
        )
    }
}
