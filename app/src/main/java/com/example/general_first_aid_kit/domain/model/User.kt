package com.example.general_first_aid_kit.domain.model

data class User(
    val id: String,
    val email: String,
    val name: String,
    val avatarURL: String?,

    val personalKitIds: List<String> = emptyList(),
    val publicKitIds: List<String> = emptyList()
)
