package com.example.general_first_aid_kit.domain.util

import com.example.general_first_aid_kit.R

object AuthValidator {

    fun validateName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.Error(R.string.error_empty_username)
            name.length > 20 -> ValidationResult.Error(R.string.error_long_username)
            name.length < 2 -> ValidationResult.Error(R.string.error_short_username)
            else -> ValidationResult.Success
        }
    }

    fun validateEmail(email: String): ValidationResult {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return when {
            email.isBlank() -> ValidationResult.Error(R.string.error_empty_email)
            !email.matches(emailPattern.toRegex()) -> ValidationResult.Error(R.string.error_invalid_email)
            else -> ValidationResult.Success
        }
    }

    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult.Error(R.string.error_empty_password)
            password.length < 6 -> ValidationResult.Error(R.string.error_short_password)
            !password.any { it.isDigit() } -> ValidationResult.Error(R.string.error_no_digit_password)
            else -> ValidationResult.Success
        }
    }
}
