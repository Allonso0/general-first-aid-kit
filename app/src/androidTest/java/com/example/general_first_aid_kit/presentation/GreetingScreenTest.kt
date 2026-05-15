package com.example.general_first_aid_kit.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.general_first_aid_kit.presentation.screens.GreetingScreen
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GreetingScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun welcomeTitle_isDisplayed() {
        composeTestRule.setContent {
            GreetingScreen(onNavigateToLogin = {}, onNavigateToRegister = {})
        }

        composeTestRule.onNodeWithText("Все аптечки в одном приложении").assertIsDisplayed()
    }

    @Test
    fun loginButton_isDisplayed() {
        composeTestRule.setContent {
            GreetingScreen(onNavigateToLogin = {}, onNavigateToRegister = {})
        }

        composeTestRule.onNodeWithText("Войти").assertIsDisplayed()
    }

    @Test
    fun registerButton_isDisplayed() {
        composeTestRule.setContent {
            GreetingScreen(onNavigateToLogin = {}, onNavigateToRegister = {})
        }

        composeTestRule.onNodeWithText("Зарегистрироваться").assertIsDisplayed()
    }

    @Test
    fun loginButton_firesOnNavigateToLoginCallback() {
        var fired = false
        composeTestRule.setContent {
            GreetingScreen(
                onNavigateToLogin = { fired = true },
                onNavigateToRegister = {}
            )
        }

        composeTestRule.onNodeWithText("Войти").performClick()

        assertTrue(fired)
    }

    @Test
    fun registerButton_firesOnNavigateToRegisterCallback() {
        var fired = false
        composeTestRule.setContent {
            GreetingScreen(
                onNavigateToLogin = {},
                onNavigateToRegister = { fired = true }
            )
        }

        composeTestRule.onNodeWithText("Зарегистрироваться").performClick()

        assertTrue(fired)
    }
}
