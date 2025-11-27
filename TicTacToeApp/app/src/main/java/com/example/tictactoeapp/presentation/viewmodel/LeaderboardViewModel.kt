// ./main/java/com/example/tictactoeapp/presentation/viewmodel/LeaderboardViewModel.kt
package com.example.tictactoeapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.tictactoeapp.domain.repository.GameRepository
import com.example.tictactoeapp.mapper.DomainToViewDataMapper
import com.example.tictactoeapp.presentation.model.LeaderboardViewData
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val domainToViewDataMapper: DomainToViewDataMapper
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardState())
    val uiState: StateFlow<LeaderboardState> = _uiState.asStateFlow()

    private val compositeDisposable = CompositeDisposable()

    init {
        loadLeaderboard()
    }

    fun loadLeaderboard() {
        println("🟡 DEBUG: LeaderboardViewModel - loadLeaderboard() called")

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        gameRepository.getLeaderboard()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ leaderboardList ->
                println("🟢 DEBUG: LeaderboardViewModel - getLeaderboard SUCCESS: ${leaderboardList.size} players")

                val viewDataList = leaderboardList.mapIndexed { index, leaderboard ->
                    domainToViewDataMapper.mapLeaderboardToViewData(leaderboard, index + 1)
                }

                _uiState.value = _uiState.value.copy(
                    leaderboard = viewDataList,
                    isLoading = false,
                    errorMessage = null
                )
            }, { error ->
                println("🔴 DEBUG: LeaderboardViewModel - getLeaderboard ERROR: ${error.message}")
                error.printStackTrace()

                val message = if (error is retrofit2.HttpException) {
                    when (error.code()) {
                        401 -> "Необходимо авторизоваться"
                        404 -> "Данные лидерборда не найдены"
                        500 -> "Ошибка сервера"
                        else -> error.message ?: "Ошибка сети (${error.code()})"
                    }
                } else {
                    error.message ?: "Неизвестная ошибка"
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = message
                )
            })
            .addTo(compositeDisposable)
    }

    fun refreshLeaderboard() {
        loadLeaderboard()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}

data class LeaderboardState(
    val leaderboard: List<LeaderboardViewData> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)