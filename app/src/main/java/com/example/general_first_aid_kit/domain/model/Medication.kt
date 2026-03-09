package com.example.general_first_aid_kit.domain.model

import kotlinx.datetime.LocalDate

data class Medication(
    val id: String,
    val name: String,
    val category: String,
    val expirationDate: LocalDate,
    val count: Int,
    val measureUnit: String,
    val photoUrl: String? = null
)
