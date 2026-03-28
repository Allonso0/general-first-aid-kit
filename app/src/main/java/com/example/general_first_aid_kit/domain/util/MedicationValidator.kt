package com.example.general_first_aid_kit.domain.util

import com.example.general_first_aid_kit.R

object MedicationValidator {

    fun validateName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.Error(R.string.error_empty_name)
            name.length < 2 -> ValidationResult.Error(R.string.error_short_name)
            name.length > 50 -> ValidationResult.Error(R.string.error_long_name)
            else -> ValidationResult.Success
        }
    }

    fun validateExpirationDate(dateMillis: Long?): ValidationResult {
        return if (dateMillis == null || dateMillis == 0L) {
            ValidationResult.Error(R.string.error_no_date)
        } else {
            ValidationResult.Success
        }
    }

    fun validateQuantity(quantity: String): ValidationResult {
        val quantityInt = quantity.toIntOrNull()
        return when {
            quantity.isBlank() -> ValidationResult.Error(R.string.error_empty_quantity)
            quantityInt == null -> ValidationResult.Error(R.string.error_not_a_number)
            quantityInt <= 0 -> ValidationResult.Error(R.string.error_quantity_zero)
            quantityInt > 1000 -> ValidationResult.Error(R.string.error_quantity_too_high)
            else -> ValidationResult.Success
        }
    }
}
