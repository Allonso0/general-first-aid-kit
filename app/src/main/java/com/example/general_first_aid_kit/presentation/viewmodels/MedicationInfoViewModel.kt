package com.example.general_first_aid_kit.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.general_first_aid_kit.domain.model.Medication
import com.example.general_first_aid_kit.domain.usecase.GetMedicationUseCase
import com.example.general_first_aid_kit.domain.usecase.GetUserUseCase
import com.example.general_first_aid_kit.domain.usecase.ObserveKitUseCase
import com.example.general_first_aid_kit.domain.usecase.SaveMedicationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MedicationInfoViewModel @Inject constructor(
    private val getMedicationUseCase: GetMedicationUseCase,
    private val saveMedicationUseCase: SaveMedicationUseCase,
    private val observeKitUseCase: ObserveKitUseCase,
    private val getUserUseCase: GetUserUseCase
) : ViewModel() {

    private val _medication = MutableStateFlow<Medication?>(null)
    val medication = _medication.asStateFlow()

    private val _isUserKickedOrDeleted = MutableStateFlow(false)
    val isUserKickedOrDeleted = _isUserKickedOrDeleted.asStateFlow()

    private var currentKitId: String = ""

    fun loadMedication(kitId: String, medicationId: String) {
        currentKitId = kitId
        viewModelScope.launch {
            getMedicationUseCase(kitId, medicationId).collect {
                _medication.value = it
            }
        }
    }

    fun updateQuantity(delta: Int) {
        val currentMed = _medication.value ?: return
        val newQuantity = (currentMed.quantity + delta).coerceAtLeast(0)

        if (newQuantity == currentMed.quantity) return

        val updatedMed = currentMed.copy(quantity = newQuantity)

        viewModelScope.launch {
            _medication.value = updatedMed
            val user = getUserUseCase()
            val result = saveMedicationUseCase(
                kitId = currentKitId,
                medication = updatedMed,
                localPhotoUri = null,
                actorUserId = user?.id ?: "",
                actorName = user?.name ?: "",
                isNew = false
            )
            if (result.isFailure) {
                _medication.value = currentMed
            }
        }
    }

    fun startObservingKit(kitId: String) {
        viewModelScope.launch {
            val currentUserId = getUserUseCase()?.id ?: ""
            observeKitUseCase(kitId).collect { kit ->
                if (kit == null || !kit.userIds.contains(currentUserId)) {
                    _isUserKickedOrDeleted.value = true
                }
            }
        }
    }
}