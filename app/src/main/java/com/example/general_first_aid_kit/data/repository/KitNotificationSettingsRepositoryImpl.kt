package com.example.general_first_aid_kit.data.repository

import com.example.general_first_aid_kit.domain.model.KitNotificationSettings
import com.example.general_first_aid_kit.domain.repository.KitNotificationSettingsRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class KitNotificationSettingsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : KitNotificationSettingsRepository {

    override suspend fun getSettings(userId: String, kitId: String): KitNotificationSettings {
        return withContext(Dispatchers.IO) {
            try {
                val doc = firestore
                    .collection("users").document(userId)
                    .collection("kitNotificationSettings").document(kitId)
                    .get().await()

                if (doc.exists()) {
                    KitNotificationSettings(
                        kitId = kitId,
                        userId = userId,
                        notifyExpiry = doc.getBoolean("notifyExpiry") ?: true,
                        notifyLowStock = doc.getBoolean("notifyLowStock") ?: true,
                        notifyMemberActivity = doc.getBoolean("notifyMemberActivity") ?: true
                    )
                } else {
                    KitNotificationSettings(kitId = kitId, userId = userId)
                }
            } catch (_: Exception) {
                KitNotificationSettings(kitId = kitId, userId = userId)
            }
        }
    }

    override suspend fun saveSettings(userId: String, settings: KitNotificationSettings) {
        withContext(Dispatchers.IO) {
            try {
                firestore
                    .collection("users").document(userId)
                    .collection("kitNotificationSettings").document(settings.kitId)
                    .set(
                        mapOf(
                            "notifyExpiry" to settings.notifyExpiry,
                            "notifyLowStock" to settings.notifyLowStock,
                            "notifyMemberActivity" to settings.notifyMemberActivity
                        )
                    ).await()
            } catch (_: Exception) {
            }
        }
    }
}
