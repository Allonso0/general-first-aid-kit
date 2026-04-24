package com.example.general_first_aid_kit.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.general_first_aid_kit.domain.model.AppNotification
import com.example.general_first_aid_kit.domain.usecase.DeleteAllNotificationsUseCase
import com.example.general_first_aid_kit.domain.usecase.GetNotificationsUseCase
import com.example.general_first_aid_kit.domain.usecase.GetUserUseCase
import com.example.general_first_aid_kit.domain.usecase.MarkNotificationsReadUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationLogViewModel @Inject constructor(
    private val getNotifications: GetNotificationsUseCase,
    private val markAllRead: MarkNotificationsReadUseCase,
    private val deleteAll: DeleteAllNotificationsUseCase,
    private val getUser: GetUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationLogUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            val userId = getUser()?.id ?: return@launch
            _uiState.update { it.copy(isLoading = true) }
            getNotifications(userId)
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { notifications ->
                    _uiState.update { it.copy(notifications = notifications, isLoading = false) }
                }
        }
    }

    fun onMarkAllRead() {
        viewModelScope.launch {
            val userId = getUser()?.id ?: return@launch
            markAllRead(userId)
        }
    }

    fun onDeleteAll() {
        viewModelScope.launch {
            val userId = getUser()?.id ?: return@launch
            deleteAll(userId)
        }
    }
}

data class NotificationLogUiState(
    val notifications: List<AppNotification> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
