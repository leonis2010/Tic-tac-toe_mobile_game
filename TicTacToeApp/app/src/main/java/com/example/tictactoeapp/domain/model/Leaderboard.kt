// ./main/java/com/example/tictactoeapp/domain/model/Leaderboard.kt
package com.example.tictactoeapp.domain.model

data class Leaderboard(
    val username: String,
    val gamesPlayed: Int,
    val gamesWon: Int,
    val gamesLost: Int,
    val gamesDrawn: Int,
    val winRate: Double,
    val rating: Int
) {
    fun getWinRateFormatted(): String {
        return String.format("%.1f%%", winRate)
    }

    fun getGamesStats(): String {
        return "$gamesWon/$gamesLost/$gamesDrawn"
    }
}