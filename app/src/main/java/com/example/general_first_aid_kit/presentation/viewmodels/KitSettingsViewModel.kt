package com.example.general_first_aid_kit.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.general_first_aid_kit.domain.repository.KitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KitSettingsViewModel @Inject constructor(
    private val repository: KitRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(KitSettingsUiState())
    val uiState = _uiState.asStateFlow()

    fun initScreen(initialName: String, initialLocation: String, initialColorIndex: Int) {
        if (_uiState.value.name.isEmpty()) {
            _uiState.update {
                it.copy(
                    name = initialName,
                    location = initialLocation,
                    selectedColorIndex = initialColorIndex
                )
            }
        }
    }

    fun onEvent(event: KitSettingsEvent) {
        when (event) {
            is KitSettingsEvent.NameChanged -> _uiState.update { it.copy(name = event.name) }
            is KitSettingsEvent.LocationChanged -> _uiState.update { it.copy(location = event.location) }
            is KitSettingsEvent.ColorSelected -> _uiState.update { it.copy(selectedColorIndex = event.index) }
            is KitSettingsEvent.TabChanged -> _uiState.update { it.copy(selectedTab = event.index) }
            is KitSettingsEvent.TogglePublic -> _uiState.update { it.copy(isPublic = event.isPublic) }
            is KitSettingsEvent.ToggleExpirationNotify -> _uiState.update { it.copy(notifyExpiration = event.enabled) }
            is KitSettingsEvent.ToggleStockNotify -> _uiState.update { it.copy(notifyLowStock = event.enabled) }
        }
    }

    fun saveChanges(kitId: String, onSuccess: () -> Unit) {
        val state = _uiState.value
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val result = repository.updateKit(
                kitId = kitId,
                name = state.name,
                location = state.location,
                colorIndex = state.selectedColorIndex
            )

            _uiState.update { it.copy(isLoading = false) }
            if (result.isSuccess) {
                onSuccess()
            } else {
                _uiState.update { it.copy(error = "Ошибка при сохранении") }
            }
        }
    }

    fun deleteKit(kitId: String, onSuccess: () -> Unit) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result = repository.deleteKit(kitId)
            if (result.isSuccess) {
                onSuccess()
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Ошибка при удалении") }
            }
        }
    }
}

data class KitSettingsUiState(
    val name: String = "",
    val location: String = "",
    val selectedColorIndex: Int = 0,
    val isPublic: Boolean = false,
    val notifyExpiration: Boolean = false,
    val notifyLowStock: Boolean = false,
    val selectedTab: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class KitSettingsEvent {
    data class NameChanged(val name: String) : KitSettingsEvent()
    data class LocationChanged(val location: String) : KitSettingsEvent()
    data class ColorSelected(val index: Int) : KitSettingsEvent()
    data class TabChanged(val index: Int) : KitSettingsEvent()
    data class TogglePublic(val isPublic: Boolean) : KitSettingsEvent()
    data class ToggleExpirationNotify(val enabled: Boolean) : KitSettingsEvent()
    data class ToggleStockNotify(val enabled: Boolean) : KitSettingsEvent()
}