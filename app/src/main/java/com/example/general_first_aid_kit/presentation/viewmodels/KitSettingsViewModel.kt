package com.example.general_first_aid_kit.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.general_first_aid_kit.domain.model.KitNotificationSettings
import com.example.general_first_aid_kit.domain.model.KitType
import com.example.general_first_aid_kit.domain.model.User
import com.example.general_first_aid_kit.domain.usecase.DeleteKitUseCase
import com.example.general_first_aid_kit.domain.usecase.GetKitNotificationSettingsUseCase
import com.example.general_first_aid_kit.domain.usecase.GetKitUseCase
import com.example.general_first_aid_kit.domain.usecase.GetUserUseCase
import com.example.general_first_aid_kit.domain.usecase.GetUsersByIdsUseCase
import com.example.general_first_aid_kit.domain.usecase.ObserveKitUseCase
import com.example.general_first_aid_kit.domain.usecase.RefreshInviteCodeUseCase
import com.example.general_first_aid_kit.domain.usecase.RemoveUserFromKitUseCase
import com.example.general_first_aid_kit.domain.usecase.SetKitArchivedUseCase
import com.example.general_first_aid_kit.domain.usecase.UpdateKitNotificationSettingsUseCase
import com.example.general_first_aid_kit.domain.usecase.UpdateKitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KitSettingsViewModel @Inject constructor(
    private val getKitUseCase: GetKitUseCase,
    private val updateKitUseCase: UpdateKitUseCase,
    private val deleteKitUseCase: DeleteKitUseCase,
    private val refreshInviteCodeUseCase: RefreshInviteCodeUseCase,
    private val getUsersByIdsUseCase: GetUsersByIdsUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val removeUserFromKitUseCase: RemoveUserFromKitUseCase,
    private val observeKitUseCase: ObserveKitUseCase,
    private val getKitNotificationSettings: GetKitNotificationSettingsUseCase,
    private val updateKitNotificationSettings: UpdateKitNotificationSettingsUseCase,
    private val setKitArchivedUseCase: SetKitArchivedUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(KitSettingsUiState())
    val uiState = _uiState.asStateFlow()

    private var currentKitId: String = ""

    fun initScreen(kitId: String, initialName: String, initialLocation: String, initialColorIndex: Int) {
        currentKitId = kitId
        _uiState.update {
            it.copy(
                name = initialName,
                location = initialLocation,
                selectedColorIndex = initialColorIndex
            )
        }
        loadKitData()
    }

    private fun loadKitData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val currentUser = getUserUseCase()
            val currentUserId = currentUser?.id ?: ""

            if (currentUserId.isNotEmpty() && currentKitId.isNotEmpty()) {
                val settings = getKitNotificationSettings(currentUserId, currentKitId)
                _uiState.update {
                    it.copy(
                        notifyExpiry = settings.notifyExpiry,
                        notifyLowStock = settings.notifyLowStock,
                        notifyMemberActivity = settings.notifyMemberActivity
                    )
                }
            }

            observeKitUseCase(currentKitId).collect { kit ->
                if (kit == null) {
                    if (!uiState.value.isLoading) {
                        _uiState.update { it.copy(isKitDeleted = true) }
                    }
                    return@collect
                }

                if (!kit.userIds.contains(currentUserId)) {
                    _uiState.update { it.copy(isKitDeleted = true) }
                    return@collect
                }

                val users = getUsersByIdsUseCase(kit.userIds)

                _uiState.update {
                    it.copy(
                        name = kit.name,
                        location = kit.location,
                        selectedColorIndex = kit.colorIndex,
                        isPublic = kit.type == KitType.SHARED,
                        isArchived = currentUserId in kit.archivedUserIds,
                        inviteCode = kit.inviteCode,
                        ownerId = kit.ownerId,
                        isOwner = kit.ownerId == currentUser?.id,
                        participants = users,
                        currentUserId = currentUser?.id ?: "",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onEvent(event: KitSettingsEvent) {
        when (event) {
            is KitSettingsEvent.NameChanged -> _uiState.update { it.copy(name = event.name) }
            is KitSettingsEvent.LocationChanged -> _uiState.update { it.copy(location = event.location) }
            is KitSettingsEvent.ColorSelected -> _uiState.update { it.copy(selectedColorIndex = event.index) }
            is KitSettingsEvent.TabChanged -> _uiState.update { it.copy(selectedTab = event.index) }
            is KitSettingsEvent.TogglePublic -> {
                _uiState.update {
                    it.copy(
                        isPublic = event.isPublic,
                        notifyMemberActivity = event.isPublic
                    )
                }
                saveNotificationSettings()
            }
            is KitSettingsEvent.NotificationSettingChanged -> {
                _uiState.update {
                    when (event.setting) {
                        NotificationSetting.EXPIRY -> it.copy(notifyExpiry = event.enabled)
                        NotificationSetting.LOW_STOCK -> it.copy(notifyLowStock = event.enabled)
                        NotificationSetting.MEMBER_ACTIVITY -> it.copy(notifyMemberActivity = event.enabled)
                    }
                }
                saveNotificationSettings()
            }
        }
    }

    private fun saveNotificationSettings() {
        viewModelScope.launch {
            val userId = getUserUseCase()?.id ?: return@launch
            val state = _uiState.value
            updateKitNotificationSettings(
                userId,
                KitNotificationSettings(
                    kitId = currentKitId,
                    userId = userId,
                    notifyExpiry = state.notifyExpiry,
                    notifyLowStock = state.notifyLowStock,
                    notifyMemberActivity = state.notifyMemberActivity
                )
            )
        }
    }

    fun saveChanges(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (!state.isOwner) return

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val newUserIds = if (!state.isPublic) {
                listOf(state.ownerId)
            } else {
                state.participants.map { it.id }
            }

            val result = updateKitUseCase(
                kitId = currentKitId,
                name = state.name,
                location = state.location,
                colorIndex = state.selectedColorIndex,
                type = if (state.isPublic) KitType.SHARED else KitType.PERSONAL,
                userIds = newUserIds
            )

            _uiState.update { it.copy(isLoading = false) }
            if (result.isSuccess) onSuccess()
            else _uiState.update { it.copy(error = "Ошибка при сохранении") }
        }
    }

    fun deleteKit(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (!state.isOwner) return

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result = deleteKitUseCase(currentKitId)
            if (result.isSuccess) {
                onSuccess()
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Ошибка при удалении") }
            }
        }
    }

    fun leaveKit(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.isOwner) return

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val actorName = getUserUseCase()?.name ?: ""
            val result = removeUserFromKitUseCase(currentKitId, state.currentUserId, actorName)
            if (result.isSuccess) {
                onSuccess()
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Ошибка при выходе из аптечки") }
            }
        }
    }

    fun generateInviteCode() {
        viewModelScope.launch {
            refreshInviteCodeUseCase(currentKitId).onSuccess { newCode ->
                _uiState.update { it.copy(inviteCode = newCode) }
            }
        }
    }

    fun removeParticipant(userId: String) {
        viewModelScope.launch {
            val actorName = _uiState.value.participants.find { it.id == userId }?.name ?: ""
            removeUserFromKitUseCase(currentKitId, userId, actorName)
        }
    }

    fun setArchived(archived: Boolean, onSuccess: () -> Unit) {
        val userId = _uiState.value.currentUserId
        if (userId.isEmpty()) return
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result = setKitArchivedUseCase(currentKitId, userId, archived)
            if (result.isSuccess) onSuccess()
            else _uiState.update { it.copy(isLoading = false, error = "Ошибка при изменении статуса аптечки") }
        }
    }
}

enum class NotificationSetting { EXPIRY, LOW_STOCK, MEMBER_ACTIVITY }

data class KitSettingsUiState(
    val name: String = "",
    val location: String = "",
    val selectedColorIndex: Int = 0,
    val isPublic: Boolean = false,
    val isArchived: Boolean = false,
    val isKitDeleted: Boolean = false,
    val notifyExpiry: Boolean = true,
    val notifyLowStock: Boolean = true,
    val notifyMemberActivity: Boolean = true,
    val selectedTab: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val inviteCode: String? = null,
    val isOwner: Boolean = false,
    val ownerId: String = "",
    val currentUserId: String = "",
    val participants: List<User> = emptyList()
)

sealed class KitSettingsEvent {
    data class NameChanged(val name: String) : KitSettingsEvent()
    data class LocationChanged(val location: String) : KitSettingsEvent()
    data class ColorSelected(val index: Int) : KitSettingsEvent()
    data class TabChanged(val index: Int) : KitSettingsEvent()
    data class TogglePublic(val isPublic: Boolean) : KitSettingsEvent()
    data class NotificationSettingChanged(
        val setting: NotificationSetting,
        val enabled: Boolean
    ) : KitSettingsEvent()
}
