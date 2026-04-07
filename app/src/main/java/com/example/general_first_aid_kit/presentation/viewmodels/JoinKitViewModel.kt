package com.example.general_first_aid_kit.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.general_first_aid_kit.domain.usecase.GetUserUseCase
import com.example.general_first_aid_kit.domain.usecase.JoinKitByCodeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JoinKitViewModel @Inject constructor(
    private val joinKitByCodeUseCase: JoinKitByCodeUseCase,
    private val getUserUseCase: GetUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(JoinKitUiState())
    val uiState = _uiState.asStateFlow()

    fun joinKit(inviteCode: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val user = getUserUseCase()
            if (user == null) {
                _uiState.update { it.copy(isLoading = false, error = "Пользователь не авторизован") }
                return@launch
            }

            val result = joinKitByCodeUseCase(user.id, inviteCode)

            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false) }
                onSuccess()
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Не удалось присоединиться"
                    )
                }
            }
        }
    }
}

data class JoinKitUiState(val isLoading: Boolean = false, val error: String? = null)