package com.example.general_first_aid_kit.domain.usecase

import com.example.general_first_aid_kit.domain.model.Kit
import com.example.general_first_aid_kit.domain.model.NotificationType
import com.example.general_first_aid_kit.domain.repository.KitRepository
import javax.inject.Inject

class JoinKitByCodeUseCase @Inject constructor(
    private val repository: KitRepository,
    private val fanOutNotification: FanOutNotificationUseCase
) {
    suspend operator fun invoke(userId: String, inviteCode: String, actorName: String): Result<Kit> {
        val result = repository.joinKitByCode(userId, inviteCode)
        result.onSuccess { kit ->
            fanOutNotification(
                kitId = kit.id,
                actorUserId = userId,
                type = NotificationType.MEMBER_JOINED,
                message = "$actorName вступил(а) в аптечку «${kit.name}»"
            )
        }
        return result
    }
}
