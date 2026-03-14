package com.example.general_first_aid_kit.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.general_first_aid_kit.domain.model.Medication
import com.example.general_first_aid_kit.domain.usecase.SaveMedicationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddMedicationViewModel @Inject constructor(
    private val saveMedicationUseCase: SaveMedicationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddMedicationUiState())
    val uiState = _uiState.asStateFlow()

    fun updateName(name: String) = _uiState.update { it.copy(name = name, error = null) }
    fun updateExpirationDate(dateMillis: Long?) = _uiState.update { it.copy(expirationDateMillis = dateMillis) }
    fun updateQuantity(quantity: String) = _uiState.update { it.copy(quantity = quantity, error = null) }
    fun updateUnit(unit: String) = _uiState.update { it.copy(unit = unit) }
    fun updateCategory(category: String) = _uiState.update { it.copy(category = category) }
    fun updateDescription(description: String) = _uiState.update { it.copy(description = description) }
    fun updatePhotoUri(uri: String?) = _uiState.update { it.copy(photoUri = uri) }

    fun saveMedication(kitId: String, onSuccess: () -> Unit) {
        val state = _uiState.value

        val quantityInt = state.quantity.toIntOrNull()
        if (state.quantity.isNotBlank() && quantityInt == null) {
            _uiState.update { it.copy(error = "Количество должно быть числом") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val medication = Medication(
                name = state.name.trim(),
                expirationDate = state.expirationDateMillis ?: 0L,
                quantity = quantityInt ?: 0,
                unit = state.unit,
                category = state.category.trim(),
                description = state.description.trim()
            )

            val result = saveMedicationUseCase(
                kitId = kitId,
                medication = medication,
                localPhotoUri = state.photoUri
            )

            _uiState.update { it.copy(isLoading = false) }

            result.onSuccess {
                onSuccess()
            }.onFailure { exception ->
                _uiState.update { it.copy(error = exception.message ?: "Не удалось сохранить лекарство") }
            }
        }
    }
}