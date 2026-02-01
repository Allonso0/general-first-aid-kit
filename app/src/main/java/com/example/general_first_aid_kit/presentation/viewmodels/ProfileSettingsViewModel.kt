package com.example.general_first_aid_kit.presentation.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.general_first_aid_kit.domain.usecase.GetUserUseCase
import com.example.general_first_aid_kit.domain.usecase.UpdateUserUseCase
import com.example.general_first_aid_kit.domain.util.AuthValidator
import com.example.general_first_aid_kit.domain.util.ValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileSettingsViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val updateUserUseCase: UpdateUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileSettingsState())
    val uiState: StateFlow<ProfileSettingsState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        val user = getUserUseCase()
        if (user != null) {
            _uiState.update {
                it.copy(
                    name = user.name,
                    currentAvatarUrl = user.avatarURL
                )
            }
        }
    }

    fun onNameChange(newName: String) {
        _uiState.update { it.copy(name = newName) }
    }

    fun onAvatarSelected(uri: Uri?) {
        _uiState.update { it.copy(newAvatarUri = uri) }
    }

    fun saveChanges() {
        val currentState = _uiState.value

        val isNameValid = AuthValidator.validateName(currentState.name)
        if (isNameValid is ValidationResult.Error) {
            _uiState.update { it.copy(errorMessage = isNameValid.message) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = updateUserUseCase(
                name = currentState.name,
                photoUri = currentState.newAvatarUri
            )

            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Ошибка сохранения"
                    )
                }
            }
        }
    }

    fun resetSuccessState() {
        _uiState.update { it.copy(isSuccess = false) }
    }
}

data class ProfileSettingsState(
    val name: String = "",
    val currentAvatarUrl: String? = null,
    val newAvatarUri: Uri? = null,

    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)