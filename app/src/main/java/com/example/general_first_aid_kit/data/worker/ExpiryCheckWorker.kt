package com.example.general_first_aid_kit.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.general_first_aid_kit.domain.model.AppNotification
import com.example.general_first_aid_kit.domain.model.NotificationType
import com.example.general_first_aid_kit.domain.usecase.GetAllMedicationsUseCase
import com.example.general_first_aid_kit.domain.usecase.GetAppSettingsUseCase
import com.example.general_first_aid_kit.domain.usecase.GetKitNotificationSettingsUseCase
import com.example.general_first_aid_kit.domain.usecase.GetKitUseCase
import com.example.general_first_aid_kit.domain.usecase.SaveNotificationUseCase
import com.example.general_first_aid_kit.presentation.service.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val MS_PER_DAY = 24 * 60 * 60 * 1000L

@HiltWorker
class ExpiryCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val auth: FirebaseAuth,
    private val getAllMedications: GetAllMedicationsUseCase,
    private val getKit: GetKitUseCase,
    private val saveNotification: SaveNotificationUseCase,
    private val getSettings: GetKitNotificationSettingsUseCase,
    private val getAppSettings: GetAppSettingsUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val userId = auth.currentUser?.uid ?: return Result.success()
        val now = System.currentTimeMillis()
        val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dayFormat.format(Date(now))
        val warningDeadline = now + getAppSettings().expiryWarningDays * MS_PER_DAY

        val medications = getAllMedications().first()
        val kitNameCache = mutableMapOf<String, String>()

        for (medication in medications) {
            if (medication.expirationDate == 0L) continue

            val type = when {
                medication.expirationDate <= now -> NotificationType.EXPIRED
                medication.expirationDate <= warningDeadline -> NotificationType.EXPIRY_WARNING
                else -> continue
            }

            val kitName = kitNameCache.getOrPut(medication.kitId) {
                getKit(medication.kitId).getOrNull()?.name ?: medication.kitId
            }

            val message = when (type) {
                NotificationType.EXPIRED ->
                    "Лекарство «${medication.name}» в аптечке «$kitName» просрочено."
                else ->
                    "Срок годности «${medication.name}» в аптечке «$kitName» скоро истекает."
            }

            val notificationId = "expiry_${medication.id}_${type.name}_$today"
            saveNotification(
                userId,
                AppNotification(
                    id = notificationId,
                    kitId = medication.kitId,
                    kitName = kitName,
                    type = type,
                    message = message,
                    timestamp = now,
                    isRead = false
                )
            )

            val settings = getSettings(userId, medication.kitId)
            if (settings.notifyExpiry) {
                val title = if (type == NotificationType.EXPIRED) "Срок годности истёк" else "Срок годности истекает"
                NotificationHelper.showNotification(applicationContext, type, title, message)
            }
        }

        return Result.success()
    }
}
