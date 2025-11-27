package com.example.tictactoeapp.domain.repository

import com.example.tictactoeapp.domain.model.User
import io.reactivex.Maybe
import io.reactivex.Completable

interface UserRepository {
    fun getCurrentUser(): Maybe<User>
    fun saveCurrentUser(user: User, authToken: String): Completable
    fun clearCurrentUser(): Completable
    fun clearAllData(): Completable
}