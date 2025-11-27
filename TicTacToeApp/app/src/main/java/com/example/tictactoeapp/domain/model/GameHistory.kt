// ./main/java/com/example/tictactoeapp/domain/model/GameHistory.kt
package com.example.tictactoeapp.domain.model

import java.util.*

data class GameHistory(
    val id: UUID? = null,
    val player1Username: String,
    val player2Username: String,
    val gameType: String, // "PVP" или "PVE"
    val result: String, // "WIN", "LOSE", "DRAW"
    val gameDate: Date,
    val winner: String? = null,
    val currentUserUsername: String
) {
    fun getResultText(): String {
        return when (result) {
            "WIN" -> "Победа"
            "LOSE" -> "Поражение"
            "DRAW" -> "Ничья"
            else -> "Неизвестно"
        }
    }

    fun getResultColorRes(): Int {
        return when (result) {
            "WIN" -> android.R.color.holo_green_light
            "LOSE" -> android.R.color.holo_red_light
            "DRAW" -> android.R.color.darker_gray
            else -> android.R.color.black
        }
    }
}