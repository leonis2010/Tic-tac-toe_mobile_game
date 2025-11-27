// ./src/main/java/com/example/tictactoeapp/presentation/viewmodel/RegisterViewModel.kt
package com.example.tictactoeapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.tictactoeapp.domain.repository.AuthRepository
import com.example.tictactoeapp.mapper.ViewDataToDomainMapper
import com.example.tictactoeapp.presentation.model.RegisterViewData
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
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val viewDataToDomainMapper: ViewDataToDomainMapper
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterViewData())
    val uiState: StateFlow<RegisterViewData> = _uiState.asStateFlow()

    private val _navigateToLogin = MutableStateFlow(false)
    val navigateToLogin: StateFlow<Boolean> = _navigateToLogin.asStateFlow()

    private val compositeDisposable = CompositeDisposable()

    fun onUsernameChanged(username: String) {
        _uiState.value = _uiState.value.copy(username = username)
    }

    fun onPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = confirmPassword)
    }

    fun onRegisterClicked() {
        println("👤 DEBUG: RegisterViewModel.onRegisterClicked called")

        val currentState = _uiState.value

        // 1. Создаем ViewData объект
        val registerViewData = RegisterViewData(
            username = currentState.username,
            password = currentState.password,
            confirmPassword = currentState.confirmPassword
        )

        // 2. Используем маппер
        val userDomain = viewDataToDomainMapper.mapRegisterViewDataToDomain(registerViewData)

        val username = userDomain.username.trim()
        val password = userDomain.password.trim()
        val confirmPassword = currentState.confirmPassword.trim() // confirmPassword нет в Domain модели

        println("👤 DEBUG: Username: '$username', Password: '$password', Confirm: '$confirmPassword'")

        // Валидация
        if (username.isEmpty()) {
            println("🔴 DEBUG: Validation failed - username empty")
            _uiState.value = currentState.copy(errorMessage = "Имя пользователя обязательно")
            return
        }

        if (password.isEmpty()) {
            println("🔴 DEBUG: Validation failed - password empty")
            _uiState.value = currentState.copy(errorMessage = "Пароль обязателен")
            return
        }

        if (password.length < 6) {
            println("🔴 DEBUG: Validation failed - password too short")
            _uiState.value = currentState.copy(errorMessage = "Пароль должен быть не менее 6 символов")
            return
        }

        if (password != confirmPassword) {
            println("🔴 DEBUG: Validation failed - passwords don't match")
            _uiState.value = currentState.copy(errorMessage = "Пароли не совпадают")
            return
        }

        println("🟢 DEBUG: Validation passed, calling authRepository.register")

        _uiState.value = currentState.copy(
            errorMessage = null,
            isLoading = true
        )

        // 3. Используем domain модель
        authRepository.register(userDomain.username, userDomain.password)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { user ->
                    println("🎉 DEBUG: RegisterViewModel - Registration SUCCESS: ${user.username}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = null
                    )
                    _navigateToLogin.value = true
                },
                { error ->
                    println("💥 DEBUG: RegisterViewModel - Registration ERROR: ${error.message}")

                    val userFriendlyMessage = when {
                        error is retrofit2.HttpException && error.code() == 400 ->
                            "Игрок с таким именем уже зарегистрирован"
                        error is retrofit2.HttpException && error.code() == 409 ->
                            "Игрок с таким именем уже зарегистрирован"
                        error is java.net.ConnectException ->
                            "Не удалось подключиться к серверу"
                        error is java.net.SocketTimeoutException ->
                            "Превышено время ожидания"
                        else -> error.message ?: "Ошибка регистрации"
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = userFriendlyMessage
                    )
                }
            )
            .addTo(compositeDisposable)
    }

    // метод для сброса флага навигации
    fun onNavigationCompleted() {
        _navigateToLogin.value = false
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear() // Отменяем все RxJava подписки
        authRepository.clear()
    }
}