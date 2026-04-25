package com.example.general_first_aid_kit.domain.model

data class AppSettings(
    val lowStockThreshold: Int = 2,
    val expiryWarningDays: Int = 7
)
