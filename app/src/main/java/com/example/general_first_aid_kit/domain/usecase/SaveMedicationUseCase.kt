package com.example.general_first_aid_kit.domain.usecase

import com.example.general_first_aid_kit.domain.model.Medication
import com.example.general_first_aid_kit.domain.model.NotificationType
import com.example.general_first_aid_kit.domain.repository.MedicationRepository
import javax.inject.Inject

class SaveMedicationUseCase @Inject constructor(
    private val repository: MedicationRepository,
    private val getKit: GetKitUseCase,
    private val fanOutNotification: FanOutNotificationUseCase
) {
    suspend operator fun invoke(
        kitId: String,
        medication: Medication,
        localPhotoUri: String?,
        actorUserId: String,
        actorName: String,
        isNew: Boolean
    ): Result<Unit> {
        val result = repository.saveMedication(kitId, medication, localPhotoUri)
        result.onSuccess {
            val kitName = getKit(kitId).getOrNull()?.name ?: kitId
            val type = if (isNew) NotificationType.MEMBER_ADDED_MEDICATION
                       else NotificationType.MEMBER_EDITED_MEDICATION
            val message = if (isNew)
                "$actorName добавил(а) «${medication.name}» в аптечку «$kitName»"
            else
                "$actorName изменил(а) «${medication.name}» в аптечке «$kitName»"
            fanOutNotification(kitId, actorUserId, type, message)
        }
        return result
    }
}
