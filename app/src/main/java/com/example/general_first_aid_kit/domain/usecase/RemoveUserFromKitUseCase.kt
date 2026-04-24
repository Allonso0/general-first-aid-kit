package com.example.general_first_aid_kit.domain.usecase

import com.example.general_first_aid_kit.domain.model.NotificationType
import com.example.general_first_aid_kit.domain.repository.KitRepository
import javax.inject.Inject

class RemoveUserFromKitUseCase @Inject constructor(
    private val repository: KitRepository,
    private val getKit: GetKitUseCase,
    private val fanOutNotification: FanOutNotificationUseCase
) {
    suspend operator fun invoke(kitId: String, userId: String, actorName: String): Result<Unit> {
        val kitName = getKit(kitId).getOrNull()?.name ?: kitId
        val result = repository.removeUserFromKit(kitId, userId)
        result.onSuccess {
            fanOutNotification(
                kitId = kitId,
                actorUserId = userId,
                type = NotificationType.MEMBER_LEFT,
                message = "$actorName покинул(а) аптечку «$kitName»"
            )
        }
        return result
    }
}
