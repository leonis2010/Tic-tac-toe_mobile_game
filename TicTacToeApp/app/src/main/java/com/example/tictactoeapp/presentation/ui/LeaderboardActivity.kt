// ./main/java/com/example/tictactoeapp/presentation/ui/LeaderboardActivity.kt
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
import com.example.tictactoeapp.databinding.ActivityLeaderboardBinding
import com.example.tictactoeapp.presentation.model.LeaderboardViewData
import com.example.tictactoeapp.presentation.viewmodel.LeaderboardViewModel
import com.example.tictactoeapp.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LeaderboardActivity : AppCompatActivity() {
    @Inject
    lateinit var sessionManager: SessionManager

    private lateinit var binding: ActivityLeaderboardBinding
    private val viewModel: LeaderboardViewModel by viewModels()
    private lateinit var adapter: LeaderboardAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLeaderboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupUI()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = LeaderboardAdapter()
        binding.recyclerViewLeaderboard.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewLeaderboard.adapter = adapter
    }

    private fun setupUI() {
        // Настройка SwipeRefreshLayout
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshLeaderboard()
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
                    println("🟡 DEBUG: LeaderboardActivity - UI State: players=${state.leaderboard.size}, loading=${state.isLoading}, error=${state.errorMessage}")

                    updateLoadingState(state.isLoading)
                    updateLeaderboardList(state.leaderboard, state.isLoading)

                    state.errorMessage?.let { showError(it) }
                }
            }
        }
    }

    private fun updateLoadingState(isLoading: Boolean) {
        println("🔵 DEBUG: LeaderboardActivity - Loading state: $isLoading")
        binding.progressBarLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.swipeRefreshLayout.isRefreshing = isLoading

        // Показываем/скрываем сообщение "Нет данных"
        binding.textViewNoData.visibility =
            if (!isLoading && adapter.itemCount == 0) View.VISIBLE else View.GONE
    }

    private fun updateLeaderboardList(leaderboard: List<LeaderboardViewData>, isLoading: Boolean) {
        adapter.submitList(leaderboard)
        println("🟢 DEBUG: LeaderboardActivity - Adapter submitted ${leaderboard.size} players")

        // Показываем "Нет данных" только когда список пустой И не грузится
        val shouldShowNoData = !isLoading && leaderboard.isEmpty()
        binding.textViewNoData.visibility = if (shouldShowNoData) View.VISIBLE else View.GONE

        println("🔵 DEBUG: LeaderboardActivity - Show no data: $shouldShowNoData, items: ${leaderboard.size}, loading: $isLoading")
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        viewModel.clearError()
    }

    override fun onBackPressed() {
        println("DEBUG: LeaderboardActivity - onBackPressed() - returning to games list")
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