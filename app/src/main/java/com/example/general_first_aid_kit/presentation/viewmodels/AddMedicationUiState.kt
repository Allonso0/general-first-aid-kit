package com.example.general_first_aid_kit.presentation.viewmodels

data class AddMedicationUiState(
    val name: String = "",
    val expirationDateMillis: Long? = null,
    val quantity: String = "",
    val unit: String = "шт",
    val category: String = "",
    val description: String = "",
    val photoUri: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
