package com.example.general_first_aid_kit.domain.repository

import com.example.general_first_aid_kit.domain.model.AppSettings

interface AppSettingsRepository {
    fun getSettings(): AppSettings
    fun saveSettings(settings: AppSettings)
}
