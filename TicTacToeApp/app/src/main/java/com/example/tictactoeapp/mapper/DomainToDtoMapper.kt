package com.example.tictactoeapp.mapper

import com.example.tictactoeapp.data.model.GameDto
import com.example.tictactoeapp.data.model.GameBoardDto
import com.example.tictactoeapp.data.model.UserDto
import com.example.tictactoeapp.domain.model.Game
import com.example.tictactoeapp.domain.model.GameBoard
import com.example.tictactoeapp.domain.model.User

class DomainToDtoMapper {

    fun mapGameToDto(game: Game?): GameDto? {
        println("DEBUG: DomainToDtoMapper - mapGameToDto called with: $game")

        return game?.let { domain ->
            val dto = GameDto(
                id = domain.id,
                board = mapGameBoardToDto(domain.board),
                isPlayerTurn = domain.isPlayerTurn,
                status = domain.status,
                gameType = domain.gameType,
                creatorUsername = domain.creatorUsername,
                player2Username = domain.player2Username,
                currentPlayerUsername = domain.currentPlayerUsername,
                        winner = domain.winner
            )
            println("DEBUG: DomainToDtoMapper - mapped to DTO: $dto")
            println("DEBUG: DomainToDtoMapper - board in DTO: ${dto.board}")
            dto
        }
    }

    fun mapGameBoardToDto(gameBoard: GameBoard?): GameBoardDto? {
        println("DEBUG: DomainToDtoMapper - mapGameBoardToDto called with: $gameBoard")
        val result = gameBoard?.board?.let { GameBoardDto(it) }
        println("DEBUG: DomainToDtoMapper - mapped board to DTO: $result")
        return result
    }

    fun mapUserToDto(user: User?): UserDto? {
        println("🟡 DEBUG: DomainToDtoMapper.mapUserToDto called")
        println("🟡 DEBUG: Input user: $user")

        val result = user?.let {
            UserDto(
                id = it.id,
                username = it.username,
                password = it.password,
                accessToken = it.accessToken,
                refreshToken = it.refreshToken,
                expiresIn = it.expiresIn
            )
        }

        println("🟡 DEBUG: Output UserDto: $result")
        return result
    }
}