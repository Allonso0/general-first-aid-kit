package com.example.general_first_aid_kit.domain.util

import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthValidatorTest {

    @Test
    fun `should_returnSuccess_when_nameIsValid`() {
        val result = AuthValidator.validateName("Иван")
        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `should_returnError_when_nameIsBlank`() {
        val result = AuthValidator.validateName("   ")
        assertTrue(result is ValidationResult.Error)
    }

    @Test
    fun `should_returnError_when_nameIsSingleChar`() {
        val result = AuthValidator.validateName("A")
        assertTrue(result is ValidationResult.Error)
    }

    @Test
    fun `should_returnError_when_nameExceeds20Chars`() {
        val result = AuthValidator.validateName("A".repeat(21))
        assertTrue(result is ValidationResult.Error)
    }

    @Test
    fun `should_returnSuccess_when_nameIs2Chars`() {
        val result = AuthValidator.validateName("AB")
        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `should_returnSuccess_when_nameIs20Chars`() {
        val result = AuthValidator.validateName("A".repeat(20))
        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `should_returnSuccess_when_emailIsValid`() {
        val result = AuthValidator.validateEmail("user@example.com")
        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `should_returnError_when_emailIsBlank`() {
        val result = AuthValidator.validateEmail("")
        assertTrue(result is ValidationResult.Error)
    }

    @Test
    fun `should_returnError_when_emailHasNoAt`() {
        val result = AuthValidator.validateEmail("notanemail")
        assertTrue(result is ValidationResult.Error)
    }

    @Test
    fun `should_returnError_when_emailHasNoDomain`() {
        val result = AuthValidator.validateEmail("user@")
        assertTrue(result is ValidationResult.Error)
    }

    @Test
    fun `should_returnSuccess_when_emailIsShortValid`() {
        val result = AuthValidator.validateEmail("a@b.ru")
        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `should_returnSuccess_when_passwordHasDigitAndMinLength`() {
        val result = AuthValidator.validatePassword("pass1word")
        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `should_returnError_when_passwordIsBlank`() {
        val result = AuthValidator.validatePassword("")
        assertTrue(result is ValidationResult.Error)
    }

    @Test
    fun `should_returnError_when_passwordIsTooShort`() {
        val result = AuthValidator.validatePassword("ab1")
        assertTrue(result is ValidationResult.Error)
    }

    @Test
    fun `should_returnError_when_passwordHasNoDigit`() {
        val result = AuthValidator.validatePassword("password")
        assertTrue(result is ValidationResult.Error)
    }

    @Test
    fun `should_returnSuccess_when_passwordIsExactly6CharsWithDigit`() {
        val result = AuthValidator.validatePassword("pass1!")
        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `should_returnError_messageResId_when_nameIsBlank`() {
        val result = AuthValidator.validateName("") as ValidationResult.Error
        assertTrue(result.messageResId != 0)
    }
}
