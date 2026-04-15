package com.example.general_first_aid_kit.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.general_first_aid_kit.data.repository.GigaChatRepository
import com.example.general_first_aid_kit.domain.model.Medication
import com.example.general_first_aid_kit.domain.usecase.SaveMedicationUseCase
import com.example.general_first_aid_kit.domain.util.MedicationValidator
import com.example.general_first_aid_kit.domain.util.ValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddMedicationViewModel @Inject constructor(
    private val saveMedicationUseCase: SaveMedicationUseCase,
    private val gigaChatRepository: GigaChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddMedicationUiState())
    val uiState = _uiState.asStateFlow()

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name, nameErrorResId = null) }
    }

    fun updateExpirationDate(dateMillis: Long?) {
        _uiState.update { it.copy(expirationDateMillis = dateMillis, expirationDateErrorResId = null) }
    }

    fun updateQuantity(quantity: String) {
        _uiState.update { it.copy(quantity = quantity, quantityErrorResId = null) }
    }

    fun updateUnit(unit: String) = _uiState.update { it.copy(unit = unit) }
    fun updateCategory(category: String) = _uiState.update { it.copy(category = category) }
    fun updateDescription(description: String) = _uiState.update { it.copy(description = description) }
    fun updatePhotoUri(uri: String?) = _uiState.update { it.copy(photoUri = uri) }

    fun saveMedication(kitId: String, onSuccess: () -> Unit) {
        if (!validateInputs()) return

        val state = _uiState.value
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val medication = Medication(
                name = state.name.trim(),
                expirationDate = state.expirationDateMillis ?: 0L,
                quantity = state.quantity.toIntOrNull() ?: 0,
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
                _uiState.update { it.copy(error = exception.message) }
            }
        }
    }

    private fun validateInputs(): Boolean {
        val state = _uiState.value
        
        val nameResult = MedicationValidator.validateName(state.name)
        val dateResult = MedicationValidator.validateExpirationDate(state.expirationDateMillis)
        val quantityResult = MedicationValidator.validateQuantity(state.quantity)

        val hasError = listOf(nameResult, dateResult, quantityResult).any { it is ValidationResult.Error }

        if (hasError) {
            _uiState.update { it.copy(
                nameErrorResId = (nameResult as? ValidationResult.Error)?.messageResId,
                expirationDateErrorResId = (dateResult as? ValidationResult.Error)?.messageResId,
                quantityErrorResId = (quantityResult as? ValidationResult.Error)?.messageResId
            ) }
            return false
        }
        return true
    }

    fun loadDetailsByBarcode(barcode: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            gigaChatRepository.getMedicationByBarcode(barcode)
                .onSuccess { info ->
                    _uiState.update { it.copy(
                        name = info.name,
                        category = info.category,
                        quantity = info.quantity.toString(),
                        unit = info.unit,
                        isLoading = false
                    ) }
                }
                .onFailure { exception ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "Не удалось распознать: ${exception.localizedMessage}"
                    ) }
                }
        }
    }
}
