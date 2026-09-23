package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDao {

    @Query("SELECT * FROM high_scores ORDER BY score DESC LIMIT 50")
    fun getAllScoresOrdered(): Flow<List<ScoreEntity>>

    @Query("SELECT * FROM high_scores ORDER BY score DESC LIMIT 1")
    fun getBestScore(): Flow<ScoreEntity?>

    @Query("SELECT * FROM high_scores WHERE timestamp >= :sinceTimestamp ORDER BY score DESC LIMIT 1")
    fun getTodayBestScore(sinceTimestamp: Long): Flow<ScoreEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: ScoreEntity): Long

    @Query("DELETE FROM high_scores")
    suspend fun clearAll()
}
