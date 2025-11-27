package com.example.tictactoeapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.tictactoeapp.domain.repository.AuthRepository
import com.example.tictactoeapp.domain.repository.UserRepository
import com.example.tictactoeapp.mapper.ViewDataToDomainMapper
import com.example.tictactoeapp.presentation.model.LoginViewData
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
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val viewDataToDomainMapper: ViewDataToDomainMapper
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginViewData())
    val uiState: StateFlow<LoginViewData> = _uiState.asStateFlow()

    private val compositeDisposable = CompositeDisposable()

    fun onUsernameChanged(username: String) {
        _uiState.value = _uiState.value.copy(username = username)
    }

    fun onPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun onLoginClicked() {
        val currentState = _uiState.value

        // 1. Создаем ViewData объект из текущего состояния
        val loginViewData = LoginViewData(
            username = currentState.username,
            password = currentState.password
        )

        // 2. Используем маппер для преобразования ViewData -> Domain
        val userDomain = viewDataToDomainMapper.mapLoginViewDataToDomain(loginViewData)

        // 3. Валидация на клиенте (используем уже преобразованные данные)
        val validationError = validateInput(userDomain.username, userDomain.password)
        if (validationError != null) {
            _uiState.value = currentState.copy(errorMessage = validationError)
            return
        }

        _uiState.value = currentState.copy(
            errorMessage = null,
            isLoading = true
        )

        // 4. Используем domain модель для вызова репозитория
        authRepository.login(userDomain.username, userDomain.password)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { user ->
                    // Сохраняем пользователя в базу данных
                    userRepository.saveCurrentUser(user, user.accessToken ?: "")
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            {
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    errorMessage = null,
                                    loginSuccess = true
                                )
                            },
                            { error ->
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    errorMessage = "Ошибка сохранения пользователя: ${error.message}"
                                )
                            }
                        )
                        .addTo(compositeDisposable)
                },
                { error ->
                    val userFriendlyMessage = when {
                        error is IllegalArgumentException -> error.message ?: "Неверные данные"
                        error is retrofit2.HttpException -> {
                            when (error.code()) {
                                401 -> "Неверное имя пользователя или пароль"
                                404 -> "Пользователь не найден"
                                409 -> "Пользователь уже авторизован на другом устройстве"
                                500 -> "Ошибка сервера. Попробуйте позже"
                                else -> "Ошибка сети: ${error.message}"
                            }
                        }
                        error is java.net.ConnectException -> "Не удалось подключиться к серверу"
                        error is java.net.SocketTimeoutException -> "Превышено время ожидания"
                        else -> "Ошибка: ${error.message ?: "Неизвестная ошибка"}"
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = userFriendlyMessage
                    )
                }
            )
            .addTo(compositeDisposable)
    }

    private fun validateInput(username: String, password: String): String? {
        return when {
            username.isEmpty() -> "Имя пользователя обязательно"
            password.isEmpty() -> "Пароль обязателен"
            password.length < 6 -> "Пароль должен быть не менее 6 символов"
            else -> null
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun onLoginSuccessNavigated() {
        _uiState.value = _uiState.value.copy(loginSuccess = false)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
        authRepository.clear()
    }
}