package com.example.general_first_aid_kit.domain.usecase

import com.example.general_first_aid_kit.domain.repository.NotificationRepository
import javax.inject.Inject

class DeleteAllNotificationsUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(userId: String) {
        repository.deleteAllNotifications(userId)
    }
}
