package com.example.general_first_aid_kit.presentation.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.general_first_aid_kit.R
import com.example.general_first_aid_kit.domain.usecase.SendPasswordResetEmailUseCase
import com.example.general_first_aid_kit.domain.util.AuthValidator
import com.example.general_first_aid_kit.domain.util.ValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<ForgotPasswordState>(ForgotPasswordState.Idle)
    val uiState: StateFlow<ForgotPasswordState> = _uiState.asStateFlow()

    private val _emailError = MutableStateFlow<Int?>(null)
    val emailError: StateFlow<Int?> = _emailError.asStateFlow()

    fun onEmailChange(email: String) {
        if (_emailError.value != null) {
            _emailError.value = (AuthValidator.validateEmail(email) as? ValidationResult.Error)?.messageResId
        }
    }

    fun onSendClick(email: String) {
        val result = AuthValidator.validateEmail(email)
        if (result is ValidationResult.Error) {
            _emailError.value = result.messageResId
            return
        }
        _emailError.value = null

        viewModelScope.launch {
            _uiState.value = ForgotPasswordState.Loading
            sendPasswordResetEmailUseCase(email)
                .onSuccess { _uiState.value = ForgotPasswordState.Success }
                .onFailure { _uiState.value = ForgotPasswordState.Error(mapError(it)) }
        }
    }

    fun resetState() {
        _uiState.value = ForgotPasswordState.Idle
    }

    private fun mapError(error: Throwable): String {
        val msg = error.message ?: ""
        return when {
            "network" in msg -> context.getString(R.string.offline_no_internet)
            else -> context.getString(R.string.error_unknown_auth)
        }
    }
}

sealed interface ForgotPasswordState {
    data object Idle : ForgotPasswordState
    data object Loading : ForgotPasswordState
    data object Success : ForgotPasswordState
    data class Error(val message: String) : ForgotPasswordState
}
