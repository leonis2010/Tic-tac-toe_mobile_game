package com.example.tictactoeapp.mapper

import com.example.tictactoeapp.domain.model.User
import com.example.tictactoeapp.presentation.model.LoginViewData
import com.example.tictactoeapp.presentation.model.RegisterViewData

class ViewDataToDomainMapper {

    fun mapLoginViewDataToDomain(loginViewData: LoginViewData): User {
        return User(
            username = loginViewData.username,
            password = loginViewData.password
        )
    }

    fun mapRegisterViewDataToDomain(registerViewData: RegisterViewData): User {
        return User(
            username = registerViewData.username,
            password = registerViewData.password
        )
    }
}