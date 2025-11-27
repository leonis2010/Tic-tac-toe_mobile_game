// ./main/java/com/example/tictactoeapp/presentation/ui/GameHistoryActivity.kt
package com.example.tictactoeapp.presentation.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tictactoeapp.databinding.ActivityGameHistoryBinding
import com.example.tictactoeapp.presentation.model.GameHistoryViewData
import com.example.tictactoeapp.presentation.viewmodel.GameHistoryViewModel
import com.example.tictactoeapp.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GameHistoryActivity : AppCompatActivity() {
    @Inject
    lateinit var sessionManager: SessionManager

    private lateinit var binding: ActivityGameHistoryBinding
    private val viewModel: GameHistoryViewModel by viewModels()
    private lateinit var adapter: GameHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupUI()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = GameHistoryAdapter()
        binding.recyclerViewHistory.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewHistory.adapter = adapter
    }

    private fun setupUI() {
        // Настройка SwipeRefreshLayout
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshHistory()
        }

        binding.swipeRefreshLayout.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )

        // Кнопка назад
        binding.buttonBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    println("🟡 DEBUG: GameHistoryActivity - UI State: history=${state.history.size}, loading=${state.isLoading}, error=${state.errorMessage}")

                    updateLoadingState(state.isLoading)
                    updateHistoryList(state.history, state.isLoading)

                    state.errorMessage?.let { showError(it) }
                }
            }
        }
    }

    private fun updateLoadingState(isLoading: Boolean) {
        println("🔵 DEBUG: GameHistoryActivity - Loading state: $isLoading")
        binding.progressBarLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.swipeRefreshLayout.isRefreshing = isLoading

        // Показываем/скрываем сообщение "Нет истории"
        binding.textViewNoHistory.visibility =
            if (!isLoading && adapter.itemCount == 0) View.VISIBLE else View.GONE
    }

    private fun updateHistoryList(history: List<GameHistoryViewData>, isLoading: Boolean) {
        adapter.submitList(history)
        println("🟢 DEBUG: GameHistoryActivity - Adapter submitted ${history.size} history items")

        // 👇 ПРОСТАЯ ЛОГИКА: показываем "Нет истории" только когда список пустой И не грузится
        val shouldShowNoHistory = !isLoading && history.isEmpty()
        binding.textViewNoHistory.visibility = if (shouldShowNoHistory) View.VISIBLE else View.GONE

        println("🔵 DEBUG: GameHistoryActivity - Show no history: $shouldShowNoHistory, items: ${history.size}, loading: $isLoading")
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        viewModel.clearError()
    }

    override fun onBackPressed() {
        println("DEBUG: GameHistoryActivity - onBackPressed() - returning to games list")
        finish()
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
    }

    override fun onPause() {
        super.onPause()
        sessionManager.setGracefulExit()
    }

    override fun onDestroy() {
        super.onDestroy()
        sessionManager.setGracefulExit()
    }
}