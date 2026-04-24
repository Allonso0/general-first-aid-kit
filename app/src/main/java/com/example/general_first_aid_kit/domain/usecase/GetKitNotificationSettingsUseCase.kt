package com.example.general_first_aid_kit.domain.usecase

import com.example.general_first_aid_kit.domain.model.KitNotificationSettings
import com.example.general_first_aid_kit.domain.repository.KitNotificationSettingsRepository
import javax.inject.Inject

class GetKitNotificationSettingsUseCase @Inject constructor(
    private val repository: KitNotificationSettingsRepository
) {
    suspend operator fun invoke(userId: String, kitId: String): KitNotificationSettings =
        repository.getSettings(userId, kitId)
}
