package com.example.tictactoeapp.data.api

import android.content.Context
import android.content.Intent
import com.example.tictactoeapp.presentation.ui.LoginActivity
import com.example.tictactoeapp.utils.TokenManager
import com.example.tictactoeapp.utils.JwtUtil
import com.example.tictactoeapp.utils.SessionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.notifyAll
import okhttp3.internal.wait
import javax.inject.Inject
import javax.inject.Provider

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
    private val jwtUtil: JwtUtil,
    private val apiProvider: Provider<TicTacToeApi>,
    private val sessionManager: SessionManager,
    @ApplicationContext private val context: Context
) : Interceptor {

    private var isRefreshing = false
    private val refreshLock = Any()

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val url = originalRequest.url.toString()

        if (url.contains("/auth/") && !url.contains("/auth/logout")) {
            return chain.proceed(originalRequest)
        }

        // ⚡ ПРОВЕРЯЕМ ПО СОХРАНЕННОМУ ВРЕМЕНИ
        val isExpired = tokenManager.isTokenExpired().blockingGet()
        if (isExpired) {
            println("🔄 DEBUG: Token expired (based on saved time) - refreshing...")
            return handleUnauthorizedError(chain, originalRequest)
        }

        // ⚡ ПРЕДУПРЕЖДЕНИЕ О СКОРОМ ИСТЕЧЕНИИ
        val isExpiringSoon = tokenManager.isTokenExpiringSoon().blockingGet()
        if (isExpiringSoon) {
            println("⚠️ DEBUG: Token expiring soon - consider background refresh")
        }

        val accessToken = tokenManager.getAccessToken().blockingGet()
        val request = addTokenToRequest(originalRequest, accessToken)
        val response = chain.proceed(request)

        // Резервная проверка (на случай расхождения времени)
        if (response.code == 401) {
            response.close()
            println("🔴 DEBUG: Server returned 401 - refreshing...")
            return handleUnauthorizedError(chain, originalRequest)
        }

        return response
    }

    private fun addTokenToRequest(originalRequest: Request, token: String?): Request {
        return if (!token.isNullOrEmpty()) {
            originalRequest.newBuilder()
                .addHeader("Authorization", jwtUtil.addBearerPrefix(token))
                .build()
        } else {
            originalRequest
        }
    }

    private fun handleUnauthorizedError(
        chain: Interceptor.Chain,
        originalRequest: Request
    ): Response {
        println("🔄 DEBUG: ===== REFRESH FLOW STARTED =====")

        synchronized(refreshLock) {
            println("🔄 DEBUG: Sync block entered, isRefreshing: $isRefreshing")
            while (isRefreshing) {
                try {
                    println("🔄 DEBUG: Waiting for existing refresh to complete...")
                    refreshLock.wait()
                } catch (e: InterruptedException) {
                    println("🔴 DEBUG: Refresh wait interrupted")
                    Thread.currentThread().interrupt()
                    throw TokenRefreshException("Token refresh interrupted")
                }
            }
            isRefreshing = true
            println("🔄 DEBUG: Set isRefreshing = true")
        }

        return try {
            println("🔄 DEBUG: Step 1 - Getting refresh token...")
            val refreshToken = tokenManager.getRefreshToken().blockingGet()
            println("🔄 DEBUG: Refresh token exists: ${!refreshToken.isNullOrEmpty()}")

            if (refreshToken.isNullOrEmpty()) {
                println("🔴 DEBUG: No refresh token available")
                throw TokenRefreshException("No refresh token available")
            }
            println("🔄 DEBUG: Step 2 - Checking refresh token validity...")
            if (refreshToken.isNullOrEmpty()) {
                println("🔴 DEBUG: No refresh token available")
                throw TokenRefreshException("No refresh token available")
            }

            println("🔄 DEBUG: Step 3 - Calling refresh API...")
            val newTokens = refreshAccessToken(refreshToken).blockingGet()
            println(
                "🔄 DEBUG: New tokens received - access: ${newTokens.accessToken.take(10)}..., refresh: ${
                    newTokens.refreshToken.take(
                        10
                    )
                }..."
            )

            println("🔄 DEBUG: Step 4 - Saving new tokens...")
            tokenManager.saveAuthData(
                accessToken = newTokens.accessToken,
                refreshToken = newTokens.refreshToken,
                expiresIn = newTokens.expiresIn,
                userId = newTokens.userId,
                username = newTokens.username
            ).blockingAwait() // ← ПРАВИЛЬНО для Completable
            println("🔄 DEBUG: Tokens saved successfully")


            println("🔄 DEBUG: Step 5 - Creating new request with fresh token...")
            val newRequest = addTokenToRequest(originalRequest, newTokens.accessToken)

            println("🔄 DEBUG: Step 6 - Proceeding with original request...")
            val response = chain.proceed(newRequest)
            println("🔄 DEBUG: Response received: ${response.code}")

            println("🔄 DEBUG: ===== REFRESH FLOW COMPLETED SUCCESSFULLY =====")
            response

        } catch (e: Exception) {
            println("🔴 DEBUG: ===== REFRESH FLOW FAILED =====")
            println("🔴 DEBUG: Error: ${e.message}")
            e.printStackTrace()

            handleTokenRefreshFailure(e)
            throw TokenRefreshException("Failed to refresh token: ${e.message}")
        } finally {
            synchronized(refreshLock) {
                isRefreshing = false
                refreshLock.notifyAll()
                println("🔄 DEBUG: Cleanup - isRefreshing = false, notified all")
            }
        }
    }

    private fun refreshAccessToken(refreshToken: String): Single<TokenRefreshResponse> {
        // Используем Provider.get() для получения API когда это нужно - критически важно для получения токенов
        return apiProvider.get().refreshTokenRx(RefreshTokenRequest(refreshToken))
            .map { userDto ->
                TokenRefreshResponse(
                    accessToken = userDto.accessToken
                        ?: throw Exception("No access token in response"),
                    refreshToken = userDto.refreshToken
                        ?: throw Exception("No refresh token in response"),
                    expiresIn = userDto.expiresIn ?: 900L,
                    userId = userDto.id,
                    username = userDto.username
                )
            }
            .subscribeOn(Schedulers.io())
    }

    private fun handleTokenRefreshFailure(error: Throwable) {
        // Очищаем токены
        tokenManager.clearTokens().blockingGet()

        sessionManager.markSessionInvalid()

        // Переход на логин
        val intent = Intent(context, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("AUTH_ERROR", "Сессия истекла. Войдите снова")
        }
        context.startActivity(intent)

        println("🔄 DEBUG: Token refresh failed, redirecting to login: ${error.message}")
    }

    data class TokenRefreshResponse(
        val accessToken: String,
        val refreshToken: String,
        val expiresIn: Long,
        val userId: Long?,
        val username: String?
    )

    class TokenRefreshException(message: String) : Exception(message)
}