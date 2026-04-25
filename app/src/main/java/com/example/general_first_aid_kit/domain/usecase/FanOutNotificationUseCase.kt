package com.example.general_first_aid_kit.domain.usecase

import com.example.general_first_aid_kit.domain.model.AppNotification
import com.example.general_first_aid_kit.domain.model.KitType
import com.example.general_first_aid_kit.domain.model.NotificationType
import com.example.general_first_aid_kit.domain.repository.KitRepository
import com.example.general_first_aid_kit.domain.repository.NotificationRepository
import java.util.UUID
import javax.inject.Inject

private val memberActivityTypes = setOf(
    NotificationType.MEMBER_JOINED,
    NotificationType.MEMBER_LEFT,
    NotificationType.MEMBER_ADDED_MEDICATION,
    NotificationType.MEMBER_REMOVED_MEDICATION,
    NotificationType.MEMBER_EDITED_MEDICATION
)

class FanOutNotificationUseCase @Inject constructor(
    private val kitRepository: KitRepository,
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(
        kitId: String,
        actorUserId: String,
        type: NotificationType,
        message: String,
        includeActor: Boolean = false
    ) {
        val kit = kitRepository.getKitById(kitId).getOrNull() ?: return

        if (kit.isArchived) return
        if (kit.type == KitType.PERSONAL && type in memberActivityTypes) return

        val recipients = if (includeActor) kit.userIds
                         else kit.userIds.filter { it != actorUserId }
        val timestamp = System.currentTimeMillis()

        recipients.forEach { userId ->
            val notification = AppNotification(
                id = UUID.randomUUID().toString(),
                kitId = kitId,
                kitName = kit.name,
                type = type,
                message = message,
                timestamp = timestamp,
                isRead = false
            )
            notificationRepository.saveNotification(userId, notification)
        }
    }
}
