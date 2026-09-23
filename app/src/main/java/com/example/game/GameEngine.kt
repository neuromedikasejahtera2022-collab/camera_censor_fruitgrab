package com.example.game

import com.example.config.GameConfig
import com.example.models.FloatingScore
import com.example.models.Fruit
import com.example.models.GameResult
import com.example.models.MatchWinner
import com.example.models.Player
import com.example.models.PlayerId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Core game loop and match state manager.
 */
class GameEngine(
    private val onSoundCatch: (Int) -> Unit = {},
    private val onSoundSpawn: () -> Unit = {},
    private val onSoundGameOver: () -> Unit = {}
) {

    private val fruitManager = FruitManager()
    private val collisionManager = CollisionManager()
    private val scope = CoroutineScope(Dispatchers.Default)
    private var gameLoopJob: Job? = null

    private val _remainingTimeMs = MutableStateFlow(GameConfig.GAME_DURATION_MS)
    val remainingTimeMs: StateFlow<Long> = _remainingTimeMs.asStateFlow()

    private val _fruits = MutableStateFlow<List<Fruit>>(emptyList())
    val fruits: StateFlow<List<Fruit>> = _fruits.asStateFlow()

    private val _floatingScores = MutableStateFlow<List<FloatingScore>>(emptyList())
    val floatingScores: StateFlow<List<FloatingScore>> = _floatingScores.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _isGameOver = MutableStateFlow(false)
    val isGameOver: StateFlow<Boolean> = _isGameOver.asStateFlow()

    private var nextScoreId = 1L
    private var lastLoopTimeMs = 0L

    var onScoreChanged: ((PlayerId, Int) -> Unit)? = null
    var onMatchFinished: ((GameResult) -> Unit)? = null

    fun startMatch(p1: Player, p2: Player) {
        stopMatch()
        _remainingTimeMs.value = GameConfig.GAME_DURATION_MS
        _fruits.value = emptyList()
        _floatingScores.value = emptyList()
        _isPaused.value = false
        _isGameOver.value = false
        fruitManager.reset()

        lastLoopTimeMs = System.currentTimeMillis()
        gameLoopJob = scope.launch {
            while (isActive && _remainingTimeMs.value > 0) {
                val now = System.currentTimeMillis()
                val deltaMs = (now - lastLoopTimeMs).coerceAtMost(100L)
                lastLoopTimeMs = now

                if (!_isPaused.value) {
                    val newRemaining = (_remainingTimeMs.value - deltaMs).coerceAtLeast(0L)
                    _remainingTimeMs.value = newRemaining

                    if (newRemaining <= 0L) {
                        endMatch(p1, p2)
                        break
                    }

                    // Progress 0.0 to 1.0
                    val progress = 1.0f - (newRemaining.toFloat() / GameConfig.GAME_DURATION_MS.toFloat())

                    // Spawning
                    if (fruitManager.shouldSpawnFruit(now, progress)) {
                        val newFruit = fruitManager.spawnFruit(now, progress)
                        _fruits.value = _fruits.value + newFruit
                        onSoundSpawn()
                    }

                    // Physics & collisions
                    tickPhysics(deltaMs / 1000f, p1, p2, now)
                }

                delay(GameConfig.TIMER_TICK_INTERVAL_MS)
            }
        }
    }

    private fun tickPhysics(deltaSeconds: Float, p1: Player, p2: Player, currentTimeMs: Long) {
        val currentFruits = _fruits.value
        val updatedFruits = mutableListOf<Fruit>()

        for (fruit in currentFruits) {
            // Update y position
            fruit.y += fruit.fallSpeed * deltaSeconds
            fruit.rotationAngle = (fruit.rotationAngle + 45f * deltaSeconds) % 360f

            // Collision check
            val catch = collisionManager.checkCatch(fruit, p1, p2)
            if (catch != null) {
                // Award points
                onScoreChanged?.invoke(catch.caughtByPlayer.id, catch.pointsAwarded)
                onSoundCatch(catch.pointsAwarded)

                // Add floating score text
                val floating = FloatingScore(
                    id = nextScoreId++,
                    text = if (catch.isZonk) "ZONK!" else "+${catch.pointsAwarded}",
                    x = fruit.x,
                    y = GameConfig.CATCH_ZONE_Y - 0.05f,
                    color = catch.fruit.type.baseColor,
                    isZonk = catch.isZonk
                )
                _floatingScores.value = _floatingScores.value + floating
            } else if (fruit.y < GameConfig.FRUIT_DESPAWN_Y && !fruit.isCaught) {
                updatedFruits.add(fruit)
            }
        }
        _fruits.value = updatedFruits

        // Clean up expired floating scores (> 1.2s old)
        _floatingScores.value = _floatingScores.value.filter { currentTimeMs - it.timestamp < 1200L }
    }

    fun pause() {
        _isPaused.value = true
    }

    fun resume() {
        lastLoopTimeMs = System.currentTimeMillis()
        _isPaused.value = false
    }

    fun togglePause() {
        if (_isPaused.value) resume() else pause()
    }

    fun endMatch(p1: Player, p2: Player) {
        _isGameOver.value = true
        _remainingTimeMs.value = 0L
        stopMatch()
        onSoundGameOver()

        val winner = when {
            p1.score > p2.score -> MatchWinner.PLAYER_1
            p2.score > p1.score -> MatchWinner.PLAYER_2
            else -> MatchWinner.DRAW
        }

        val result = GameResult(
            p1Score = p1.score,
            p2Score = p2.score,
            winner = winner
        )
        onMatchFinished?.invoke(result)
    }

    fun stopMatch() {
        gameLoopJob?.cancel()
        gameLoopJob = null
    }

    companion object {
        fun formatTimeRemaining(ms: Long): String {
            val totalSeconds = (ms + 999) / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%02d:%02d".format(minutes, seconds)
        }
    }
}
