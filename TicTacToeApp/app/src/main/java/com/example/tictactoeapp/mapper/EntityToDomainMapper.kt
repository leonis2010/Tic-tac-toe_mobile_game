package com.example.tictactoeapp.mapper

import com.example.tictactoeapp.data.database.entity.GameEntity
import com.example.tictactoeapp.data.database.entity.UserEntity
import com.example.tictactoeapp.data.database.entity.CurrentUserEntity
import com.example.tictactoeapp.domain.model.Game
import com.example.tictactoeapp.domain.model.GameBoard
import com.example.tictactoeapp.domain.model.User
import com.google.gson.Gson

class EntityToDomainMapper {
    private val gson = Gson()

    fun mapGameEntityToDomain(gameEntity: GameEntity?): Game? {
        if (gameEntity == null) {
            println("DEBUG: EntityToDomainMapper - gameEntity is null")
            return null
        }

        println("DEBUG: EntityToDomainMapper - mapping gameEntity: ${gameEntity.id}")
        println("DEBUG: EntityToDomainMapper - boardData: ${gameEntity.boardData}")

        val boardArray = try {
            gson.fromJson(gameEntity.boardData, Array<IntArray>::class.java)
        } catch (e: Exception) {
            println("DEBUG: EntityToDomainMapper - Error parsing boardData: ${e.message}")
            Array(3) { IntArray(3) }
        }

        println("DEBUG: EntityToDomainMapper - parsed board: ${boardArray.contentDeepToString()}")

        val gameBoard = GameBoard(boardArray)

        return Game(
            id = gameEntity.id,
            board = gameBoard,
            isPlayerTurn = gameEntity.isPlayerTurn,
            status = gameEntity.status
        )
    }

    fun mapUserEntityToDomain(userEntity: UserEntity?): User? {
        return userEntity?.let {
            User(
                id = it.id,
                username = it.username,
                password = it.password
            )
        }
    }

    fun mapCurrentUserEntityToDomain(currentUserEntity: CurrentUserEntity?): User? {
        return currentUserEntity?.let {
            User(
                id = it.userId,
                username = it.username,
                password = "" // Пароль не хранится в CurrentUser
            )
        }
    }
}