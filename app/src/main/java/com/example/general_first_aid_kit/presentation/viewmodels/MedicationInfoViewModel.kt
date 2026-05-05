package com.example.general_first_aid_kit.presentation.viewmodels

import androidx.lifecycle.viewModelScope
import com.example.general_first_aid_kit.data.connectivity.ConnectivityMonitor
import com.example.general_first_aid_kit.domain.model.Medication
import com.example.general_first_aid_kit.domain.usecase.GetMedicationUseCase
import com.example.general_first_aid_kit.domain.usecase.GetUserUseCase
import com.example.general_first_aid_kit.domain.usecase.ObserveKitUseCase
import com.example.general_first_aid_kit.domain.usecase.SaveMedicationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@HiltViewModel
class MedicationInfoViewModel @Inject constructor(
    private val getMedicationUseCase: GetMedicationUseCase,
    private val saveMedicationUseCase: SaveMedicationUseCase,
    observeKitUseCase: ObserveKitUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val connectivityMonitor: ConnectivityMonitor
) : KitAwareViewModel(observeKitUseCase, getUserUseCase) {

    val isOnline: StateFlow<Boolean> = connectivityMonitor.isOnline

    private val _medication = MutableStateFlow<Medication?>(null)
    val medication = _medication.asStateFlow()

    private val quantityMutex = Mutex()
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
        if (_isKitShared.value && !connectivityMonitor.isOnline.value) return

        viewModelScope.launch {
            quantityMutex.withLock {
                val currentMed = _medication.value ?: return@withLock
                val newQuantity = (currentMed.quantity + delta).coerceAtLeast(0)
                if (newQuantity == currentMed.quantity) return@withLock

                val updatedMed = currentMed.copy(quantity = newQuantity)
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
    }
}
