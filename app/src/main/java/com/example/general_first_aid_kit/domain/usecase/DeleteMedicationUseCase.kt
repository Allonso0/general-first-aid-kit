package com.example.general_first_aid_kit.domain.usecase

import com.example.general_first_aid_kit.domain.model.Medication
import com.example.general_first_aid_kit.domain.model.NotificationType
import com.example.general_first_aid_kit.domain.repository.MedicationRepository
import javax.inject.Inject

class DeleteMedicationUseCase @Inject constructor(
    private val repository: MedicationRepository,
    private val getKit: GetKitUseCase,
    private val fanOutNotification: FanOutNotificationUseCase
) {
    suspend operator fun invoke(
        kitId: String,
        medication: Medication,
        actorUserId: String,
        actorName: String
    ): Result<Unit> {
        val result = repository.deleteMedication(kitId, medication)
        result.onSuccess {
            val kitName = getKit(kitId).getOrNull()?.name ?: kitId
            fanOutNotification(
                kitId = kitId,
                actorUserId = actorUserId,
                type = NotificationType.MEMBER_REMOVED_MEDICATION,
                message = "$actorName удалил(а) «${medication.name}» из аптечки «$kitName»"
            )
        }
        return result
    }
}
