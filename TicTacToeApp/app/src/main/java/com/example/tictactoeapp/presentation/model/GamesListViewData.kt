// ./src/main/java/com/example/tictactoeapp/presentation/model/GamesListViewData.kt
package com.example.tictactoeapp.presentation.model

import java.util.UUID

data class GamesListViewData(
    val games: List<GameItemViewData> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val joinSuccess: Boolean = false,
    val joinedGameId: UUID? = null
)

// ViewData для отдельного элемента списка
data class GameItemViewData(
    val id: UUID,
    val creatorUsername: String
)
