// ./src/main/java/com/example/tictactoeapp/mapper/DomainToViewDataMapper.kt
package com.example.tictactoeapp.mapper

import com.example.tictactoeapp.domain.model.Game
import com.example.tictactoeapp.domain.model.GameHistory
import com.example.tictactoeapp.domain.model.Leaderboard
import com.example.tictactoeapp.presentation.model.GameHistoryViewData
import com.example.tictactoeapp.presentation.model.GameItemViewData
import com.example.tictactoeapp.presentation.model.LeaderboardViewData
import javax.inject.Inject

class DomainToViewDataMapper @Inject constructor() {

    /**
     * Преобразовать доменную модель Game в модель представления GameItemViewData для списка игр.
     * @param game Доменная модель игры.
     * @return GameItemViewData для отображения в RecyclerView.
     */
    fun mapGameToGameItemViewData(game: Game): GameItemViewData {
        return GameItemViewData(
            id = game.id ?: java.util.UUID.randomUUID(), // <-- если id null
            creatorUsername = game.creatorUsername ?: "Unknown" // <-- если creatorUsername null
        )
    }

    fun mapGameHistoryToViewData(history: GameHistory): GameHistoryViewData {
        val dateFormatter = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())

        return GameHistoryViewData(
            id = history.id,
            gameIdShort = history.id?.toString()?.take(8) ?: "UNKNOWN",
            player1Username = history.player1Username,
            player2Username = history.player2Username,
            gameType = if (history.gameType == "PVE") "Против компьютера" else "Против игрока",
            resultText = history.getResultText(),
            resultColorRes = history.getResultColorRes(),
            gameDate = dateFormatter.format(history.gameDate)
        )
    }

    fun mapLeaderboardToViewData(leaderboard: Leaderboard, position: Int): LeaderboardViewData {
        return LeaderboardViewData(
            username = leaderboard.username,
            position = position,
            gamesPlayed = leaderboard.gamesPlayed,
            gamesWon = leaderboard.gamesWon,
            gamesLost = leaderboard.gamesLost,
            gamesDrawn = leaderboard.gamesDrawn,
            winRate = leaderboard.getWinRateFormatted(),
            gamesStats = leaderboard.getGamesStats(),
            rating = leaderboard.rating
        )
    }
}