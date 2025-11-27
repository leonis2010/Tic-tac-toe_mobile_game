// ./src/main/java/com/example/tictactoeapp/data/repository/NetworkGameService.kt
package com.example.tictactoeapp.data.repository

import com.example.tictactoeapp.data.api.RefreshTokenRequest
import com.example.tictactoeapp.data.api.TicTacToeApi
import com.example.tictactoeapp.data.model.GameDto
import com.example.tictactoeapp.data.model.GameHistoryDto
import com.example.tictactoeapp.data.model.LeaderboardDto
import com.example.tictactoeapp.data.model.UserDto
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.*

class NetworkGameService(
    private val api: TicTacToeApi
) {
    fun startNewGame(): Single<GameDto> {
        return api.startNewGameRx()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun startNewGameWithComputer(): Single<GameDto> {
        return api.startNewGameWithComputerRx()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun startNewGameWithPlayer(): Single<GameDto> {
        return api.startNewGameWithPlayerRx()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun makeMove(gameId: UUID, gameDto: GameDto): Single<GameDto> {
        println("DEBUG: NetworkGameService - makeMove called")
        println("DEBUG: NetworkGameService - gameId: $gameId")
        println("DEBUG: NetworkGameService - gameDto: $gameDto")
        println("DEBUG: NetworkGameService - gameDto.board: ${gameDto.board}")
        println("DEBUG: NetworkGameService - gameDto.isPlayerTurn: ${gameDto.isPlayerTurn}")

        return api.makeMoveRx(gameId, gameDto)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { response ->
                println("DEBUG: NetworkGameService - makeMove SUCCESS: $response")
            }
            .doOnError { error ->
                println("DEBUG: NetworkGameService - makeMove ERROR: ${error.message}")
            }
    }

    fun getGame(gameId: UUID): Single<GameDto> {
        return api.getGameRx(gameId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    // Реализация getGames
    fun getGames(): Single<List<GameDto>> {
        return api.getGamesRx()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun joinGame(gameId: UUID): Single<GameDto> {
        return api.joinGameRx(gameId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    // Методы для AuthRepositoryImpl
    fun login(userDto: UserDto): Single<UserDto> {
        println("🚨 DEBUG: NetworkGameService.login() called")
        println("🚨 DEBUG: userDto: $userDto")

        return api.loginRx(userDto)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                println("🚨 DEBUG: API login SUBSCRIBED")
            }
            .doOnSuccess { response ->
                println("🚨 DEBUG: API login SUCCESS: $response")
            }
            .doOnError { error ->
                println("🚨 DEBUG: API login ERROR: ${error.message}")
                if (error is retrofit2.HttpException) {
                    println("🚨 DEBUG: HTTP Error code: ${error.code()}")
                    try {
                        val errorBody = error.response()?.errorBody()?.string()
                        println("🚨 DEBUG: HTTP Error body: $errorBody")
                    } catch (e: Exception) {
                        println("🚨 DEBUG: Could not read error body: ${e.message}")
                    }
                }
                error.printStackTrace()
            }
    }

    fun register(userDto: UserDto): Single<UserDto> {
        println("🚨 DEBUG: NetworkGameService.register() called")
        println("🚨 DEBUG: userDto: $userDto")

        // РЕАЛЬНЫЙ ВЫЗОВ (раскомментировать):
        return api.registerRx(userDto)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { response ->
                println("🚨 DEBUG: Real API register SUCCESS: $response")
            }
            .doOnError { error ->
                println("🚨 DEBUG: Real API register ERROR: ${error.message}")
            }

        // МОК (заглушка) регистрация без сервера (закомментировать):
        // return mockRegister(userDto)
    }

    // МОК (заглушка) регистрация без сервера
    private fun mockRegister(userDto: UserDto): Single<UserDto> {
        println("🚨 DEBUG: Using MOCK register")

        val mockResponse = UserDto(
            id = 12345L,
            username = userDto.username,
            password = userDto.password,
            accessToken = "mock-access-token-${System.currentTimeMillis()}",
            refreshToken = "mock-refresh-token-${System.currentTimeMillis()}",
            expiresIn = 900L
        )

        println("🚨 DEBUG: Mock response: $mockResponse")

        return Single.just(mockResponse)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess {
                println("🚨 DEBUG: Mock register SUCCESS")
            }
            .doOnError { error ->
                println("🚨 DEBUG: Mock register ERROR: ${error.message}")
            }
    }

    /**
     * Присоединиться к существующей игре через API.
     * @param gameId ID игры, к которой нужно присоединиться.
     * @return Single<GameDto> - Observable, который эмитит GameDto после успешного присоединения.
     */
    fun joinGameRx(gameId: UUID): Single<GameDto> {
        println("🟡 DEBUG: NetworkGameService - joinGameRx() called for gameId: $gameId")
        return api.joinGameRx(gameId)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { println("🔵 DEBUG: API joinGame subscribed") }
            .doOnSuccess { gameDto ->
                println("🟢 DEBUG: NetworkGameService - joinGameRx SUCCESS: $gameDto")
            }
            .doOnError { error ->
                println("🔴 DEBUG: NetworkGameService - joinGameRx ERROR: ${error.message}")
            }
    }

    fun playerLeftGame(gameId: UUID): Single<GameDto> {
        println("🟡 DEBUG: NetworkGameService - playerLeftGame() called for gameId: $gameId")
        return api.playerLeftGameRx(gameId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { gameDto ->
                println("🟢 DEBUG: NetworkGameService - playerLeftGame SUCCESS: $gameDto")
            }
            .doOnError { error ->
                println("🔴 DEBUG: NetworkGameService - playerLeftGame ERROR: ${error.message}")
            }
    }

    fun refreshToken(refreshToken: String): Single<UserDto> {
        return api.refreshTokenRx(RefreshTokenRequest(refreshToken))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getGameHistory(): Single<List<GameHistoryDto>> {
        return api.getGameHistoryRx()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { history ->
                println("🟢 DEBUG: NetworkGameService - getGameHistory SUCCESS: ${history.size} games")
            }
            .doOnError { error ->
                println("🔴 DEBUG: NetworkGameService - getGameHistory ERROR: ${error.message}")
            }
    }

    fun getLeaderboard(): Single<List<LeaderboardDto>> {
        return api.getLeaderboardRx()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { leaderboard ->
                println("🟢 DEBUG: NetworkGameService - getLeaderboard SUCCESS: ${leaderboard.size} players")
            }
            .doOnError { error ->
                println("🔴 DEBUG: NetworkGameService - getLeaderboard ERROR: ${error.message}")
            }
    }
}