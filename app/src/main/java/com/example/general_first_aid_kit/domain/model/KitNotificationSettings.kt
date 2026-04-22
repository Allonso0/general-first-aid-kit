package com.example.general_first_aid_kit.domain.model

data class KitNotificationSettings(
    val kitId: String = "",
    val userId: String = "",
    val notifyExpiry: Boolean = true,
    val notifyLowStock: Boolean = true,
    val notifyMemberActivity: Boolean = true
)
