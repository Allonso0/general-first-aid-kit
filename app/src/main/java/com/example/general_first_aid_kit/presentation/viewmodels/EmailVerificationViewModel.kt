package com.example.general_first_aid_kit.presentation.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.general_first_aid_kit.R
import com.example.general_first_aid_kit.domain.usecase.CheckEmailVerifiedUseCase
import com.example.general_first_aid_kit.domain.usecase.SendEmailVerificationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmailVerificationViewModel @Inject constructor(
    private val checkEmailVerifiedUseCase: CheckEmailVerifiedUseCase,
    private val sendEmailVerificationUseCase: SendEmailVerificationUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<EmailVerificationState>(EmailVerificationState.Idle)
    val uiState: StateFlow<EmailVerificationState> = _uiState.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun onCheckClick() {
        viewModelScope.launch {
            _uiState.value = EmailVerificationState.Loading
            checkEmailVerifiedUseCase()
                .onSuccess { isVerified ->
                    if (isVerified) {
                        _uiState.value = EmailVerificationState.Verified
                    } else {
                        _uiState.value = EmailVerificationState.Idle
                        _snackbarMessage.value = context.getString(R.string.email_not_verified)
                    }
                }
                .onFailure {
                    _uiState.value = EmailVerificationState.Idle
                    _snackbarMessage.value = context.getString(R.string.error_unknown_auth)
                }
        }
    }

    fun onResendClick() {
        viewModelScope.launch {
            sendEmailVerificationUseCase()
                .onSuccess { _snackbarMessage.value = context.getString(R.string.email_resend_success) }
                .onFailure { _snackbarMessage.value = context.getString(R.string.error_unknown_auth) }
        }
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
}

sealed interface EmailVerificationState {
    data object Idle : EmailVerificationState
    data object Loading : EmailVerificationState
    data object Verified : EmailVerificationState
}
