// ./main/java/com/example/tictactoeapp/data/model/GameHistoryDto.kt
package com.example.tictactoeapp.data.model

import com.google.gson.annotations.SerializedName
import java.util.*

data class GameHistoryDto(
    @SerializedName("id")
    val id: UUID? = null,

    @SerializedName("player1Username")
    val player1Username: String? = null,

    @SerializedName("player2Username")
    val player2Username: String? = null,

    @SerializedName("gameType")
    val gameType: String? = null,

    @SerializedName("result")
    val result: String? = null, // "WIN", "LOSE", "DRAW"

    @SerializedName("gameDate")
    val gameDate: Date? = null,

    @SerializedName("winner")
    val winner: String? = null,

    @SerializedName("currentUserUsername")
    val currentUserUsername: String? = null
)