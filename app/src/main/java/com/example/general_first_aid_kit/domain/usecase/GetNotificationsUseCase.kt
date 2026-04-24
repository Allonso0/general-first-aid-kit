package com.example.general_first_aid_kit.domain.usecase

import com.example.general_first_aid_kit.domain.model.AppNotification
import com.example.general_first_aid_kit.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNotificationsUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    operator fun invoke(userId: String): Flow<List<AppNotification>> =
        repository.observeNotifications(userId)
}
