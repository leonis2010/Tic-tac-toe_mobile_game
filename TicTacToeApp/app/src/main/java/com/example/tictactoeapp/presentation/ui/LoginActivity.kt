// ./src/main/java/com/example/tictactoeapp/presentation/ui/LoginActivity.kt
package com.example.tictactoeapp.presentation.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.tictactoeapp.databinding.ActivityLoginBinding
import com.example.tictactoeapp.presentation.viewmodel.LoginViewModel
import com.example.tictactoeapp.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    @Inject
    lateinit var sessionManager: SessionManager

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkExistingSession()
        setupUI()
        observeViewModel()
    }

    private fun checkExistingSession() {
        // Проверяем, есть ли валидная сессия
        if (sessionManager.isSessionValid()) {
            val lastUsername = sessionManager.getLastUsername()
            println("🔄 DEBUG: Valid session found for user: $lastUsername")
            // Автоматически переходим к списку игр
            navigateToGamesList()
        } else {
            println("🔄 DEBUG: No valid session found")
        }
    }
    private fun navigateToGamesList() {
        val intent = Intent(this, GamesListActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setupUI() {
        // Обработчики текстовых полей
        binding.editTextUsername.doOnTextChanged { text, _, _, _ ->
            viewModel.onUsernameChanged(text.toString())
        }
        binding.editTextPassword.doOnTextChanged { text, _, _, _ ->
            viewModel.onPasswordChanged(text.toString())
        }
        // Обработчики кнопок
        binding.buttonLogin.setOnClickListener {
            viewModel.onLoginClicked()
        }
        binding.buttonRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        // Переключатель видимости пароля
        binding.checkBoxShowPassword.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.editTextPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT
            } else {
                binding.editTextPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or
                        android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            // Сохраняем позицию курсора
            binding.editTextPassword.setSelection(binding.editTextPassword.text?.length ?: 0)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateLoadingState(state.isLoading)
                    state.errorMessage?.let { showError(it) }

                    if (state.loginSuccess) {
                        // Помечаем сессию как валидную и сохраняем пользователя
                        sessionManager.markSessionValid()
                        sessionManager.saveLastUsername(state.username)

                        navigateToGamesList()
                        viewModel.onLoginSuccessNavigated()
                    }
                }
            }
        }
    }

    private fun updateLoadingState(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.buttonLogin.isEnabled = !isLoading
        binding.buttonRegister.isEnabled = !isLoading
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
        println("DEBUG: LoginActivity - onBackPressed() - exiting app")
        // Полный выход из приложения
        exitAppCompletely()
    }

    private fun exitAppCompletely() {
        println("DEBUG: Exiting app completely")

        // Помечаем сессию как невалидную
        sessionManager.markSessionInvalid()
        sessionManager.setGracefulExit()

        // Создаем Intent для возврата на домашний экран
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        startActivity(intent)
        finishAffinity()
    }
}
