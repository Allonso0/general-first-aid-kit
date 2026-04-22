package com.example.general_first_aid_kit.domain.repository

import com.example.general_first_aid_kit.domain.model.KitNotificationSettings

interface KitNotificationSettingsRepository {
    suspend fun getSettings(userId: String, kitId: String): KitNotificationSettings
    suspend fun saveSettings(userId: String, settings: KitNotificationSettings)
}
