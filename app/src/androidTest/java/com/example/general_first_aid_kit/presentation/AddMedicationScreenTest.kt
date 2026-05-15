package com.example.general_first_aid_kit.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.general_first_aid_kit.domain.usecase.GetMedicationByBarcodeUseCase
import com.example.general_first_aid_kit.domain.usecase.GetUserUseCase
import com.example.general_first_aid_kit.domain.usecase.SaveMedicationUseCase
import com.example.general_first_aid_kit.presentation.screens.AddMedicationScreen
import com.example.general_first_aid_kit.presentation.viewmodels.AddMedicationViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddMedicationScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val saveMedicationUseCase = mockk<SaveMedicationUseCase>()
    private val getMedicationByBarcodeUseCase = mockk<GetMedicationByBarcodeUseCase>()
    private val getUserUseCase = mockk<GetUserUseCase>()

    private lateinit var viewModel: AddMedicationViewModel

    @Before
    fun setUp() {
        every { getUserUseCase() } returns null
        coEvery {
            saveMedicationUseCase(any(), any(), any(), any(), any(), any())
        } returns Result.success(Unit)

        viewModel = AddMedicationViewModel(
            saveMedicationUseCase, getMedicationByBarcodeUseCase, getUserUseCase
        )

        composeTestRule.setContent {
            AddMedicationScreen(
                kitId = "test-kit-id",
                onNavigateBack = {},
                viewModel = viewModel
            )
        }
    }

    @Test
    fun saveButton_isEnabled_initially() {
        composeTestRule
            .onNode(hasContentDescription("Сохранить") and hasClickAction())
            .assertIsEnabled()
    }

    @Test
    fun nameError_isDisplayed_when_savingWithEmptyName() {
        composeTestRule
            .onNode(hasContentDescription("Сохранить") and hasClickAction())
            .performClick()

        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("Название не может быть пустым")
            .assertIsDisplayed()
    }

    @Test
    fun allValidationErrors_areDisplayed_when_savingWithEmptyInputs() {
        composeTestRule
            .onNode(hasContentDescription("Сохранить") and hasClickAction())
            .performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Название не может быть пустым").assertIsDisplayed()
        composeTestRule.onNodeWithText("Выберите срок годности").assertIsDisplayed()
        composeTestRule.onNodeWithText("Укажите количество").assertIsDisplayed()
    }

    @Test
    fun noValidationErrors_shown_when_validInputsProvided() {
        composeTestRule.onAllNodes(hasSetTextAction())[0].performTextInput("Аспирин")
        viewModel.updateExpirationDate(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000)
        composeTestRule.waitForIdle()
        composeTestRule.onAllNodes(hasSetTextAction())[1].performTextInput("10")
        composeTestRule.waitForIdle()

        composeTestRule
            .onNode(hasContentDescription("Сохранить") and hasClickAction())
            .performClick()

        composeTestRule.waitForIdle()

        val nameErrors = composeTestRule
            .onAllNodesWithText("Название не может быть пустым")
            .fetchSemanticsNodes()
        val dateErrors = composeTestRule
            .onAllNodesWithText("Выберите срок годности")
            .fetchSemanticsNodes()
        val quantityErrors = composeTestRule
            .onAllNodesWithText("Укажите количество")
            .fetchSemanticsNodes()

        assertTrue("Name error should not be shown", nameErrors.isEmpty())
        assertTrue("Date error should not be shown", dateErrors.isEmpty())
        assertTrue("Quantity error should not be shown", quantityErrors.isEmpty())
    }

    @Test
    fun saveMedicationUseCase_isCalled_when_validInputsProvided() {
        composeTestRule.onAllNodes(hasSetTextAction())[0].performTextInput("Аспирин")
        viewModel.updateExpirationDate(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000)
        composeTestRule.waitForIdle()
        composeTestRule.onAllNodes(hasSetTextAction())[1].performTextInput("10")
        composeTestRule.waitForIdle()

        composeTestRule
            .onNode(hasContentDescription("Сохранить") and hasClickAction())
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 2000) {
            try {
                coVerify(exactly = 1) {
                    saveMedicationUseCase(any(), any(), any(), any(), any(), any())
                }
                true
            } catch (e: AssertionError) {
                false
            }
        }

        coVerify(exactly = 1) {
            saveMedicationUseCase(any(), any(), any(), any(), any(), any())
        }
    }
}
