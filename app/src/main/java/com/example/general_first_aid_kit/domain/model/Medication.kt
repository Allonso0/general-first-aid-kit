package com.example.general_first_aid_kit.domain.model

data class Medication(
    val id: String = "",
    val name: String = "",
    val expirationDate: Long = 0L,
    val quantity: Int = 0,
    val unit: String = "шт",
    val category: String = "",
    val description: String = "",
    val photoUrl: String? = null
)
