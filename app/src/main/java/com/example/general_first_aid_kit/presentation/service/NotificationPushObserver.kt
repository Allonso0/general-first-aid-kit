package com.example.general_first_aid_kit.presentation.service

import android.content.Context
import com.example.general_first_aid_kit.domain.model.NotificationType
import com.example.general_first_aid_kit.domain.usecase.GetKitNotificationSettingsUseCase
import com.example.general_first_aid_kit.domain.usecase.GetNotificationsUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private val memberActivityTypes = setOf(
    NotificationType.MEMBER_JOINED,
    NotificationType.MEMBER_LEFT,
    NotificationType.MEMBER_ADDED_MEDICATION,
    NotificationType.MEMBER_REMOVED_MEDICATION,
    NotificationType.MEMBER_EDITED_MEDICATION
)

private val pushEligibleTypes = memberActivityTypes + NotificationType.LOW_STOCK

@Singleton
class NotificationPushObserver @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth,
    private val getNotifications: GetNotificationsUseCase,
    private val getSettings: GetKitNotificationSettingsUseCase
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var observationJob: Job? = null

    fun start() {
        auth.addAuthStateListener { firebaseAuth ->
            observationJob?.cancel()
            val userId = firebaseAuth.currentUser?.uid ?: return@addAuthStateListener
            observationJob = scope.launch { observeForUser(userId) }
        }
    }

    private suspend fun observeForUser(userId: String) {
        val seenIds = mutableSetOf<String>()
        var isFirstEmit = true
        getNotifications(userId).collect { notifications ->
            if (isFirstEmit) {
                seenIds.addAll(notifications.map { it.id })
                isFirstEmit = false
                return@collect
            }
            notifications
                .filter { it.id !in seenIds && !it.isRead && it.type in pushEligibleTypes }
                .forEach { notification ->
                    seenIds.add(notification.id)
                    val settings = runCatching {
                        getSettings(userId, notification.kitId)
                    }.getOrNull() ?: return@forEach

                    val shouldPush = when (notification.type) {
                        in memberActivityTypes -> settings.notifyMemberActivity
                        NotificationType.LOW_STOCK -> settings.notifyLowStock
                        else -> false
                    }
                    if (shouldPush) {
                        NotificationHelper.showNotification(
                            context, notification.type, "Аптечка", notification.message
                        )
                    }
                }
        }
    }
}
