package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "high_scores")
data class ScoreEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val playerName: String,
    val score: Int,
    val opponentScore: Int,
    val isWinner: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val selfieUri: String? = null
)
