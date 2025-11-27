// ./src/main/java/com/example/tictactoeapp/data/api/TicTacToeApi.kt
package com.example.tictactoeapp.data.api

import com.example.tictactoeapp.data.model.GameDto
import com.example.tictactoeapp.data.model.GameHistoryDto
import com.example.tictactoeapp.data.model.LeaderboardDto
import com.example.tictactoeapp.data.model.UserDto
import com.google.gson.annotations.SerializedName
import retrofit2.http.*
import java.util.*
import io.reactivex.Single
interface TicTacToeApi {
    // Авторизация
    @POST("auth/login")
    fun loginRx(@Body userDto: UserDto): Single<UserDto>

    @POST("auth/refresh")
    fun refreshTokenRx(@Body request: RefreshTokenRequest): Single<UserDto>

    @POST("auth/register")
    fun registerRx(@Body userDto: UserDto): Single<UserDto>

    @POST("auth/logout")
    fun logoutRx(): Single<Map<String, String>>

    // Игры
    @POST("games/start")
    fun startNewGameRx(): Single<GameDto>

    @POST("games/start/computer")
    fun startNewGameWithComputerRx(): Single<GameDto>

    @POST("games/start/player")
    fun startNewGameWithPlayerRx(): Single<GameDto>

    @POST("games/{gameId}")
    fun makeMoveRx(
        @Path("gameId") gameId: UUID,
        @Body gameDto: GameDto
    ): Single<GameDto>

    @GET("games/{gameId}")
    fun getGameRx(@Path("gameId") gameId: UUID): Single<GameDto>

    // Получение списка игр
    @GET("games")
    fun getGamesRx(): Single<List<GameDto>>

    @POST("games/{gameId}/join")
    fun joinGameRx(@Path("gameId") gameId: UUID): Single<GameDto>

    @POST("games/{gameId}/player-left")
    fun playerLeftGameRx(@Path("gameId") gameId: UUID): Single<GameDto>

    // МОК МЕТОД для тестирования начало дебаг
    @POST("auth/mock-register")
    fun mockRegisterRx(@Body userDto: UserDto): Single<UserDto>
    // конец дебаг

    @GET("games/history")
    fun getGameHistoryRx(): Single<List<GameHistoryDto>>

    @GET("games/leaderboard")
    fun getLeaderboardRx(): Single<List<LeaderboardDto>>


}

data class RefreshTokenRequest(
    @SerializedName("refreshToken") val refreshToken: String
)
// Модель ответа начало дебаг
data class AuthResponse(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("username") val username: String? = null,
    @SerializedName("accessToken") val accessToken: String? = null,
    @SerializedName("refreshToken") val refreshToken: String? = null,
    @SerializedName("expiresIn") val expiresIn: Long? = null,
    @SerializedName("message") val message: String? = null
)
// конец дебаг