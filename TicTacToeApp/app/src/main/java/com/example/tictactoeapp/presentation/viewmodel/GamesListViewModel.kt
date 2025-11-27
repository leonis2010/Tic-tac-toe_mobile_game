// ./src/main/java/com/example/tictactoeapp/presentation/viewmodel/GamesListViewModel.kt
package com.example.tictactoeapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.tictactoeapp.domain.repository.GameRepository
import com.example.tictactoeapp.mapper.DomainToViewDataMapper
import com.example.tictactoeapp.presentation.model.GamesListViewData
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import javax.inject.Inject

@HiltViewModel
class GamesListViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val domainToViewDataMapper: DomainToViewDataMapper
) : ViewModel() {

    private val _uiState = MutableStateFlow(GamesListViewData())
    val uiState: StateFlow<GamesListViewData> = _uiState.asStateFlow()

    private val compositeDisposable = CompositeDisposable()

    init {
        loadAvailableGames()
    }

    internal fun loadAvailableGames() {
        println("🟡 DEBUG: GamesListViewModel - loadAvailableGames() called")

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        gameRepository.getAvailableGames()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { println("🔵 DEBUG: getAvailableGames SUBSCRIBED") }
            .subscribe({ gamesDomainList ->
                println("🟢 DEBUG: GamesListViewModel - getAvailableGames SUCCESS: ${gamesDomainList.size} games")

                // ДЕТАЛЬНОЕ ЛОГИРОВАНИЕ ВСЕХ ПОЛУЧЕННЫХ ИГР
                gamesDomainList.forEach { game ->
                    println("🔵 DEBUG: Received Game - id=${game.id}, type=${game.gameType}, creator=${game.creatorUsername}")
                }

                // ФИЛЬТРАЦИЯ
                val pvpGames = gamesDomainList.filter { game ->
                    val isPvp = game.gameType == "PVP"
                    if (!isPvp) {
                        println("🔴 DEBUG: FILTERING OUT non-PVP game: id=${game.id}, type=${game.gameType}")
                    }
                    isPvp
                }

                println("🟢 DEBUG: After filtering: ${pvpGames.size} PVP games")

                val gamesViewDataList = pvpGames.map { domainToViewDataMapper.mapGameToGameItemViewData(it) }
                println("🟢 DEBUG: Mapped to view data: ${gamesViewDataList.size} games")

                _uiState.value = _uiState.value.copy(
                    games = gamesViewDataList,
                    isLoading = false,
                    errorMessage = null
                )
            }, { error: Throwable ->
                println("🔴 DEBUG: GamesListViewModel - getAvailableGames ERROR: ${error.message}")
                error.printStackTrace()

                val message = if (error is retrofit2.HttpException) {
                    when (error.code()) {
                        401 -> "Unauthorized. Please log in again."
                        404 -> "Games not found."
                        500 -> "Server error."
                        else -> error.message ?: "Network error (${error.code()})"
                    }
                } else {
                    error.message ?: "Unknown network error"
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = message
                )
            }).addTo(compositeDisposable)
    }

    fun joinGame(gameId: UUID) {
        println("🟡 DEBUG: GamesListViewModel - joinGame called for: $gameId")

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        gameRepository.joinGame(gameId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { println("🔵 DEBUG: joinGame subscribed") }
            .subscribe({ game ->
                println("🟢 DEBUG: GamesListViewModel - joinGame SUCCESS: ${game.id}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    joinSuccess = true,
                    joinedGameId = game.id
                )
            }, { error ->
                println("🔴 DEBUG: GamesListViewModel - joinGame ERROR: ${error.message}")
                error.printStackTrace() // ← ДОБАВЬ ЭТО
                val message = if (error is retrofit2.HttpException) {
                    when (error.code()) {
                        401 -> {
                            // AuthInterceptor уже обработал refresh, просто перезагружаем игры
                            println("🔄 DEBUG: 401 handled by interceptor, reloading games...")
                            loadAvailableGames() // ← ПЕРЕЗАГРУЗКА ВМЕСТО ПОКАЗА ОШИБКИ
                            return@subscribe // ← ВЫХОДИМ ИЗ ОБРАБОТЧИКА ОШИБОК
                        }
                        400 -> "Cannot join this game (might be full or already started)."
                        404 -> "Game not found."
                        500 -> "Server error."
                        else -> error.message ?: "Network error (${error.code()})"
                    }
                } else {
                    error.message ?: "Unknown network error"
                }

                // Показываем только НЕ-401 ошибки
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = message
                )
            }).addTo(compositeDisposable)
    }

    fun clearJoinSuccess() {
        _uiState.value = _uiState.value.copy(joinSuccess = false, joinedGameId = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}