// ./src/main/java/com/example/tictactoeapp/presentation/viewmodel/CurrentGameViewModel.kt
package com.example.tictactoeapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tictactoeapp.domain.model.Game
import com.example.tictactoeapp.domain.model.GameBoard
import com.example.tictactoeapp.domain.repository.GameRepository
import com.example.tictactoeapp.domain.repository.UserRepository
import com.example.tictactoeapp.presentation.model.CurrentGameViewData
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CurrentGameViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CurrentGameViewData())
    val uiState: StateFlow<CurrentGameViewData> = _uiState.asStateFlow()

    private var gameId: UUID? = null
    private var updateJob: Job? = null
    private var currentUserUsername: String? = null

    val currentUsername: String?
        get() = currentUserUsername

    private val compositeDisposable = CompositeDisposable()

    sealed class NavigationEvent {
        object NavigateToLogin : NavigationEvent()
    }

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: kotlinx.coroutines.flow.SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()
    val isMyTurn: Boolean
        get() = _uiState.value.gameStatus == "IN_PROGRESS" &&
                _uiState.value.currentPlayerUsername == currentUserUsername

    fun loadGame(gameId: UUID) {
        println("DEBUG: CurrentGameViewModel - loadGame() called with: $gameId")

        this.gameId = gameId
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        // Сначала загружаем пользователя
        userRepository.getCurrentUser()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { currentUser ->
                    println("DEBUG: CurrentGameViewModel - User loaded SUCCESS: ${currentUser?.username}")
                    this.currentUserUsername = currentUser?.username

                    // Теперь загружаем игру
                    loadGameData(gameId)
                },
                { error ->
                    println("DEBUG: CurrentGameViewModel - ERROR getting user: ${error.message}")
                    // Все равно пытаемся загрузить игру, но с null username
                    loadGameData(gameId)
                }
            )
            .addTo(compositeDisposable)
    }

    private fun loadGameData(gameId: UUID) {
        println("DEBUG: CurrentGameViewModel - loadGameData() called, currentUser: $currentUserUsername")

        gameRepository.getGame(gameId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { game ->
                    println("DEBUG: CurrentGameViewModel - Game loaded:")
                    println("DEBUG: - Creator: ${game.creatorUsername}")
                    println("DEBUG: - Player2: ${game.player2Username}")
                    println("DEBUG: - Current Player: ${game.currentPlayerUsername}")
                    println("DEBUG: - Status: ${game.status}")
                    println("DEBUG: - Winner: '${game.winner}'")

                    // Определяем имена игроков для отображения
                    val player1Name = game.creatorUsername ?: "Unknown Creator"
                    val player2DisplayName = when {
                        game.gameType == "PVE" -> "COMPUTER"
                        game.player2Username != null -> game.player2Username
                        else -> "Waiting for player..."
                    }

                    _uiState.value = CurrentGameViewData(
                        gameId = game.id,
                        player1Username = player1Name,
                        player2Username = player2DisplayName,
                        player1Symbol = "X",
                        player2Symbol = "O",
                        currentPlayerUsername = game.currentPlayerUsername,
                        board = game.board.board.copyOf(),
                        gameStatus = game.status,
                        winner = game.winner,
                        isLoading = false,
                        errorMessage = null
                    )

                    println("DEBUG: CurrentGameViewModel - UI state updated with winner: '${game.winner}'")
                    startPeriodicUpdates()
                },
                { error ->
                    println("DEBUG: CurrentGameViewModel - ERROR loading game: ${error.message}")
                    handleNetworkError(error)
                }
            )
            .addTo(compositeDisposable)
    }

    fun makeMove(row: Int, col: Int) {
        val currentState = _uiState.value
        val gameId = currentState.gameId ?: return
        val board = currentState.board

        val userIdentifier = "[USER:${currentUserUsername?.take(3)}...]"
        println("$userIdentifier DEBUG: CurrentGameViewModel - makeMove called: row=$row, col=$col")
        println("$userIdentifier DEBUG: Game status: ${currentState.gameStatus}")
        println("$userIdentifier DEBUG: Current player: ${currentState.currentPlayerUsername}")
        println("$userIdentifier DEBUG: My Username: $currentUserUsername")
        println("$userIdentifier DEBUG: isMyTurn: ${isMyTurn}")

        // ПРОВЕРКА ХОДА НА ОСНОВЕ АКТУАЛЬНЫХ ДАННЫХ С СЕРВЕРА
        val canMakeMove = currentState.gameStatus == "IN_PROGRESS" &&
                currentState.currentPlayerUsername == currentUserUsername &&
                row in board.indices &&
                col in board[row].indices &&
                board[row][col] == 0

        if (!canMakeMove) {
            println("$userIdentifier DEBUG: Cannot make move - not your turn or invalid cell")
            return
        }

        println("$userIdentifier DEBUG: Move validation passed")

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        val newBoardArray = board.map { it.copyOf() }.toTypedArray()

        // ОПРЕДЕЛЯЕМ СИМВОЛ ИГРОКА
        val playerSymbol = if (currentUserUsername == currentState.player1Username) {
            1 // X - создатель игры
        } else {
            -1 // O - второй игрок
        }

        newBoardArray[row][col] = playerSymbol

        println("$userIdentifier DEBUG: New board with symbol $playerSymbol: ${newBoardArray.contentDeepToString()}")

        val newGameBoard = GameBoard(newBoardArray)

        // ОПРЕДЕЛЯЕМ СЛЕДУЮЩЕГО ИГРОКА (ДЛЯ PVP)
        val nextPlayer = if (currentState.player2Username != null && currentState.player2Username != "Waiting for player...") {
            // PVP игра - передаем ход другому игроку
            if (currentUserUsername == currentState.player1Username) {
                currentState.player2Username
            } else {
                currentState.player1Username
            }
        } else {
            // PVE игра - передаем ход компьютеру
            "COMPUTER"
        }

        val updatedGame = Game(
            id = gameId,
            board = newGameBoard,
            isPlayerTurn = false,
            status = "IN_PROGRESS",
            creatorUsername = currentState.player1Username,
            player2Username = currentState.player2Username,
            currentPlayerUsername = nextPlayer
        )

        println("$userIdentifier DEBUG: Sending move to server, next player: $nextPlayer")

        gameRepository.makeMove(gameId, updatedGame)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { gameFromServer ->
                    println("$userIdentifier DEBUG: Move successful on server")
                    println("$userIdentifier DEBUG: Server response - status: ${gameFromServer.status}")
                    println("$userIdentifier DEBUG: Server response - currentPlayer: ${gameFromServer.currentPlayerUsername}")
                    println("$userIdentifier DEBUG: Server response - winner: ${gameFromServer.winner}")
                    // ОБНОВЛЯЕМ UI С ДАННЫМИ С СЕРВЕРА
                    _uiState.value = _uiState.value.copy(
                        currentPlayerUsername = gameFromServer.currentPlayerUsername,
                        board = gameFromServer.board.board.copyOf(),
                        gameStatus = gameFromServer.status,
                        winner = gameFromServer.winner,
                        isLoading = false,
                        errorMessage = null
                    )

                    println("$userIdentifier DEBUG: UI updated, current player: ${gameFromServer.currentPlayerUsername}")
                },
                { error ->
                    println("$userIdentifier DEBUG: ERROR making move: ${error.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Move failed: ${error.message}"
                    )
                }
            )
            .addTo(compositeDisposable)
    }

    fun updateGame() {
        gameId?.let { id ->
            gameRepository.getGame(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { game ->
                        println("🔄 DEBUG: updateGame - received from server:")
                        println("🔄 DEBUG: - Status: ${game.status}")
                        println("🔄 DEBUG: - Winner: '${game.winner}'")
                        println("🔄 DEBUG: - CurrentPlayer: ${game.currentPlayerUsername}")

                        val currentState = _uiState.value

                        val shouldUpdate = currentState.gameStatus != game.status ||
                                currentState.currentPlayerUsername != game.currentPlayerUsername ||
                                currentState.player2Username != (game.player2Username ?: "Waiting for player...") ||
                                currentState.winner != game.winner ||
                                !currentState.board.contentDeepEquals(game.board.board)

                        if (shouldUpdate) {
                            _uiState.value = currentState.copy(
                                gameStatus = game.status,
                                currentPlayerUsername = game.currentPlayerUsername,
                                player2Username = game.player2Username ?: "Waiting for player...",
                                board = game.board.board.copyOf(),
                                winner = game.winner
                            )
                            println("🔄 DEBUG: UI UPDATED with winner: '${game.winner}'")
                        }
                    },
                    { error ->
                        println("DEBUG: CurrentGameViewModel - Background update failed: ${error.message}")
                    }
                )
                .addTo(compositeDisposable)
        }
    }

    private fun startPeriodicUpdates() {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            while (true) {
                delay(2000) // Обновлять каждые 2 секунды
                updateGame()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        updateJob?.cancel()
        compositeDisposable.clear()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun handleNetworkError(error: Throwable) {
        val message = if (error is retrofit2.HttpException) {
            when (error.code()) {
                401 -> {
                    viewModelScope.launch {
                        _navigationEvent.emit(NavigationEvent.NavigateToLogin)
                    }
                    "Unauthorized. Redirecting to login..."
                }
                404 -> "Game not found."
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
    }

    fun leaveGame() {
        gameId?.let { id ->
            println("DEBUG: CurrentGameViewModel - leaveGame called for: $id")

            // Отправляем запрос на сервер о выходе игрока
            gameRepository.playerLeftGame(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { game ->
                        println("DEBUG: Player left game successfully, status: ${game.status}, winner: ${game.winner}")
                    },
                    { error ->
                        println("DEBUG: Error sending leave notification: ${error.message}")
                    }
                )
                .addTo(compositeDisposable)
        }
    }
}