package com.example.tictactoeapp.mapper

import com.example.tictactoeapp.data.database.entity.GameEntity
import com.example.tictactoeapp.data.database.entity.UserEntity
import com.example.tictactoeapp.data.database.entity.CurrentUserEntity
import com.example.tictactoeapp.domain.model.Game
import com.example.tictactoeapp.domain.model.User
import com.google.gson.Gson

class DomainToEntityMapper {
    private val gson = Gson()

    fun mapGameToEntity(game: Game?): GameEntity? {
        if (game?.id == null || game.board.board == null) return null

        val boardData = gson.toJson(game.board.board)

        return GameEntity(
            id = game.id,
            boardData = boardData,
            isPlayerTurn = game.isPlayerTurn,
            status = game.status
        )
    }

    fun mapUserToEntity(user: User?): UserEntity? {
        if (user?.id == null) return null

        return UserEntity(
            id = user.id,
            username = user.username,
            password = user.password
        )
    }

    fun mapUserToCurrentUserEntity(user: User?, authToken: String?): CurrentUserEntity? {
        if (user?.id == null) return null

        return CurrentUserEntity(
            userId = user.id,
            username = user.username,
            authToken = authToken
        )
    }
}