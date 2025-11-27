// ./main/java/com/example/tictactoeapp/presentation/model/CurrentGameViewData.kt
package com.example.tictactoeapp.presentation.model

import java.util.*

data class CurrentGameViewData(
    val gameId: UUID? = null,
    val currentPlayerId: UUID? = null,
    val player1Username: String = "Player 1",
    val player2Username: String = "Player 2",
    val player1Symbol: String = "X",
    val player2Symbol: String = "O",
    val currentPlayerUsername: String? = null,
    val board: Array<IntArray> = Array(3) { IntArray(3) },
    val gameStatus: String = GameStatus.WAITING_FOR_PLAYERS,
    val winner: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val updateSuccess: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CurrentGameViewData

        if (gameId != other.gameId) return false
        if (currentPlayerId != other.currentPlayerId) return false
        if (player1Username != other.player1Username) return false
        if (player2Username != other.player2Username) return false
        if (player1Symbol != other.player1Symbol) return false
        if (player2Symbol != other.player2Symbol) return false
        if (currentPlayerUsername != other.currentPlayerUsername) return false
        if (!board.contentDeepEquals(other.board)) return false
        if (gameStatus != other.gameStatus) return false
        if (winner != other.winner) return false
        if (isLoading != other.isLoading) return false
        if (errorMessage != other.errorMessage) return false
        if (updateSuccess != other.updateSuccess) return false

        return true
    }

    override fun hashCode(): Int {
        var result = gameId?.hashCode() ?: 0
        result = 31 * result + (currentPlayerId?.hashCode() ?: 0)
        result = 31 * result + player1Username.hashCode()
        result = 31 * result + player2Username.hashCode()
        result = 31 * result + player1Symbol.hashCode()
        result = 31 * result + player2Symbol.hashCode()
        result = 31 * result + (currentPlayerUsername?.hashCode() ?: 0)
        result = 31 * result + board.contentDeepHashCode()
        result = 31 * result + gameStatus.hashCode()
        result = 31 * result + (winner?.hashCode() ?: 0)
        result = 31 * result + isLoading.hashCode()
        result = 31 * result + (errorMessage?.hashCode() ?: 0)
        result = 31 * result + updateSuccess.hashCode()
        return result
    }
}