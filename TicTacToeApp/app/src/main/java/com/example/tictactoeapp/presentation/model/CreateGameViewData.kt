// ./src/main/java/com/example/tictactoeapp/presentation/model/CreateGameViewData.kt
package com.example.tictactoeapp.presentation.model

import java.util.*

data class CreateGameViewData(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    // Флаг для сигнала успешного создания
    val createSuccess: Boolean = false,
    // ID созданной игры (для навигации)
    val createdGameId: UUID? = null,
    // Тип созданной игры
    val gameType: String? = null // Например, "COMPUTER" или "PLAYER"
)
