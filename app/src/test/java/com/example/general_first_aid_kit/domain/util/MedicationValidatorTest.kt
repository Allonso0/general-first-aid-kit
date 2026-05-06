package com.example.general_first_aid_kit.domain.util

import org.junit.Assert.assertTrue
import org.junit.Test

class MedicationValidatorTest {

    @Test
    fun `should_returnSuccess_when_nameIsValid`() {
        val result = MedicationValidator.validateName("Аспирин")
        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `should_returnError_when_nameIsBlank`() {
        val result = MedicationValidator.validateName("")
        assertTrue(result is ValidationResult.Error)
    }

    @Test
    fun `should_returnError_when_nameIsWhitespaceOnly`() {
        val result = MedicationValidator.validateName("   ")
        assertTrue(result is ValidationResult.Error)
    }

    @Test
    fun `should_returnError_when_nameIsSingleChar`() {
        val result = MedicationValidator.validateName("A")
        assertTrue(result is ValidationResult.Error)
    }

    @Test
    fun `should_returnSuccess_when_nameIs2Chars`() {
        val result = MedicationValidator.validateName("AB")
        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `should_returnSuccess_when_nameIs50Chars`() {
        val result = MedicationValidator.validateName("A".repeat(50))
        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `should_returnError_when_nameExceeds50Chars`() {
        val result = MedicationValidator.validateName("A".repeat(51))
        assertTrue(result is ValidationResult.Error)
    }

    @Test
    fun `should_returnSuccess_when_dateIsPositiveMillis`() {
        val result = MedicationValidator.validateExpirationDate(System.currentTimeMillis())
        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `should_returnError_when_dateIsNull`() {
        val result = MedicationValidator.validateExpirationDate(null)
        assertTrue(result is ValidationResult.Error)
    }

    @Test
    fun `should_returnError_when_dateIsZero`() {
        val result = MedicationValidator.validateExpirationDate(0L)
        assertTrue(result is ValidationResult.Error)
    }

    @Test
    fun `should_returnSuccess_when_dateIs1`() {
        val result = MedicationValidator.validateExpirationDate(1L)
        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `should_returnSuccess_when_quantityIsOne`() {
        val result = MedicationValidator.validateQuantity("1")
        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `should_returnSuccess_when_quantityIs1000`() {
        val result = MedicationValidator.validateQuantity("1000")
        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `should_returnError_when_quantityIsBlank`() {
        val result = MedicationValidator.validateQuantity("")
        assertTrue(result is ValidationResult.Error)
    }

    @Test
    fun `should_returnError_when_quantityIsNotANumber`() {
        val result = MedicationValidator.validateQuantity("abc")
        assertTrue(result is ValidationResult.Error)
    }

    @Test
    fun `should_returnError_when_quantityIsZero`() {
        val result = MedicationValidator.validateQuantity("0")
        assertTrue(result is ValidationResult.Error)
    }

    @Test
    fun `should_returnError_when_quantityIsNegative`() {
        val result = MedicationValidator.validateQuantity("-5")
        assertTrue(result is ValidationResult.Error)
    }

    @Test
    fun `should_returnError_when_quantityExceeds1000`() {
        val result = MedicationValidator.validateQuantity("1001")
        assertTrue(result is ValidationResult.Error)
    }

    @Test
    fun `should_returnError_when_quantityIsDecimal`() {
        val result = MedicationValidator.validateQuantity("1.5")
        assertTrue(result is ValidationResult.Error)
    }
}
