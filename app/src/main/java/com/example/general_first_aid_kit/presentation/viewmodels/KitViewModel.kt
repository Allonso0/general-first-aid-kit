package com.example.general_first_aid_kit.presentation.viewmodels

import androidx.lifecycle.viewModelScope
import com.example.general_first_aid_kit.data.connectivity.ConnectivityMonitor
import com.example.general_first_aid_kit.domain.model.Kit
import com.example.general_first_aid_kit.domain.model.Medication
import com.example.general_first_aid_kit.domain.usecase.GetMedicationsUseCase
import com.example.general_first_aid_kit.domain.usecase.GetUserUseCase
import com.example.general_first_aid_kit.domain.usecase.ObserveKitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class KitViewModel @Inject constructor(
    private val getMedicationsUseCase: GetMedicationsUseCase,
    observeKitUseCase: ObserveKitUseCase,
    getUserUseCase: GetUserUseCase,
    private val connectivityMonitor: ConnectivityMonitor
) : KitAwareViewModel(observeKitUseCase, getUserUseCase) {

    val isOnline: StateFlow<Boolean> = connectivityMonitor.isOnline

    private val _kitId = MutableStateFlow<String?>(null)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>("Все")
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _currentKit = MutableStateFlow<Kit?>(null)
    val currentKit: StateFlow<Kit?> = _currentKit.asStateFlow()

    val isSharedAndOffline: StateFlow<Boolean> = combine(
        connectivityMonitor.isOnline,
        isKitShared
    ) { online, shared -> !online && shared }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    @OptIn(ExperimentalCoroutinesApi::class)
    val medications: StateFlow<List<Medication>> = combine(
        _kitId.filterNotNull().flatMapLatest { id -> getMedicationsUseCase(id) },
        _searchQuery,
        _selectedCategory
    ) { list, query, category ->
        list.filter { med ->
            val matchesQuery = query.isBlank() || med.name.contains(query, ignoreCase = true)
            val matchesCategory = category == null || category == "Все" || med.category == category
            matchesQuery && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun initWithKitId(kitId: String) {
        if (_kitId.value != kitId) {
            _kitId.value = kitId
        }
        startObservingKit(kitId)
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
    }

    override fun onKitUpdate(kit: Kit?) {
        _currentKit.value = kit
    }
}
