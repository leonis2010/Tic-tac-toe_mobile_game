package com.example.tictactoeapp.domain.repository

import com.example.tictactoeapp.domain.model.User
import io.reactivex.Single

interface AuthRepository {
    fun login(username: String, password: String): Single<User>
    fun register(username: String, password: String): Single<User>
    fun refreshToken(): Single<User>
    fun logout(): Single<Boolean>
    fun validateCredentials(username: String, password: String): Boolean
    fun clear()
}