package com.example.tictactoeapp.presentation.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.tictactoeapp.databinding.ActivityRegisterBinding
import com.example.tictactoeapp.presentation.viewmodel.RegisterViewModel
import com.example.tictactoeapp.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {
    @Inject
    lateinit var sessionManager: SessionManager

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeViewModel()
        observeNavigation()
    }


    private fun setupUI() {
        // Обработчики текстовых полей
        binding.editTextUsername.doOnTextChanged { text, _, _, _ ->
            viewModel.onUsernameChanged(text.toString())
        }

        binding.editTextPassword.doOnTextChanged { text, _, _, _ ->
            viewModel.onPasswordChanged(text.toString())
        }

        binding.editTextConfirmPassword.doOnTextChanged { text, _, _, _ ->
            viewModel.onConfirmPasswordChanged(text.toString())
        }

        // Обработчики кнопок
        binding.buttonRegister.setOnClickListener {
            println("DEBUG: Register button clicked")
            viewModel.onRegisterClicked()
        }

        binding.buttonLogin.setOnClickListener {
            finish() // Возвращаемся к экрану логина
        }

        // Переключатели видимости пароля
        setupPasswordVisibility()
    }

    private fun setupPasswordVisibility() {
        binding.checkBoxShowPassword.setOnCheckedChangeListener { _, isChecked ->
            updatePasswordVisibility(isChecked, binding.editTextPassword)
        }

        binding.checkBoxShowConfirmPassword.setOnCheckedChangeListener { _, isChecked ->
            updatePasswordVisibility(isChecked, binding.editTextConfirmPassword)
        }
    }

    private fun updatePasswordVisibility(isChecked: Boolean, editText: com.google.android.material.textfield.TextInputEditText) {
        if (isChecked) {
            editText.inputType = android.text.InputType.TYPE_CLASS_TEXT
        } else {
            editText.inputType = android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        editText.setSelection(editText.text?.length ?: 0)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateLoadingState(state.isLoading)
                    state.errorMessage?.let { showError(it) }
                }
            }
        }
    }

    private fun observeNavigation() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigateToLogin.collect { shouldNavigate ->
                    if (shouldNavigate) {
                        // Показываем сообщение об успехе и возвращаемся на логин
                        Toast.makeText(
                            this@RegisterActivity,
                            "Registration successful! Please login",
                            Toast.LENGTH_LONG
                        ).show()
                        // Возвращаемся на LoginActivity
                        finish()
                        // Сбрасываем флаг навигации
                        viewModel.onNavigationCompleted()
                    }
                }
            }
        }
    }

    private fun updateLoadingState(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.buttonRegister.isEnabled = !isLoading
        binding.buttonLogin.isEnabled = !isLoading
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
        println("DEBUG: RegisterActivity - onBackPressed() - returning to login")
        // Просто завершаем активность - вернемся к LoginActivity
        finish()
        // анимацию перехода
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
    }
}