package com.example.general_first_aid_kit.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.general_first_aid_kit.domain.model.Medication
import com.example.general_first_aid_kit.domain.usecase.GetMedicationByBarcodeUseCase
import com.example.general_first_aid_kit.domain.usecase.GetUserUseCase
import com.example.general_first_aid_kit.domain.usecase.SaveMedicationUseCase
import com.example.general_first_aid_kit.domain.util.MedicationValidator
import com.example.general_first_aid_kit.domain.util.ValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddMedicationViewModel @Inject constructor(
    private val saveMedicationUseCase: SaveMedicationUseCase,
    private val getMedicationByBarcodeUseCase: GetMedicationByBarcodeUseCase,
    private val getUserUseCase: GetUserUseCase
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

    fun clearError() = _uiState.update { it.copy(error = null) }

    fun saveMedication(kitId: String, onSuccess: () -> Unit) {
        if (!validateInputs()) return

        val state = _uiState.value
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val user = getUserUseCase()
            val medicationId = UUID.randomUUID().toString()
            val medication = Medication(
                id = medicationId,
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
                localPhotoUri = state.photoUri,
                actorUserId = user?.id ?: "",
                actorName = user?.name ?: "",
                isNew = true
            )

            _uiState.update { it.copy(isLoading = false) }

            result.onSuccess { onSuccess() }
                .onFailure { exception ->
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
            _uiState.update {
                it.copy(
                    nameErrorResId = (nameResult as? ValidationResult.Error)?.messageResId,
                    expirationDateErrorResId = (dateResult as? ValidationResult.Error)?.messageResId,
                    quantityErrorResId = (quantityResult as? ValidationResult.Error)?.messageResId
                )
            }
            return false
        }
        return true
    }

    fun loadDetailsByBarcode(barcode: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            getMedicationByBarcodeUseCase(barcode)
                .onSuccess { suggestion ->
                    _uiState.update {
                        it.copy(
                            name = suggestion.name,
                            category = suggestion.category,
                            quantity = suggestion.quantity.toString(),
                            unit = suggestion.unit,
                            isLoading = false
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Не удалось распознать: ${exception.localizedMessage}"
                        )
                    }
                }
        }
    }
}

data class AddMedicationUiState(
    val name: String = "",
    val nameErrorResId: Int? = null,
    val expirationDateMillis: Long? = null,
    val expirationDateErrorResId: Int? = null,
    val quantity: String = "",
    val quantityErrorResId: Int? = null,
    val unit: String = "шт",
    val category: String = "",
    val description: String = "",
    val photoUri: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
