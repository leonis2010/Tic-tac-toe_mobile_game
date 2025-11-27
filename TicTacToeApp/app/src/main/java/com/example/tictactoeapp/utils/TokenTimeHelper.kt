// ./main/java/com/example/tictactoeapp/utils/TokenTimeHelper.kt
package com.example.tictactoeapp.utils

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JwtUtil @Inject constructor() {

    companion object {
        private const val TOKEN_PREFIX = "Bearer "
    }

    /**
     * Используем время, сохраненное при получении токена
     */
    fun isTokenExpiredBasedOnSavedTime(savedExpiryTime: Long): Boolean {
        return System.currentTimeMillis() > savedExpiryTime
    }

    /**
     * Проверяем, истекает ли токен скоро (на основе сохраненного времени)
     */

    //10 секунд для показа проекта
    fun isTokenExpiringSoon(savedExpiryTime: Long, thresholdMs: Long = 10 * 1000): Boolean {
        val remainingTime = savedExpiryTime - System.currentTimeMillis()
        return remainingTime in 1..thresholdMs
    }

    /**
     * Добавляет Bearer префикс к токену
     */
    fun addBearerPrefix(token: String): String {
        return if (token.startsWith(TOKEN_PREFIX)) {
            token
        } else {
            "$TOKEN_PREFIX$token"
        }
    }

}