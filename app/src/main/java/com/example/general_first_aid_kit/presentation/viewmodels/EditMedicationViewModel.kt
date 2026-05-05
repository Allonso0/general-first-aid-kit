package com.example.general_first_aid_kit.presentation.viewmodels

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.general_first_aid_kit.R
import com.example.general_first_aid_kit.data.connectivity.ConnectivityMonitor
import com.example.general_first_aid_kit.domain.model.Medication
import com.example.general_first_aid_kit.domain.usecase.DeleteMedicationUseCase
import com.example.general_first_aid_kit.domain.usecase.GetMedicationUseCase
import com.example.general_first_aid_kit.domain.usecase.GetUserUseCase
import com.example.general_first_aid_kit.domain.usecase.ObserveKitUseCase
import com.example.general_first_aid_kit.domain.usecase.SaveMedicationUseCase
import com.example.general_first_aid_kit.domain.util.MedicationValidator
import com.example.general_first_aid_kit.domain.util.ValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditMedicationViewModel @Inject constructor(
    private val getMedicationUseCase: GetMedicationUseCase,
    private val saveMedicationUseCase: SaveMedicationUseCase,
    private val deleteMedicationUseCase: DeleteMedicationUseCase,
    observeKitUseCase: ObserveKitUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val connectivityMonitor: ConnectivityMonitor,
    @ApplicationContext private val context: Context
) : KitAwareViewModel(observeKitUseCase, getUserUseCase) {

    val isOnline: StateFlow<Boolean> = connectivityMonitor.isOnline

    private val _uiState = MutableStateFlow(AddMedicationUiState())
    val uiState = _uiState.asStateFlow()

    private var originalMedication: Medication? = null
    private var kitId: String = ""

    fun loadMedication(kitId: String, medicationId: String) {
        this.kitId = kitId
        viewModelScope.launch {
            getMedicationUseCase(kitId, medicationId).collect { med ->
                med?.let {
                    originalMedication = it
                    _uiState.update { state ->
                        state.copy(
                            name = it.name,
                            expirationDateMillis = it.expirationDate,
                            quantity = it.quantity.toString(),
                            unit = it.unit,
                            category = it.category,
                            description = it.description,
                            photoUri = it.photoUrl
                        )
                    }
                }
            }
        }
    }

    fun updateName(name: String) = _uiState.update { it.copy(name = name, nameErrorResId = null) }
    fun updateExpirationDate(dateMillis: Long?) = _uiState.update { it.copy(expirationDateMillis = dateMillis, expirationDateErrorResId = null) }
    fun updateQuantity(quantity: String) = _uiState.update { it.copy(quantity = quantity, quantityErrorResId = null) }
    fun updateUnit(unit: String) = _uiState.update { it.copy(unit = unit) }
    fun updateCategory(category: String) = _uiState.update { it.copy(category = category) }
    fun updateDescription(description: String) = _uiState.update { it.copy(description = description) }
    fun updatePhotoUri(uri: String?) = _uiState.update { it.copy(photoUri = uri) }

    fun saveMedication(onSuccess: () -> Unit) {
        if (_isKitShared.value && !connectivityMonitor.isOnline.value) {
            _uiState.update { it.copy(error = context.getString(R.string.error_offline_edit_shared)) }
            return
        }
        if (!validateInputs()) return

        val state = _uiState.value
        originalMedication?.id ?: return

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val user = getUserUseCase()
            val updatedMedication = originalMedication!!.copy(
                name = state.name.trim(),
                expirationDate = state.expirationDateMillis ?: 0L,
                quantity = state.quantity.toIntOrNull() ?: 0,
                unit = state.unit,
                category = state.category.trim(),
                description = state.description.trim()
            )

            val localUri = if (state.photoUri?.startsWith("http") == true) null else state.photoUri

            val result = saveMedicationUseCase(
                kitId = kitId,
                medication = updatedMedication,
                localPhotoUri = localUri,
                actorUserId = user?.id ?: "",
                actorName = user?.name ?: "",
                isNew = false
            )

            _uiState.update { it.copy(isLoading = false) }

            result.onSuccess { onSuccess() }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun deleteMedication(onSuccess: () -> Unit) {
        if (_isKitShared.value && !connectivityMonitor.isOnline.value) {
            _uiState.update { it.copy(error = context.getString(R.string.error_offline_delete_shared)) }
            return
        }
        val medication = originalMedication ?: return
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val user = getUserUseCase()
            val result = deleteMedicationUseCase(
                kitId = kitId,
                medication = medication,
                actorUserId = user?.id ?: "",
                actorName = user?.name ?: ""
            )
            _uiState.update { it.copy(isLoading = false) }
            result.onSuccess { onSuccess() }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
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
}
