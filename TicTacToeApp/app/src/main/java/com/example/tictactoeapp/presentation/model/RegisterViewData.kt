package com.example.tictactoeapp.presentation.model

data class RegisterViewData(
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)