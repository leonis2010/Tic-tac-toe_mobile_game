// ./src/main/java/com/example/tictactoeapp/domain/model/Game.kt
package com.example.tictactoeapp.domain.model

import java.util.*
data class Game(
    val id: UUID? = null,
    val board: GameBoard,
    var isPlayerTurn: Boolean = true,
    var status: String = "IN_PROGRESS",
    val gameType: String = "PVP", // "PVP" или "PVE"
    val creatorUsername: String? = null,
    val player2Username: String? = null,
    val currentPlayerUsername: String? = null,
    val winner: String? = null
)