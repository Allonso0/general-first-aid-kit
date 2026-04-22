package com.example.general_first_aid_kit.domain.model

data class AppNotification(
    val id: String = "",
    val kitId: String = "",
    val kitName: String = "",
    val type: NotificationType = NotificationType.EXPIRED,
    val message: String = "",
    val timestamp: Long = 0L,
    val isRead: Boolean = false
)
