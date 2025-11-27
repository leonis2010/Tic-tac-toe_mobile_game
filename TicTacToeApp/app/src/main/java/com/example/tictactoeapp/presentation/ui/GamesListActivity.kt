// ./src/main/java/com/example/tictactoeapp/presentation/ui/GamesListActivity.kt
package com.example.tictactoeapp.presentation.ui

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tictactoeapp.databinding.ActivityGamesListBinding
import com.example.tictactoeapp.presentation.viewmodel.GamesListViewModel
import com.example.tictactoeapp.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GamesListActivity : AppCompatActivity() {
    @Inject
    lateinit var sessionManager: SessionManager

    private lateinit var binding: ActivityGamesListBinding
    private val viewModel: GamesListViewModel by viewModels()
    private lateinit var adapter: GameItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGamesListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupUI()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = GameItemAdapter { gameItem ->
            println("DEBUG: Game item clicked: ${gameItem.id}")
            viewModel.joinGame(gameItem.id)
        }
        binding.recyclerViewGames.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewGames.adapter = adapter
    }

    private fun setupUI() {
        // Обновляем состояние загрузки при запуске
        updateLoadingState(viewModel.uiState.value.isLoading)

        binding.swipeRefreshLayout.setOnRefreshListener {
            // Добавляем небольшую задержку для лучшего UX
            Handler(Looper.getMainLooper()).postDelayed({
                viewModel.loadAvailableGames()
            }, 500)
        }

        binding.swipeRefreshLayout.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )

        binding.buttonCreateGame.setOnClickListener {
            // Переход на экран создания игры
            startActivity(Intent(this, CreateGameActivity::class.java))
        }

        // 👇 ДОБАВИТЬ ВРЕМЕННУЮ КНОПКУ ДЛЯ ТЕСТИРОВАНИЯ
        binding.buttonTestRefresh?.setOnClickListener {
            testRealRefreshScenario()
        }

        binding.buttonLogout.setOnClickListener {
            performLogout()
        }

        // 👇 ДОБАВИТЬ КНОПКУ ИСТОРИИ ИГР
        binding.buttonHistory.setOnClickListener {
            navigateToGameHistory()
        }

        binding.buttonLogout.setOnClickListener {
            performLogout()
        }

        // Кнопка таблицы лидеров
        binding.buttonLeaderboard?.setOnClickListener {
            navigateToLeaderboard()
        }
    }

    // 👇 ДОБАВИТЬ МЕТОД НАВИГАЦИИ
    private fun navigateToGameHistory() {
        println("DEBUG: GamesListActivity - Navigating to game history")
        val intent = Intent(this, GameHistoryActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    // 👇 МЕТОД НАВИГАЦИИ НА ТАБЛИЦУ ЛИДЕРОВ
    private fun navigateToLeaderboard() {
        println("DEBUG: GamesListActivity - Navigating to leaderboard")
        val intent = Intent(this, LeaderboardActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun performLogout() {
        sessionManager.markSessionInvalid()
        sessionManager.setGracefulExit() // Устанавливаем флаг корректного завершения

        // Переходим на экран логина
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->

                    println("🟡 DEBUG: GamesListActivity - UI State: games=${state.games.size}, loading=${state.isLoading}, error=${state.errorMessage}")
                    // ФИЛЬТРУЕМ игры: показываем только ожидающие второго игрока
                    val waitingGames = state.games.filter {
                        // Здесь можно добавить логику фильтрации, если нужно
                        true // показываем все игры
                    }
                    // Логируем каждую игру
                    state.games.forEach { game ->
                        println("🔵 DEBUG: GamesListActivity - Game in list: id=${game.id}, creator=${game.creatorUsername}")
                    }

                    // Обновляем индикатор загрузки и состояние UI
                    updateLoadingState(state.isLoading)

                    // Обновляем список игр в адаптере
                    adapter.submitList(state.games)
                    println("🟢 DEBUG: GamesListActivity - Adapter submitted ${state.games.size} games")

                    // Показываем/скрываем сообщение "Нет игр"
                    binding.textViewNoGames.visibility = if (state.games.isEmpty() && !state.isLoading) View.VISIBLE else View.GONE
                    println("🔵 DEBUG: GamesListActivity - No games text visible: ${binding.textViewNoGames.visibility == View.VISIBLE}")

                    // Обрабатываем ошибки
                    state.errorMessage?.let { showError(it) }

                    // Обрабатываем успешное присоединение к игре
                    if (state.joinSuccess) {
                        println("🟢 DEBUG: GamesListActivity - JOIN SUCCESS! Navigating to game: ${state.joinedGameId}")
                        state.joinedGameId?.let { gameId ->
                            // Навигация на экран текущей игры
                            val intent = Intent(this@GamesListActivity, CurrentGameActivity::class.java).apply {
                                putExtra("GAME_ID", gameId.toString())
                            }
                            startActivity(intent)
                            viewModel.clearJoinSuccess() // <-- Очищаем флаг в ViewModel
                        }
                    }
                }
            }
        }
    }

    // Обновляем состояние UI в зависимости от загрузки
    private fun updateLoadingState(isLoading: Boolean) {
        println("🔵 DEBUG: GamesListActivity - Loading state: $isLoading")
        binding.progressBarLoading.isVisible = isLoading // <-- Используем isVisible
        binding.swipeRefreshLayout.isRefreshing = isLoading // <-- Отражаем состояние загрузки в SwipeRefreshLayout
        binding.buttonCreateGame.isEnabled = !isLoading // <-- Отключаем кнопки во время загрузки
        binding.buttonLogout.isEnabled = !isLoading
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        viewModel.clearError()
    }

    override fun onResume() {
        super.onResume()
        println("DEBUG: GamesListActivity - onResume() called")
        viewModel.loadAvailableGames()
    }

    override fun onPause() {
        super.onPause()
        sessionManager.setGracefulExit()
    }

    override fun onDestroy() {
        super.onDestroy()
        sessionManager.setGracefulExit()
    }

    override fun onBackPressed() {
        println("DEBUG: GamesListActivity - onBackPressed()")

        // Показываем диалог подтверждения выхода
        showExitConfirmation()
    }

    private fun showExitConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Выход")
            .setMessage("Вы уверены, что хотите выйти из приложения?")
            .setPositiveButton("Да") { _, _ ->
                // Выполняем логаут и выходим
                performLogoutAndExit()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun performLogoutAndExit() {
        // Помечаем сессию как невалидную
        sessionManager.markSessionInvalid()
        sessionManager.setGracefulExit()

        // Завершаем все активности
        finishAffinity()
    }

    // ДЕБАГ Временный метод для тестирования - добавить в GamesListActivity
        private fun testRealRefreshScenario() {
        println("🧪 DEBUG: Testing real refresh scenario")

        // Вручную портим access token в SharedPreferences
        val prefs = getSharedPreferences("jwt_tokens", Context.MODE_PRIVATE)
        val originalToken = prefs.getString("access_token", "")
        prefs.edit().putString("access_token", "invalid_token_123").apply()

        println("🧪 DEBUG: Original token: ${originalToken?.take(10)}...")
        println("🧪 DEBUG: Token corrupted, making API call...")

        // Пытаемся сделать запрос - должен вызвать refresh
        viewModel.loadAvailableGames()

        // Через 5 секунд восстанавливаем оригинальный токен
        Handler(Looper.getMainLooper()).postDelayed({
            prefs.edit().putString("access_token", originalToken).apply()
            println("🧪 DEBUG: Original token restored")
        }, 5000)
    }
    //конец ДЕБАГА
}


