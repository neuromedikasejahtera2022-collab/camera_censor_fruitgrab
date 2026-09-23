package com.example.data

import com.example.models.MatchWinner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Calendar

class HighScoreRepository(private val scoreDao: ScoreDao) {

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        scope.launch {
            val existing = scoreDao.getAllScoresOrdered().firstOrNull()
            if (existing.isNullOrEmpty()) {
                // Populate default high scores for initial arcade feel
                val now = System.currentTimeMillis()
                scoreDao.insertScore(ScoreEntity(playerName = "Player 1", score = 3800, opponentScore = 3100, isWinner = true, timestamp = now - 3600_000 * 2))
                scoreDao.insertScore(ScoreEntity(playerName = "Player 2", score = 3100, opponentScore = 3800, isWinner = false, timestamp = now - 3600_000 * 2))
                scoreDao.insertScore(ScoreEntity(playerName = "Player 1", score = 2900, opponentScore = 2400, isWinner = true, timestamp = now - 3600_000 * 5))
                scoreDao.insertScore(ScoreEntity(playerName = "Player 2", score = 2700, opponentScore = 2200, isWinner = true, timestamp = now - 3600_000 * 8))
            }
        }
    }

    fun getAllScores(): Flow<List<ScoreEntity>> = scoreDao.getAllScoresOrdered()

    fun getBestScore(): Flow<ScoreEntity?> = scoreDao.getBestScore()

    fun getTodayBestScore(): Flow<ScoreEntity?> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return scoreDao.getTodayBestScore(calendar.timeInMillis)
    }

    suspend fun saveGameResult(p1Score: Int, p2Score: Int, winner: MatchWinner, selfieUri: String?) {
        val now = System.currentTimeMillis()
        val p1Winner = winner == MatchWinner.PLAYER_1
        val p2Winner = winner == MatchWinner.PLAYER_2

        scoreDao.insertScore(
            ScoreEntity(
                playerName = "Player 1",
                score = p1Score,
                opponentScore = p2Score,
                isWinner = p1Winner,
                timestamp = now,
                selfieUri = selfieUri
            )
        )
        scoreDao.insertScore(
            ScoreEntity(
                playerName = "Player 2",
                score = p2Score,
                opponentScore = p1Score,
                isWinner = p2Winner,
                timestamp = now,
                selfieUri = selfieUri
            )
        )
    }
}
