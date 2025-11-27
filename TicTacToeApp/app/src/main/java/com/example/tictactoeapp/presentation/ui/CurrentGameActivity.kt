package com.example.tictactoeapp.presentation.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.tictactoeapp.R
import com.example.tictactoeapp.databinding.ActivityCurrentGameBinding
import com.example.tictactoeapp.presentation.model.CurrentGameViewData
import com.example.tictactoeapp.presentation.viewmodel.CurrentGameViewModel
import com.example.tictactoeapp.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class CurrentGameActivity : AppCompatActivity() {
    @Inject
    lateinit var sessionManager: SessionManager

    private lateinit var binding: ActivityCurrentGameBinding
    private val viewModel: CurrentGameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCurrentGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gameIdString = intent.getStringExtra("GAME_ID")
        if (gameIdString != null) {
            try {
                val gameId = UUID.fromString(gameIdString)
                viewModel.loadGame(gameId)
            } catch (e: IllegalArgumentException) {
                Toast.makeText(this, "Invalid Game ID", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
        } else {
            Toast.makeText(this, "Game ID not provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupBoard()
        observeViewModel()
        observeNavigationEvents()
    }

    private fun setupBoard() {
        binding.buttonCell00.setOnClickListener { viewModel.makeMove(0, 0) }
        binding.buttonCell01.setOnClickListener { viewModel.makeMove(0, 1) }
        binding.buttonCell02.setOnClickListener { viewModel.makeMove(0, 2) }
        binding.buttonCell10.setOnClickListener { viewModel.makeMove(1, 0) }
        binding.buttonCell11.setOnClickListener { viewModel.makeMove(1, 1) }
        binding.buttonCell12.setOnClickListener { viewModel.makeMove(1, 2) }
        binding.buttonCell20.setOnClickListener { viewModel.makeMove(2, 0) }
        binding.buttonCell21.setOnClickListener { viewModel.makeMove(2, 1) }
        binding.buttonCell22.setOnClickListener { viewModel.makeMove(2, 2) }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    println("[UI] DEBUG: UI State updated:")
                    println("[UI] DEBUG: - Status: ${state.gameStatus}")
                    println("[UI] DEBUG: - Player2: ${state.player2Username}")
                    println("[UI] DEBUG: - Is computer game: ${state.player2Username == null}")

                    updateLoadingState(state.isLoading)
                    state.errorMessage?.let { showError(it) }

                    // Используем ресурсы для текста
                    binding.textViewGameId.text =
                        getString(R.string.game_id_format, state.gameId?.toString()?.take(8) ?: "")
                    binding.textViewGameStatus.text =
                        getString(R.string.status_format, state.gameStatus)

                    val player1Name = state.player1Username ?: "Player 1"
                    val player2Name = state.player2Username ?: "COMPUTER"

                    binding.textViewPlayer1.text =
                        getString(R.string.player_format, player1Name, "X")
                    binding.textViewPlayer2.text =
                        getString(R.string.player_format, player2Name, "O")

                    updateBoard(state.board)
                    updateGameStateMessage(state)
                }
            }
        }
    }

    private fun observeNavigationEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigationEvent.collect { event ->
                    when (event) {
                        is CurrentGameViewModel.NavigationEvent.NavigateToLogin -> {
                            val intent = Intent(this@CurrentGameActivity, LoginActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            }
        }
    }

    private fun updateBoard(board: Array<IntArray>) {
        val buttons = listOf(
            binding.buttonCell00, binding.buttonCell01, binding.buttonCell02,
            binding.buttonCell10, binding.buttonCell11, binding.buttonCell12,
            binding.buttonCell20, binding.buttonCell21, binding.buttonCell22
        )

        for (i in board.indices) {
            for (j in board[i].indices) {
                val index = i * 3 + j
                val button = buttons[index]
                when (board[i][j]) {
                    1 -> button.text = "X"  // Создатель игры
                    -1 -> button.text = "O" // Второй игрок или компьютер
                    else -> button.text = ""
                }
            }
        }
    }

    private fun updateGameStateMessage(state: CurrentGameViewData) {
        println("[UI] DEBUG: updateGameStateMessage - status: ${state.gameStatus}, winner: ${state.winner}")

        val messagesPVE = listOf(
            "Победа!\nСоперник покинул игру. 🏆",
            "Вы победили!\nОппонент вышел из игры. 🎯",
            "Победа!\nИгрок сдался. ⚡"
        )

        val messagesDrawPVE = listOf(
            "Ничья!\nКомпьютер сыграл вничью. 🤝",
            "Ничья!\nОтличная игра с компьютером! 🎮",
            "Ничья!\nОба игрока проявили мастерство! ⚔️"
        )

        val messagesWinPVE = listOf(
            "Победа!\nВы обыграли компьютер! 🎉",
            "Победа!\nИскусственный интеллект повержен! ⚡",
            "Победа!\nМашина не смогла вас победить! 🏆"
        )

        val messagesLosePVE = listOf(
            "Поражение!\nКомпьютер оказался сильнее. 🤖",
            "Проигрыш!\nИИ одержал победу. 💻",
            "Поражение!\nМашина победила человека. 🎮"
        )

        val message = when {
            // Специальный случай - игрок вышел (PLAYER_LEFT)
            state.gameStatus == "PLAYER_LEFT" -> messagesPVE.random()

            // Победа над компьютером в PVE
            state.winner == viewModel.currentUsername && state.player2Username == "COMPUTER" -> messagesWinPVE.random()

            // Поражение от компьютера в PVE
            state.winner == "COMPUTER" -> messagesLosePVE.random()

            // Ничья в PVE игре
            state.winner == "DRAW_PVE" -> messagesDrawPVE.random()

            // Обычная ничья в PVP игре
            state.winner == "DRAW" -> "Ничья!"

            // Обычная победа в PVP игре
            state.winner == viewModel.currentUsername -> "Победа! 🎉"

            // Поражение в PVP игре (другой игрок победил)
            state.winner != null && state.winner != viewModel.currentUsername &&
                    state.winner != "COMPUTER" && state.winner != "DRAW" && state.winner != "DRAW_PVE" -> "Поражение 😔"

            // Ожидание игроков
            state.gameStatus == "WAITING_FOR_PLAYERS" -> "Ожидание второго игрока..."

            // Игра в процессе
            state.gameStatus == "IN_PROGRESS" -> {
                val isMyTurn = state.currentPlayerUsername == viewModel.currentUsername
                if (isMyTurn) "Ваш ход!" else "Ход соперника..."
            }

            // Запасной вариант
            else -> "Статус: ${state.gameStatus}"
        }

        binding.textViewGameStateMessage.text = message

        val isBoardEnabled = state.gameStatus == "IN_PROGRESS" &&
                state.currentPlayerUsername == viewModel.currentUsername &&
                state.winner == null &&
                state.gameStatus != "PLAYER_LEFT"

        setBoardEnabled(isBoardEnabled)

        println("[UI] DEBUG: Final message: '$message', board enabled: $isBoardEnabled")
    }

    private fun updateLoadingState(isLoading: Boolean) {
        binding.progressBarLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
        if (isLoading) {
            setBoardEnabled(false)
        } else {
            val state = viewModel.uiState.value
            updateGameStateMessage(state)
        }
    }

    private fun setBoardEnabled(enabled: Boolean) {
        val buttons = listOf(
            binding.buttonCell00, binding.buttonCell01, binding.buttonCell02,
            binding.buttonCell10, binding.buttonCell11, binding.buttonCell12,
            binding.buttonCell20, binding.buttonCell21, binding.buttonCell22
        )
        buttons.forEach { it.isEnabled = enabled }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        viewModel.clearError()
    }

    private fun exitToGamesList() {
        println("DEBUG: CurrentGameActivity - exiting to games list")
        viewModel.leaveGame()

        // Возвращаемся к списку игр
        val intent = Intent(this, GamesListActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
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
        println("DEBUG: CurrentGameActivity - onBackPressed() - returning to games list")

        // Отправляем запрос о выходе из игры
        viewModel.leaveGame()
        finish()

        // анимация перехода
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
    }
}