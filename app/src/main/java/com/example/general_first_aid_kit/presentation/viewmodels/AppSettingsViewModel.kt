package com.example.general_first_aid_kit.presentation.viewmodels

import androidx.lifecycle.ViewModel
import com.example.general_first_aid_kit.domain.usecase.GetAppSettingsUseCase
import com.example.general_first_aid_kit.domain.usecase.SaveAppSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AppSettingsViewModel @Inject constructor(
    private val getAppSettings: GetAppSettingsUseCase,
    private val saveAppSettings: SaveAppSettingsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppSettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        val settings = getAppSettings()
        _uiState.update {
            it.copy(
                lowStockThreshold = settings.lowStockThreshold.toString(),
                expiryWarningDays = settings.expiryWarningDays.toString()
            )
        }
    }

    fun onEvent(event: AppSettingsEvent) {
        when (event) {
            is AppSettingsEvent.LowStockThresholdChanged ->
                _uiState.update { it.copy(lowStockThreshold = event.value) }
            is AppSettingsEvent.ExpiryWarningDaysChanged ->
                _uiState.update { it.copy(expiryWarningDays = event.value) }
            AppSettingsEvent.LowStockThresholdCommitted -> commitLowStockThreshold()
            AppSettingsEvent.ExpiryWarningDaysCommitted -> commitExpiryWarningDays()
        }
    }

    private fun commitLowStockThreshold() {
        val value = _uiState.value.lowStockThreshold.toIntOrNull()
        if (value != null && value > 0) {
            saveAppSettings(getAppSettings().copy(lowStockThreshold = value))
        } else {
            _uiState.update { it.copy(lowStockThreshold = getAppSettings().lowStockThreshold.toString()) }
        }
    }

    private fun commitExpiryWarningDays() {
        val value = _uiState.value.expiryWarningDays.toIntOrNull()
        if (value != null && value > 0) {
            saveAppSettings(getAppSettings().copy(expiryWarningDays = value))
        } else {
            _uiState.update { it.copy(expiryWarningDays = getAppSettings().expiryWarningDays.toString()) }
        }
    }
}

data class AppSettingsUiState(
    val lowStockThreshold: String = "2",
    val expiryWarningDays: String = "7"
)

sealed class AppSettingsEvent {
    data class LowStockThresholdChanged(val value: String) : AppSettingsEvent()
    data class ExpiryWarningDaysChanged(val value: String) : AppSettingsEvent()
    data object LowStockThresholdCommitted : AppSettingsEvent()
    data object ExpiryWarningDaysCommitted : AppSettingsEvent()
}
