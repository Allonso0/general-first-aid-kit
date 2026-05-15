package com.example.general_first_aid_kit.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.general_first_aid_kit.domain.usecase.CheckAuthUseCase
import com.example.general_first_aid_kit.domain.usecase.SignInUseCase
import com.example.general_first_aid_kit.domain.usecase.SignOutUseCase
import com.example.general_first_aid_kit.domain.usecase.SignUpUseCase
import com.example.general_first_aid_kit.presentation.screens.RegistrationScreen
import com.example.general_first_aid_kit.presentation.viewmodels.AuthViewModel
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegistrationScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val signInUseCase = mockk<SignInUseCase>(relaxed = true)
    private val signUpUseCase = mockk<SignUpUseCase>(relaxed = true)
    private val signOutUseCase = mockk<SignOutUseCase>(relaxed = true)
    private val checkAuthUseCase = mockk<CheckAuthUseCase>(relaxed = true)

    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        viewModel = AuthViewModel(
            signInUseCase,
            signUpUseCase,
            signOutUseCase,
            checkAuthUseCase,
            ApplicationProvider.getApplicationContext()
        )
        composeTestRule.setContent {
            RegistrationScreen(
                onNavigateBack = {},
                onRegistrationClick = {},
                viewModel = viewModel
            )
        }
    }

    @Test
    fun screenTitle_isDisplayed() {
        composeTestRule.onNodeWithText("Регистрация").assertIsDisplayed()
    }

    @Test
    fun usernameField_isDisplayed() {
        composeTestRule.onNodeWithText("Имя*").assertIsDisplayed()
    }

    @Test
    fun emailField_isDisplayed() {
        composeTestRule.onNodeWithText("Электронная почта*").assertIsDisplayed()
    }

    @Test
    fun registerButton_isDisplayed() {
        composeTestRule.onNodeWithText("Зарегистрироваться").assertIsDisplayed()
    }

    @Test
    fun nameError_isShown_when_registerClickedWithBlankName() {
        composeTestRule.onNodeWithText("Зарегистрироваться").performClick()

        composeTestRule.onNodeWithText("Введите имя").assertIsDisplayed()
    }

    @Test
    fun emailError_isShown_when_registerClickedWithBlankEmail() {
        composeTestRule.onNodeWithText("Зарегистрироваться").performClick()

        composeTestRule.onNodeWithText("Введите email").assertIsDisplayed()
    }

    @Test
    fun passwordError_isShown_when_registerClickedWithBlankPassword() {
        composeTestRule.onNodeWithText("Зарегистрироваться").performClick()

        composeTestRule.onNodeWithText("Введите пароль").assertIsDisplayed()
    }

    @Test
    fun allErrors_areShown_when_registerClickedWithAllFieldsBlank() {
        composeTestRule.onNodeWithText("Зарегистрироваться").performClick()

        composeTestRule.onNodeWithText("Введите имя").assertIsDisplayed()
        composeTestRule.onNodeWithText("Введите email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Введите пароль").assertIsDisplayed()
    }
}
