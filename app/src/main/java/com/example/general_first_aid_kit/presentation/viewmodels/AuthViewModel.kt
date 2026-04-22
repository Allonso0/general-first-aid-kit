package com.example.general_first_aid_kit.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.general_first_aid_kit.domain.usecase.CheckAuthUseCase
import com.example.general_first_aid_kit.domain.usecase.SignInUseCase
import com.example.general_first_aid_kit.domain.usecase.SignOutUseCase
import com.example.general_first_aid_kit.domain.usecase.SignUpUseCase
import com.example.general_first_aid_kit.domain.util.AuthValidator
import com.example.general_first_aid_kit.domain.util.ValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val signUpUseCase: SignUpUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val checkAuthUseCase: CheckAuthUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthState>(AuthState.Idle)
    val uiState: StateFlow<AuthState> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        checkInitialAuth()
    }

    private fun checkInitialAuth() {
        viewModelScope.launch {
            delay(1500)
            checkAuthUseCase()
            _isLoading.value = false
        }
    }

    fun onLoginClick(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthState.Loading
            signInUseCase(email, password)
                .onSuccess { _uiState.value = AuthState.Authenticated }
                .onFailure { _uiState.value = AuthState.Error(it.message ?: "Ошибка при входе") }
        }
    }

    fun onRegisterClick(email: String, password: String, username: String) {
        val nameResult = AuthValidator.validateName(username)
        val emailResult = AuthValidator.validateEmail(email)
        val passwordResult = AuthValidator.validatePassword(password)

        val validationErrorResId = when {
            nameResult is ValidationResult.Error -> nameResult.messageResId
            emailResult is ValidationResult.Error -> emailResult.messageResId
            passwordResult is ValidationResult.Error -> passwordResult.messageResId
            else -> null
        }

        if (validationErrorResId != null) {
            _uiState.value = AuthState.ErrorRes(validationErrorResId)
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthState.Loading
            signUpUseCase(email, password, username)
                .onSuccess { _uiState.value = AuthState.Authenticated }
                .onFailure { _uiState.value = AuthState.Error(it.message ?: "Ошибка регистрации") }
        }
    }

    fun onSignOutClick() {
        _uiState.value = AuthState.Idle
        signOutUseCase()
    }

    fun isLogged() = checkAuthUseCase()

    fun resetState() {
        _uiState.value = AuthState.Idle
    }
}

sealed interface AuthState {
    data object Idle : AuthState
    data object Loading : AuthState
    data object Authenticated : AuthState
    data class Error(val message: String) : AuthState
    data class ErrorRes(val resId: Int) : AuthState
}
