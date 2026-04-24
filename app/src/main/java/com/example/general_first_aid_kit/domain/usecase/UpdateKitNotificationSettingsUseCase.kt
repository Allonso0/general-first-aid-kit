package com.example.general_first_aid_kit.domain.usecase

import com.example.general_first_aid_kit.domain.model.KitNotificationSettings
import com.example.general_first_aid_kit.domain.repository.KitNotificationSettingsRepository
import javax.inject.Inject

class UpdateKitNotificationSettingsUseCase @Inject constructor(
    private val repository: KitNotificationSettingsRepository
) {
    suspend operator fun invoke(userId: String, settings: KitNotificationSettings) {
        repository.saveSettings(userId, settings)
    }
}
