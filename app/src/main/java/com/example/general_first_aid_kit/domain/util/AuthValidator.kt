package com.example.general_first_aid_kit.domain.util

sealed class ValidationResult {
    data object Success : ValidationResult()
    data class Error(val message: String): ValidationResult()
}

object AuthValidator {

    fun validateName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.Error("Имя не может быть пустым")
            name.length > 20 -> ValidationResult.Error("Имя должно быть не длиннее 20 символов")
            name.length < 2 -> ValidationResult.Error("Имя слишком короткое")
            else -> ValidationResult.Success
        }
    }

    fun validateEmail(email: String): ValidationResult {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return when {
            email.isBlank() -> ValidationResult.Error("Введите email")
            !email.matches(emailPattern.toRegex()) -> ValidationResult.Error("Некорректный формат email")
            else -> ValidationResult.Success
        }
    }

    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult.Error("Введите пароль")
            password.length < 6 -> ValidationResult.Error("Пароль должен быть не менее 6 символов")
            !password.any { it.isDigit() } -> ValidationResult.Error("Пароль должен содержать хотя бы одну цифру")
            else -> ValidationResult.Success
        }
    }
}