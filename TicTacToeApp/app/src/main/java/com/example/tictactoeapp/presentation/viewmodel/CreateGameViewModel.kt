package com.example.tictactoeapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.tictactoeapp.domain.repository.GameRepository
import com.example.tictactoeapp.presentation.model.CreateGameViewData
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class CreateGameViewModel @Inject constructor(
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateGameViewData())
    val uiState: StateFlow<CreateGameViewData> = _uiState.asStateFlow()

    // Поля для хранения Disposable
    private var createGameWithComputerDisposable: Disposable? = null
    private var createGameWithPlayerDisposable: Disposable? = null

    fun createGameWithComputer() {
        println("🟡 DEBUG: CreateGameViewModel - createGameWithComputer() called")

        createGameWithComputerDisposable?.dispose()

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        createGameWithComputerDisposable = gameRepository.startNewGameWithComputer()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { game ->
                    println("🟢 DEBUG: COMPUTER game created - ID: ${game.id}, Status: ${game.status}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        createSuccess = true,
                        createdGameId = game.id,
                        gameType = "COMPUTER"
                    )
                },
                { error ->
                    println("🔴 DEBUG: Error: ${error.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to create game with computer"
                    )
                }
            )
    }

    fun createGameWithPlayer() {
        println("🟡 DEBUG: CreateGameViewModel - createGameWithPlayer() called")

        createGameWithPlayerDisposable?.dispose()

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        createGameWithPlayerDisposable = gameRepository.startNewGameWithPlayer()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { game ->
                    println("🟢 DEBUG: PLAYER game created - ID: ${game.id}, Status: ${game.status}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        createSuccess = true,
                        createdGameId = game.id,
                        gameType = "PLAYER"
                    )
                },
                { error ->
                    println("🔴 DEBUG: Error: ${error.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to create game with player"
                    )
                }
            )
    }

    fun clearCreateSuccess() {
        _uiState.value = _uiState.value.copy(createSuccess = false, createdGameId = null, gameType = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        // Отменяем все подписки при уничтожении ViewModel
        createGameWithComputerDisposable?.dispose()
        createGameWithPlayerDisposable?.dispose()
    }
}