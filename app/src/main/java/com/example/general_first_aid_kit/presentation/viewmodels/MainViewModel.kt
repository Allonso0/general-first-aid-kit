package com.example.general_first_aid_kit.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.general_first_aid_kit.domain.model.Kit
import com.example.general_first_aid_kit.data.connectivity.ConnectivityMonitor
import com.example.general_first_aid_kit.domain.usecase.DeleteKitUseCase
import com.example.general_first_aid_kit.domain.usecase.GetAllMedicationsUseCase
import com.example.general_first_aid_kit.domain.usecase.GetAppSettingsUseCase
import com.example.general_first_aid_kit.domain.usecase.GetKitsUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getKitsUseCase: GetKitsUseCase,
    private val deleteKitUseCase: DeleteKitUseCase,
    private val getAllMedicationsUseCase: GetAllMedicationsUseCase,
    private val getAppSettingsUseCase: GetAppSettingsUseCase,
    private val auth: FirebaseAuth,
    private val connectivityMonitor: ConnectivityMonitor
) : ViewModel() {

    val isOnline: StateFlow<Boolean> = connectivityMonitor.isOnline

    private val _isArchiveMode = MutableStateFlow(false)
    val isArchiveMode = _isArchiveMode.asStateFlow()

    val uiState: StateFlow<MainUiState> = combine(
        getKitsUseCase(),
        getAllMedicationsUseCase(),
        _isArchiveMode
    ) { kits, allMedications, isArchive ->
        val currentTime = System.currentTimeMillis()
        val currentUserId = auth.currentUser?.uid ?: ""
        val lowStockThreshold = getAppSettingsUseCase().lowStockThreshold

        val enrichedKits = kits
            .filter { (currentUserId in it.archivedUserIds) == isArchive }
            .map { kit ->
                val kitMedications = allMedications.filter { it.kitId == kit.id }

                kit.copy(
                    countMedicine = kitMedications.size,
                    countExpired = kitMedications.count { it.expirationDate < currentTime },
                    countRunningOut = kitMedications.count { it.quantity <= lowStockThreshold }
                )
            }

        MainUiState(kits = enrichedKits, isLoading = false, error = null)
    }
    .onStart {
        emit(MainUiState(isLoading = true))
    }
    .catch { e ->
        emit(MainUiState(isLoading = false, error = e.message ?: "Неизвестная ошибка"))
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState(isLoading = true)
    )


    fun setArchiveMode(enabled: Boolean) {
        _isArchiveMode.value = enabled
    }

    fun deleteKit(kit: Kit) {
        viewModelScope.launch {
            deleteKitUseCase(kit.id)
        }
    }
}

data class MainUiState(
    val kits: List<Kit> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)