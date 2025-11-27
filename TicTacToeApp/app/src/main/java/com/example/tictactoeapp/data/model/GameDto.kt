// ./src/main/java/com/example/tictactoeapp/data/model/GameDto.kt
package com.example.tictactoeapp.data.model

import com.google.gson.annotations.SerializedName
import java.util.*

data class GameDto(
    @SerializedName("id") val id: UUID? = null,
    @SerializedName("board") val board: GameBoardDto? = null,
    @SerializedName("playerTurn") val isPlayerTurn: Boolean = true,
    @SerializedName("status") val status: String = "IN_PROGRESS",
    @SerializedName("gameType") val gameType: String? = "PVP", // "PVP" или "PVE"
    @SerializedName("creatorUsername") val creatorUsername: String? = null,
    @SerializedName("player2Username") val player2Username: String? = null,
    @SerializedName("currentPlayerUsername") val currentPlayerUsername: String? = null,
    @SerializedName("winner") val winner: String? = null
)