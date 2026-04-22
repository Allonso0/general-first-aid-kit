package com.example.general_first_aid_kit.domain.model

data class MedicationSuggestion(
    val name: String,
    val category: String,
    val quantity: Int,
    val unit: String
)
