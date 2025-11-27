// ./src/main/java/com/example/tictactoeapp/presentation/ui/CreateGameActivity.kt
package com.example.tictactoeapp.presentation.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.tictactoeapp.databinding.ActivityCreateGameBinding
import com.example.tictactoeapp.presentation.viewmodel.CreateGameViewModel
import com.example.tictactoeapp.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CreateGameActivity : AppCompatActivity() {
    @Inject
    lateinit var sessionManager: SessionManager

    private lateinit var binding: ActivityCreateGameBinding
    private val viewModel: CreateGameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.buttonCreateWithComputer.setOnClickListener {
            viewModel.createGameWithComputer()
        }

        binding.buttonCreateWithPlayer.setOnClickListener {
            viewModel.createGameWithPlayer()
        }
    }

    private fun observeViewModel() {
        println("DEBUG: Activity - observeViewModel() started")

        lifecycleScope.launch {
            println("DEBUG: Activity - inside lifecycleScope.launch")

            viewModel.uiState.collect { state ->
                println("DEBUG: Activity - state collected: createSuccess=${state.createSuccess}, gameId=${state.createdGameId}, isLoading=${state.isLoading}")

                updateLoadingState(state.isLoading)
                state.errorMessage?.let { error ->
                    if (error.contains("Unauthorized") || error.contains("401")) {
                        // 👇 ПРОСТО РАЗЛОГИНИВАЕМ И ВОЗВРАЩАЕМ НА ЛОГИН
                        sessionManager.markSessionInvalid()
                        val intent = Intent(this@CreateGameActivity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        showError(error)
                    }
                }

                if (state.createSuccess) {
                    println("DEBUG: Activity - CREATE SUCCESS DETECTED!")
                    state.createdGameId?.let { gameId ->
                        println("DEBUG: Activity - Starting navigation with gameId: $gameId")

                        val intent = Intent(this@CreateGameActivity, CurrentGameActivity::class.java).apply {
                            putExtra("GAME_ID", gameId.toString())
                        }
                        println("DEBUG: Activity - Intent created, starting activity...")
                        startActivity(intent)
                        println("DEBUG: Activity - Activity started, finishing current activity...")
                        finish()
                        viewModel.clearCreateSuccess()
                        println("DEBUG: Activity - Navigation completed")
                    } ?: run {
                        println("DEBUG: Activity - ERROR: createSuccess=true but gameId is null!")
                    }
                }
            }
        }
    }

    private fun updateLoadingState(isLoading: Boolean) {
        binding.progressBarLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.buttonCreateWithComputer.isEnabled = !isLoading
        binding.buttonCreateWithPlayer.isEnabled = !isLoading
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        viewModel.clearError()
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
        println("DEBUG: CreateGameActivity - onBackPressed() - returning to games list")
        finish()
        // анимация перехода
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
    }
}