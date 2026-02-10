package com.example.general_first_aid_kit.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.general_first_aid_kit.domain.model.Kit
import com.example.general_first_aid_kit.domain.usecase.GetKitsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getKitsUseCase: GetKitsUseCase
) : ViewModel() {

    private val _isArchiveMode = MutableStateFlow(false)
    val isArchiveMode = _isArchiveMode.asStateFlow()

    val uiState: StateFlow<MainUiState> = combine(
        getKitsUseCase(),
        _isArchiveMode
    ) { kits, isArchive ->
        val filteredKits = kits.filter { it.isArchived == isArchive }
        MainUiState(kits = filteredKits, isLoading = false, error = null)
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
}

data class MainUiState(
    val kits: List<Kit> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)