package com.example.general_first_aid_kit.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.general_first_aid_kit.domain.model.KitType
import com.example.general_first_aid_kit.domain.usecase.CreateKitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateKitViewModel @Inject constructor(
    private val createKitUseCase: CreateKitUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateKitState())
    val uiState: StateFlow<CreateKitState> = _uiState.asStateFlow()

    fun onNameChange(text: String) {
        _uiState.update { it.copy(name = text) }
    }

    fun onLocationChange(text: String) {
        _uiState.update { it.copy(location = text) }
    }

    fun onColorSelected(index: Int) {
        _uiState.update { it.copy(colorIndex = index) }
    }

    fun onTypeChange(isShared: Boolean) {
        _uiState.update { it.copy(isShared = isShared) }
    }

    fun createKit(onSuccess: () -> Unit) {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = createKitUseCase(
                name = state.name,
                location = state.location,
                colorIndex = state.colorIndex,
                type = if (state.isShared) KitType.SHARED else KitType.PERSONAL
            )

            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false) }
                onSuccess()
            } else {
                _uiState.update {
                    it.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "Ошибка")
                }
            }
        }
    }
}

data class CreateKitState(
    val name: String = "",
    val location: String = "",
    val colorIndex: Int = 0,
    val isShared: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)
