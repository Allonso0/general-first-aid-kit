package com.example.general_first_aid_kit.domain.repository

import com.example.general_first_aid_kit.domain.model.AppNotification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun observeNotifications(userId: String): Flow<List<AppNotification>>
    suspend fun saveNotification(userId: String, notification: AppNotification)
    suspend fun markAllAsRead(userId: String)
}
