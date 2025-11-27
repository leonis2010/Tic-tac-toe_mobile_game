// ./main/java/com/example/tictactoeapp/presentation/model/GameHistoryViewData.kt
package com.example.tictactoeapp.presentation.model

import java.util.*

data class GameHistoryViewData(
    val id: UUID? = null,
    val gameIdShort: String,
    val player1Username: String,
    val player2Username: String,
    val gameType: String,
    val resultText: String,
    val resultColorRes: Int,
    val gameDate: String,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)