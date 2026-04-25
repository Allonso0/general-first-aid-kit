package com.example.general_first_aid_kit.domain.usecase

import com.example.general_first_aid_kit.domain.model.Medication
import com.example.general_first_aid_kit.domain.model.NotificationType
import com.example.general_first_aid_kit.domain.repository.MedicationRepository
import javax.inject.Inject

class SaveMedicationUseCase @Inject constructor(
    private val repository: MedicationRepository,
    private val getKit: GetKitUseCase,
    private val fanOutNotification: FanOutNotificationUseCase,
    private val getAppSettings: GetAppSettingsUseCase
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

            val activityType = if (isNew) NotificationType.MEMBER_ADDED_MEDICATION
                               else NotificationType.MEMBER_EDITED_MEDICATION
            val activityMessage = if (isNew)
                "$actorName добавил(а) «${medication.name}» в аптечку «$kitName»"
            else
                "$actorName изменил(а) «${medication.name}» в аптечке «$kitName»"
            fanOutNotification(kitId, actorUserId, activityType, activityMessage)

            if (medication.quantity in 0..getAppSettings().lowStockThreshold) {
                val stockMessage = "В аптечке «$kitName» заканчивается «${medication.name}»: осталось ${medication.quantity} шт."
                fanOutNotification(
                    kitId, actorUserId,
                    NotificationType.LOW_STOCK, stockMessage,
                    includeActor = true
                )
            }
        }
        return result
    }
}
