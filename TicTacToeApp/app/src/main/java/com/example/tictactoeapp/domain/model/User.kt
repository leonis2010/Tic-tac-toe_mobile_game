package com.example.tictactoeapp.domain.model

data class User(
    val id: Long? = null,
    val username: String,
    val password: String = "", // Не храним пароль в доменной модели после аутентификации
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val expiresIn: Long? = null
)