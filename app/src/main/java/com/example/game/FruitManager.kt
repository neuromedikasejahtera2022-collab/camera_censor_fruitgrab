package com.example.game

import com.example.config.GameConfig
import com.example.models.Fruit
import com.example.models.FruitType
import kotlin.random.Random

/**
 * Manages fruit spawning, types, and speed scaling.
 */
class FruitManager {

    private var nextFruitId = 1L
    private var lastSpawnTime = 0L

    fun shouldSpawnFruit(currentTimeMs: Long, gameProgress: Float): Boolean {
        // gameProgress goes from 0.0 (start) to 1.0 (end)
        // Spawn interval gets faster gradually as match progresses
        val currentInterval = (GameConfig.MAX_SPAWN_INTERVAL_MS - (gameProgress * (GameConfig.MAX_SPAWN_INTERVAL_MS - GameConfig.MIN_SPAWN_INTERVAL_MS))).toLong()
        return (currentTimeMs - lastSpawnTime) >= currentInterval
    }

    fun spawnFruit(currentTimeMs: Long, gameProgress: Float): Fruit {
        lastSpawnTime = currentTimeMs
        val type = FruitType.selectWeightedRandom(Random.nextInt(100))

        // Horizontal spawn position between 0.10 and 0.90 to keep inside playable bounds
        val spawnX = Random.nextFloat() * 0.80f + 0.10f

        // Fall speed scales gradually with game difficulty
        val baseSpeed = GameConfig.MIN_FALL_SPEED + Random.nextFloat() * (GameConfig.MAX_FALL_SPEED - GameConfig.MIN_FALL_SPEED)
        val scaledSpeed = baseSpeed * (1.0f + gameProgress * GameConfig.DIFFICULTY_SCALING_FACTOR)

        val fruit = Fruit(
            id = nextFruitId++,
            type = type,
            x = spawnX,
            y = -0.05f, // Starts just above screen
            fallSpeed = scaledSpeed,
            rotationAngle = Random.nextFloat() * 360f
        )
        return fruit
    }

    fun reset() {
        nextFruitId = 1L
        lastSpawnTime = 0L
    }
}
