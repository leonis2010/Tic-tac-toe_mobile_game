package com.example.tictactoeapp.mapper

import com.example.tictactoeapp.data.model.GameDto
import com.example.tictactoeapp.data.model.GameBoardDto
import com.example.tictactoeapp.data.model.GameHistoryDto
import com.example.tictactoeapp.data.model.LeaderboardDto
import com.example.tictactoeapp.data.model.UserDto
import com.example.tictactoeapp.domain.model.Game
import com.example.tictactoeapp.domain.model.GameBoard
import com.example.tictactoeapp.domain.model.GameHistory
import com.example.tictactoeapp.domain.model.Leaderboard
import com.example.tictactoeapp.domain.model.User
import java.util.Date

class DtoToDomainMapper {

    fun mapGameDtoToDomain(gameDto: GameDto?): Game? {
        println("DEBUG: DtoToDomainMapper - mapGameDtoToDomain called with: $gameDto")
        return gameDto?.let { dto ->
            Game(
                id = dto.id,
                board = mapGameBoardDtoToDomain(dto.board),
                isPlayerTurn = dto.isPlayerTurn,
                status = dto.status,
                gameType = dto.gameType ?: "PVP",
                creatorUsername = dto.creatorUsername,
                player2Username = dto.player2Username,
                currentPlayerUsername = dto.currentPlayerUsername,
                winner = dto.winner
            )
        }
    }

    fun mapGameBoardDtoToDomain(boardDto: GameBoardDto?): GameBoard {
        val boardArray = boardDto?.board ?: Array(3) { IntArray(3) }
        return GameBoard(boardArray)
    }

    fun mapUserDtoToDomain(userDto: UserDto?): User? {
        return userDto?.let {
            User(
                id = it.id,
                username = it.username ?: "",
                password = it.password ?: "",
                accessToken = it.accessToken,
                refreshToken = it.refreshToken,
                expiresIn = it.expiresIn
            )
        }
    }

    fun mapGameHistoryDtoToDomain(dto: GameHistoryDto?): GameHistory? {
        return dto?.let {
            GameHistory(
                id = it.id,
                player1Username = it.player1Username ?: "Unknown",
                player2Username = it.player2Username ?: "Unknown",
                gameType = it.gameType ?: "PVP",
                result = it.result ?: "UNKNOWN",
                gameDate = it.gameDate ?: Date(),
                winner = it.winner,
                currentUserUsername = it.currentUserUsername ?: ""
            )
        }
    }

    fun mapLeaderboardDtoToDomain(dto: LeaderboardDto?): Leaderboard? {
        return dto?.let {
            Leaderboard(
                username = it.username ?: "Unknown",
                gamesPlayed = it.gamesPlayed,
                gamesWon = it.gamesWon,
                gamesLost = it.gamesLost,
                gamesDrawn = it.gamesDrawn,
                winRate = it.winRate,
                rating = it.rating
            )
        }
    }
}