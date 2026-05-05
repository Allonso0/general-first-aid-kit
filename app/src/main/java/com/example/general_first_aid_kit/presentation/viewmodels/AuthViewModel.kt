package com.example.general_first_aid_kit.presentation.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.general_first_aid_kit.R
import com.example.general_first_aid_kit.domain.usecase.CheckAuthUseCase
import com.example.general_first_aid_kit.domain.usecase.SignInUseCase
import com.example.general_first_aid_kit.domain.usecase.SignOutUseCase
import com.example.general_first_aid_kit.domain.usecase.SignUpUseCase
import com.example.general_first_aid_kit.domain.util.AuthValidator
import com.example.general_first_aid_kit.domain.util.ValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val signUpUseCase: SignUpUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val checkAuthUseCase: CheckAuthUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthState>(AuthState.Idle)
    val uiState: StateFlow<AuthState> = _uiState.asStateFlow()

    private val _formErrors = MutableStateFlow(AuthFormErrors())
    val formErrors: StateFlow<AuthFormErrors> = _formErrors.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        checkInitialAuth()
    }

    private fun checkInitialAuth() {
        viewModelScope.launch {
            delay(SPLASH_DELAY_MS)
            _isLoading.value = false
        }
    }

    private companion object {
        const val SPLASH_DELAY_MS = 1500L
    }

    fun validateEmailField(email: String) {
        val result = AuthValidator.validateEmail(email)
        _formErrors.update { it.copy(emailError = (result as? ValidationResult.Error)?.messageResId) }
    }

    fun validatePasswordField(password: String) {
        val result = AuthValidator.validatePassword(password)
        _formErrors.update { it.copy(passwordError = (result as? ValidationResult.Error)?.messageResId) }
    }

    fun validateNameField(name: String) {
        val result = AuthValidator.validateName(name)
        _formErrors.update { it.copy(nameError = (result as? ValidationResult.Error)?.messageResId) }
    }

    fun onLoginClick(email: String, password: String) {
        val emailResult = AuthValidator.validateEmail(email)
        val passwordResult = AuthValidator.validatePassword(password)

        _formErrors.update {
            it.copy(
                emailError = (emailResult as? ValidationResult.Error)?.messageResId,
                passwordError = (passwordResult as? ValidationResult.Error)?.messageResId
            )
        }

        if (emailResult is ValidationResult.Error || passwordResult is ValidationResult.Error) return

        viewModelScope.launch {
            _uiState.value = AuthState.Loading
            signInUseCase(email, password)
                .onSuccess { _uiState.value = AuthState.Authenticated }
                .onFailure { _uiState.value = AuthState.Error(mapFirebaseError(it)) }
        }
    }

    fun onRegisterClick(email: String, password: String, username: String) {
        val nameResult = AuthValidator.validateName(username)
        val emailResult = AuthValidator.validateEmail(email)
        val passwordResult = AuthValidator.validatePassword(password)

        _formErrors.update {
            it.copy(
                nameError = (nameResult as? ValidationResult.Error)?.messageResId,
                emailError = (emailResult as? ValidationResult.Error)?.messageResId,
                passwordError = (passwordResult as? ValidationResult.Error)?.messageResId
            )
        }

        if (nameResult is ValidationResult.Error || emailResult is ValidationResult.Error || passwordResult is ValidationResult.Error) return

        viewModelScope.launch {
            _uiState.value = AuthState.Loading
            signUpUseCase(email, password, username)
                .onSuccess { _uiState.value = AuthState.Authenticated }
                .onFailure { _uiState.value = AuthState.Error(mapFirebaseError(it)) }
        }
    }

    private fun mapFirebaseError(error: Throwable): String {
        val msg = error.message ?: ""
        return when {
            "no user record" in msg || "user-not-found" in msg ->
                context.getString(R.string.error_user_not_found)
            "password is invalid" in msg || "wrong-password" in msg ->
                context.getString(R.string.error_wrong_password)
            "email address is already in use" in msg || "email-already-in-use" in msg ->
                context.getString(R.string.error_email_in_use)
            "too-many-requests" in msg || "too many" in msg ->
                context.getString(R.string.error_too_many_requests)
            "network" in msg ->
                context.getString(R.string.offline_no_internet)
            else -> context.getString(R.string.error_unknown_auth)
        }
    }

    fun onSignOutClick() {
        _uiState.value = AuthState.Idle
        signOutUseCase()
    }

    fun isLogged() = checkAuthUseCase()

    fun resetState() {
        _uiState.value = AuthState.Idle
        _formErrors.value = AuthFormErrors()
    }
}

data class AuthFormErrors(
    val nameError: Int? = null,
    val emailError: Int? = null,
    val passwordError: Int? = null
)

sealed interface AuthState {
    data object Idle : AuthState
    data object Loading : AuthState
    data object Authenticated : AuthState
    data class Error(val message: String) : AuthState
}
