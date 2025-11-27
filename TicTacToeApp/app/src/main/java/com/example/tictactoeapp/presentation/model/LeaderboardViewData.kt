// ./main/java/com/example/tictactoeapp/presentation/model/LeaderboardViewData.kt
package com.example.tictactoeapp.presentation.model

data class LeaderboardViewData(
    val username: String,
    val position: Int,
    val gamesPlayed: Int,
    val gamesWon: Int,
    val gamesLost: Int,
    val gamesDrawn: Int,
    val winRate: String,
    val gamesStats: String,
    val rating: Int,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)