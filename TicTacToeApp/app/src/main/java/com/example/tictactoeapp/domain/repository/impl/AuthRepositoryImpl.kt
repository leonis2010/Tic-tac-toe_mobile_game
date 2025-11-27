package com.example.tictactoeapp.domain.repository.impl

import com.example.tictactoeapp.data.api.RefreshTokenRequest
import com.example.tictactoeapp.data.api.TicTacToeApi
import com.example.tictactoeapp.data.model.UserDto
import com.example.tictactoeapp.data.repository.NetworkGameService
import com.example.tictactoeapp.domain.repository.AuthRepository
import com.example.tictactoeapp.domain.model.User
import com.example.tictactoeapp.utils.TokenManager
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: TicTacToeApi,
    private val networkGameService: NetworkGameService,
    private val tokenManager: TokenManager,
) : AuthRepository {
    private val compositeDisposable = CompositeDisposable()

    override fun validateCredentials(username: String, password: String): Boolean {
        return username.isNotBlank() && password.length >= 6
    }

    override fun login(username: String, password: String): Single<User> {
        println("🟡 DEBUG: AuthRepositoryImpl.login() called")

        if (!validateCredentials(username, password)) {
            return Single.error(IllegalArgumentException("Invalid credentials"))
        }

        val userDto = UserDto(username = username, password = password)

        return api.loginRx(userDto)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { error ->
                println("🔴 DEBUG: Login failed: ${error.message}")
                if (error is retrofit2.HttpException) {
                    println("🔴 DEBUG: HTTP Error code: ${error.code()}")
                }
            }
            .flatMap { responseUserDto ->
                val accessToken = responseUserDto.accessToken ?: ""
                val refreshToken = responseUserDto.refreshToken ?: ""
                val expiresIn = responseUserDto.expiresIn ?: 900L

                tokenManager.saveAuthData(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    expiresIn = expiresIn,
                    userId = responseUserDto.id,
                    username = responseUserDto.username
                ).andThen(Single.just(responseUserDto))
            }
            .map { responseUserDto ->
                User(
                    id = responseUserDto.id,
                    username = responseUserDto.username ?: username,
                    password = "",
                    accessToken = responseUserDto.accessToken,
                    refreshToken = responseUserDto.refreshToken,
                    expiresIn = responseUserDto.expiresIn
                )
            }
            .doOnSuccess { user ->
                println("🟢 DEBUG: Login successful for user: ${user.username}")
            }
    }

    override fun register(username: String, password: String): Single<User> {
        println("🔵 DEBUG: AuthRepositoryImpl.register START")
        println("🔵 DEBUG: username: '$username', password: '$password'")

        if (!validateCredentials(username, password)) {
            println("🔴 DEBUG: Validation failed")
            return Single.error(IllegalArgumentException("Invalid credentials"))
        }
        println("🟢 DEBUG: Validation passed")

        val userDto = UserDto(username = username, password = password)
        println("🔵 DEBUG: Created UserDto: $userDto")

        // Используем NetworkGameService вместо прямого вызова API
        println("🔵 DEBUG: Calling networkGameService.register()")
        return networkGameService.register(userDto)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                println("🔵 DEBUG: networkGameService.register SUBSCRIBED")
            }
            .doOnSuccess { responseUserDto ->
                println("🟢 DEBUG: networkGameService.register SUCCESS: $responseUserDto")
            }
            .doOnError { error ->
                println("🔴 DEBUG: ========== NETWORK SERVICE ERROR DETAILS START ==========")
                println("🔴 DEBUG: networkGameService.register ERROR: ${error.message}")
                println("🔴 DEBUG: Error class: ${error.javaClass.name}")

                if (error is retrofit2.HttpException) {
                    println("🔴 DEBUG: HTTP Error code: ${error.code()}")
                    try {
                        val errorBody = error.response()?.errorBody()?.string()
                        println("🔴 DEBUG: HTTP Error body: $errorBody")
                    } catch (e: Exception) {
                        println("🔴 DEBUG: Could not read error body: ${e.message}")
                    }
                }
                error.printStackTrace()
                println("🔴 DEBUG: ========== NETWORK SERVICE ERROR DETAILS END ==========")
            }
            .flatMap { responseUserDto ->
                println("🔵 DEBUG: Starting token save process")
                val accessToken = responseUserDto.accessToken ?: ""
                val refreshToken = responseUserDto.refreshToken ?: ""
                val expiresIn = responseUserDto.expiresIn ?: 900L

                println(
                    "🔵 DEBUG: Saving tokens - access: ${accessToken.take(10)}..., refresh: ${
                        refreshToken.take(
                            10
                        )
                    }..."
                )

                tokenManager.saveAuthData(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    expiresIn = expiresIn,
                    userId = responseUserDto.id,
                    username = responseUserDto.username
                ).doOnComplete {
                    println("🟢 DEBUG: Tokens saved successfully")
                }.doOnError { error ->
                    println("🔴 DEBUG: Token save failed: ${error.message}")
                }.andThen(Single.just(responseUserDto))
            }
            .map { responseUserDto ->
                println("🔵 DEBUG: Mapping DTO to Domain")
                val user = User(
                    id = responseUserDto.id,
                    username = responseUserDto.username ?: username,
                    password = "",
                    accessToken = responseUserDto.accessToken,
                    refreshToken = responseUserDto.refreshToken,
                    expiresIn = responseUserDto.expiresIn
                )
                println("🟢 DEBUG: Mapped user: $user")
                user
            }
            .doOnSuccess { user ->
                println("🎉 DEBUG: Registration COMPLETE for user: ${user.username}")
            }
    }

    override fun refreshToken(): Single<User> {
        return tokenManager.getRefreshToken()
            .flatMap { refreshToken ->
                if (refreshToken.isNullOrEmpty()) {
                    Single.error(Exception("No refresh token available"))
                } else {
                    val request = RefreshTokenRequest(refreshToken = refreshToken)
                    api.refreshTokenRx(request)
                        .flatMap { responseUserDto ->
                            val accessToken = responseUserDto.accessToken ?: ""
                            val newRefreshToken = responseUserDto.refreshToken ?: ""
                            val expiresIn = responseUserDto.expiresIn ?: 900L
                            val username =
                                responseUserDto.username ?: tokenManager.getUsername().blockingGet()

                            tokenManager.saveAuthData(
                                accessToken = accessToken,
                                refreshToken = newRefreshToken,
                                expiresIn = expiresIn,
                                userId = responseUserDto.id,
                                username = username // ← СОХРАНЯЕМ USERNAME ИЗ ОТВЕТА
                            ).andThen(Single.just(responseUserDto))
                        }
                        .map { responseUserDto ->
                            User(
                                id = responseUserDto.id,
                                username = responseUserDto.username
                                    ?: "", // ← ИСПОЛЬЗУЕМ USERNAME ИЗ ОТВЕТА
                                accessToken = responseUserDto.accessToken,
                                refreshToken = responseUserDto.refreshToken,
                                expiresIn = responseUserDto.expiresIn
                            )
                        }
                }
            }
    }

    override fun logout(): Single<Boolean> {
        return tokenManager.clearTokens()
            .toSingle { true }
            .onErrorReturn { false }
            .doOnSuccess {
                api.logoutRx()
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                        { println("Server logout successful") },
                        { error -> println("Server logout failed: ${error.message}") }
                    )
                    .addTo(compositeDisposable)
            }
    }
    override fun clear() {
        compositeDisposable.clear()
        println("🔄 DEBUG: AuthRepository cleared")
    }
}