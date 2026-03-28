package com.example.general_first_aid_kit.domain.util

sealed class ValidationResult {
    data object Success : ValidationResult()
    data class Error(val messageResId: Int) : ValidationResult()
}
