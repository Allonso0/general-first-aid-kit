package com.example.general_first_aid_kit.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.general_first_aid_kit.domain.model.AppNotification
import com.example.general_first_aid_kit.domain.model.NotificationType
import com.example.general_first_aid_kit.domain.usecase.GetAppSettingsUseCase
import com.example.general_first_aid_kit.domain.usecase.GetKitNotificationSettingsUseCase
import com.example.general_first_aid_kit.domain.usecase.GetKitUseCase
import com.example.general_first_aid_kit.domain.usecase.SaveNotificationUseCase
import com.example.general_first_aid_kit.presentation.service.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class LowStockCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val auth: FirebaseAuth,
    private val saveNotification: SaveNotificationUseCase,
    private val getSettings: GetKitNotificationSettingsUseCase,
    private val getKit: GetKitUseCase,
    private val getAppSettings: GetAppSettingsUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val userId = auth.currentUser?.uid ?: return Result.success()

        val kitId = inputData.getString(KEY_KIT_ID) ?: return Result.failure()
        val medicationId = inputData.getString(KEY_MEDICATION_ID) ?: return Result.failure()
        val medicationName = inputData.getString(KEY_MEDICATION_NAME) ?: return Result.failure()
        val quantity = inputData.getInt(KEY_QUANTITY, -1)

        val threshold = getAppSettings().lowStockThreshold
        if (quantity < 0 || quantity > threshold) return Result.success()

        val kitName = getKit(kitId).getOrNull()?.name ?: kitId
        val now = System.currentTimeMillis()
        val message = "В аптечке «$kitName» заканчивается «$medicationName»: осталось $quantity шт."

        val notificationId = "low_stock_${medicationId}_$quantity"
        saveNotification(
            userId,
            AppNotification(
                id = notificationId,
                kitId = kitId,
                kitName = kitName,
                type = NotificationType.LOW_STOCK,
                message = message,
                timestamp = now,
                isRead = false
            )
        )

        val settings = getSettings(userId, kitId)
        if (settings.notifyLowStock) {
            NotificationHelper.showNotification(
                applicationContext,
                NotificationType.LOW_STOCK,
                "Заканчивается лекарство",
                message
            )
        }

        return Result.success()
    }

    companion object {
        const val KEY_KIT_ID = "kit_id"
        const val KEY_MEDICATION_ID = "medication_id"
        const val KEY_MEDICATION_NAME = "medication_name"
        const val KEY_QUANTITY = "quantity"
    }
}
