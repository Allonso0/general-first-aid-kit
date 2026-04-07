package com.example.general_first_aid_kit.presentation.viewmodels

import androidx.activity.result.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.general_first_aid_kit.domain.model.Medication
import com.example.general_first_aid_kit.domain.usecase.GetMedicationsUseCase
import com.example.general_first_aid_kit.domain.usecase.GetUserUseCase
import com.example.general_first_aid_kit.domain.usecase.ObserveKitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KitViewModel @Inject constructor(
    private val getMedicationsUseCase: GetMedicationsUseCase,
    private val observeKitUseCase: ObserveKitUseCase,
    private val getUserUseCase: GetUserUseCase
) : ViewModel() {

    private val _kitId = MutableStateFlow<String?>(null)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>("Все")
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _isUserKickedOrDeleted = MutableStateFlow(false)
    val isUserKickedOrDeleted = _isUserKickedOrDeleted.asStateFlow()

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

    fun loadKit(id: String) {
        if (_kitId.value != id) {
            _kitId.value = id
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
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