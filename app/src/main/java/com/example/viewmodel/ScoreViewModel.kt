package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.HighScoreRepository
import com.example.data.ScoreEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ScoreViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HighScoreRepository(AppDatabase.getDatabase(application).scoreDao())

    val allScores: StateFlow<List<ScoreEntity>> = repository.getAllScores()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bestScore: StateFlow<ScoreEntity?> = repository.getBestScore()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val todayBestScore: StateFlow<ScoreEntity?> = repository.getTodayBestScore()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
