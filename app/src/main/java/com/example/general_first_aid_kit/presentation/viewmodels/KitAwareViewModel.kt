package com.example.general_first_aid_kit.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.general_first_aid_kit.domain.model.Kit
import com.example.general_first_aid_kit.domain.model.KitType
import com.example.general_first_aid_kit.domain.usecase.GetUserUseCase
import com.example.general_first_aid_kit.domain.usecase.ObserveKitUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class KitAwareViewModel(
    private val observeKitUseCase: ObserveKitUseCase,
    private val getUserUseCase: GetUserUseCase
) : ViewModel() {

    protected val _isKitShared = MutableStateFlow(false)
    val isKitShared: StateFlow<Boolean> = _isKitShared.asStateFlow()

    protected val _isUserKickedOrDeleted = MutableStateFlow(false)
    val isUserKickedOrDeleted: StateFlow<Boolean> = _isUserKickedOrDeleted.asStateFlow()

    private var observeKitJob: Job? = null

    fun startObservingKit(kitId: String) {
        observeKitJob?.cancel()
        observeKitJob = viewModelScope.launch {
            val currentUserId = getUserUseCase()?.id ?: ""
            observeKitUseCase(kitId).collect { kit ->
                _isKitShared.value = kit?.type == KitType.SHARED
                if (kit == null || !kit.userIds.contains(currentUserId)) {
                    _isUserKickedOrDeleted.value = true
                }
                onKitUpdate(kit)
            }
        }
    }

    protected open fun onKitUpdate(kit: Kit?) {}
}
