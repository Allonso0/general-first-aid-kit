package com.example.general_first_aid_kit.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.general_first_aid_kit.domain.model.Kit
import com.example.general_first_aid_kit.domain.model.KitType
import com.example.general_first_aid_kit.domain.model.User
import com.example.general_first_aid_kit.domain.usecase.GetUserUseCase
import com.example.general_first_aid_kit.domain.usecase.JoinKitByCodeUseCase
import com.example.general_first_aid_kit.presentation.screens.JoinKitScreen
import com.example.general_first_aid_kit.presentation.viewmodels.JoinKitViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JoinKitScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val joinKitByCodeUseCase = mockk<JoinKitByCodeUseCase>()
    private val getUserUseCase = mockk<GetUserUseCase>()

    private lateinit var viewModel: JoinKitViewModel

    @Before
    fun setUp() {
        every { getUserUseCase() } returns null
        coEvery { joinKitByCodeUseCase(any(), any(), any()) } returns Result.success(fakeKit())

        viewModel = JoinKitViewModel(joinKitByCodeUseCase, getUserUseCase)
        composeTestRule.setContent {
            JoinKitScreen(
                onNavigateBack = {},
                viewModel = viewModel
            )
        }
    }

    @Test
    fun description_isDisplayed() {
        composeTestRule
            .onNodeWithText("Введите 8-значный код приглашения, чтобы получить доступ к общей аптечке")
            .assertIsDisplayed()
    }

    @Test
    fun joinButton_isDisabled_when_codeIsEmpty() {
        composeTestRule
            .onNode(hasText("Присоединиться") and hasClickAction())
            .assertIsNotEnabled()
    }

    @Test
    fun joinButton_isDisabled_when_codeLessThan8Chars() {
        composeTestRule.onNode(hasSetTextAction()).performTextInput("ABC")
        composeTestRule.waitForIdle()

        composeTestRule
            .onNode(hasText("Присоединиться") and hasClickAction())
            .assertIsNotEnabled()
    }

    @Test
    fun joinButton_isEnabled_when_codeIsExactly8Chars() {
        composeTestRule.onNode(hasSetTextAction()).performTextInput("ABCDE123")
        composeTestRule.waitForIdle()

        composeTestRule
            .onNode(hasText("Присоединиться") and hasClickAction())
            .assertIsEnabled()
    }

    @Test
    fun error_isShown_when_joinFails() {
        every { getUserUseCase() } returns fakeUser()
        coEvery { joinKitByCodeUseCase(any(), any(), any()) } returns
            Result.failure(Exception("Неверный код"))

        composeTestRule.onNode(hasSetTextAction()).performTextInput("ABCDE123")
        composeTestRule.waitForIdle()
        composeTestRule.onNode(hasText("Присоединиться") and hasClickAction()).performClick()

        composeTestRule.waitUntil(timeoutMillis = 2000) {
            composeTestRule.onAllNodesWithText("Неверный код")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNodeWithText("Неверный код").assertIsDisplayed()
    }

    private fun fakeUser() = User(
        id = "user-1",
        email = "test@test.com",
        name = "Тест",
        avatarURL = null
    )

    private fun fakeKit() = Kit(
        id = "kit-1",
        name = "Аптечка",
        type = KitType.SHARED,
        userIds = listOf("user-1")
    )
}
