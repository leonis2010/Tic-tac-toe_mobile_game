// ./main/java/com/example/tictactoeapp/TicTacToeApplication.kt
package com.example.tictactoeapp

import android.app.Application
import com.example.tictactoeapp.utils.SessionManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class TicTacToeApplication : Application() {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate() {
        super.onCreate()

        // Проверяем состояние сессии при запуске приложения
        checkSessionState()
    }

    private fun checkSessionState() {
        if (!sessionManager.wasGracefulExit()) {
            // Приложение было завершено аварийно
            println("🔄 DEBUG: App was terminated unexpectedly, clearing session")
            sessionManager.markSessionInvalid()
        }

        // Сбрасываем флаг для следующего запуска
        sessionManager.clearGracefulExit()
    }

    override fun onTerminate() {
        // Устанавливаем флаг корректного завершения
        sessionManager.setGracefulExit()
        super.onTerminate()
    }
}