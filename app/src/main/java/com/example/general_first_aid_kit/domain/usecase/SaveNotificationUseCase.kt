package com.example.general_first_aid_kit.domain.usecase

import com.example.general_first_aid_kit.domain.model.AppNotification
import com.example.general_first_aid_kit.domain.repository.NotificationRepository
import javax.inject.Inject

class SaveNotificationUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(userId: String, notification: AppNotification) {
        repository.saveNotification(userId, notification)
    }
}
