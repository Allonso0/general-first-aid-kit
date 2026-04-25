package com.example.general_first_aid_kit.domain.model

data class Kit(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val colorIndex: Int = 0,
    val ownerId: String = "",
    val userIds: List<String> = emptyList(),
    val type: KitType = KitType.PERSONAL,
    val archivedUserIds: List<String> = emptyList(),
    val inviteCode: String? = null,
    val countMedicine: Int = 0,
    val countExpired: Int = 0,
    val countRunningOut: Int = 0
)
