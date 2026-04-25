package com.example.general_first_aid_kit.domain.usecase

import com.example.general_first_aid_kit.domain.model.AppSettings
import com.example.general_first_aid_kit.domain.repository.AppSettingsRepository
import javax.inject.Inject

class GetAppSettingsUseCase @Inject constructor(
    private val repository: AppSettingsRepository
) {
    operator fun invoke(): AppSettings = repository.getSettings()
}
