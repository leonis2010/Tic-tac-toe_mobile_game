// ./main/java/com/example/tictactoeapp/data/model/LeaderboardDto.kt
package com.example.tictactoeapp.data.model

import com.google.gson.annotations.SerializedName

data class LeaderboardDto(
    @SerializedName("username")
    val username: String? = null,

    @SerializedName("gamesPlayed")
    val gamesPlayed: Int = 0,

    @SerializedName("gamesWon")
    val gamesWon: Int = 0,

    @SerializedName("gamesLost")
    val gamesLost: Int = 0,

    @SerializedName("gamesDrawn")
    val gamesDrawn: Int = 0,

    @SerializedName("winRate")
    val winRate: Double = 0.0,

    @SerializedName("rating")
    val rating: Int = 0
)