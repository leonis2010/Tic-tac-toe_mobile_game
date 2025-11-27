// ./main/java/com/example/tictactoeapp/utils/SessionManager.kt
package com.example.tictactoeapp.utils

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val context: Context
) {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)
    }

    companion object {
        private const val KEY_GRACEFUL_EXIT = "graceful_exit"
        private const val KEY_SESSION_VALID = "session_valid"
        private const val KEY_LAST_USERNAME = "last_username"
    }

    // Устанавливаем флаг корректного завершения
    fun setGracefulExit() {
        prefs.edit().putBoolean(KEY_GRACEFUL_EXIT, true).apply()
    }

    // Сбрасываем флаг при запуске приложения
    fun clearGracefulExit() {
        prefs.edit().putBoolean(KEY_GRACEFUL_EXIT, false).apply()
    }

    // Проверяем, было ли корректное завершение
    fun wasGracefulExit(): Boolean {
        return prefs.getBoolean(KEY_GRACEFUL_EXIT, false)
    }

    // Помечаем сессию как валидную
    fun markSessionValid() {
        prefs.edit().putBoolean(KEY_SESSION_VALID, true).apply()
    }

    // Помечаем сессию как невалидную (при logout)
    fun markSessionInvalid() {
        prefs.edit().putBoolean(KEY_SESSION_VALID, false).apply()
    }

    // Проверяем валидность сессии
    fun isSessionValid(): Boolean {
        return prefs.getBoolean(KEY_SESSION_VALID, false)
    }

    // Сохраняем последнего пользователя
    fun saveLastUsername(username: String) {
        prefs.edit().putString(KEY_LAST_USERNAME, username).apply()
    }

    // Получаем последнего пользователя
    fun getLastUsername(): String? {
        return prefs.getString(KEY_LAST_USERNAME, null)
    }

}