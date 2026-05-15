package com.example.general_first_aid_kit.presentation.viewmodels

import app.cash.turbine.test
import com.example.general_first_aid_kit.domain.model.MedicationSuggestion
import com.example.general_first_aid_kit.domain.model.User
import com.example.general_first_aid_kit.domain.usecase.GetMedicationByBarcodeUseCase
import com.example.general_first_aid_kit.domain.usecase.GetUserUseCase
import com.example.general_first_aid_kit.domain.usecase.SaveMedicationUseCase
import com.example.general_first_aid_kit.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddMedicationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val saveMedicationUseCase = mockk<SaveMedicationUseCase>()
    private val getMedicationByBarcodeUseCase = mockk<GetMedicationByBarcodeUseCase>()
    private val getUserUseCase = mockk<GetUserUseCase>()
    private lateinit var viewModel: AddMedicationViewModel

    @Before
    fun setUp() {
        viewModel = AddMedicationViewModel(
            saveMedicationUseCase,
            getMedicationByBarcodeUseCase,
            getUserUseCase
        )
        every { getUserUseCase() } returns fakeUser()
    }

    @Test
    fun `should_updateName_when_updateNameCalled`() {
        viewModel.updateName("Аспирин")

        assertEquals("Аспирин", viewModel.uiState.value.name)
    }

    @Test
    fun `should_updateQuantity_when_updateQuantityCalled`() {
        viewModel.updateQuantity("15")

        assertEquals("15", viewModel.uiState.value.quantity)
    }

    @Test
    fun `should_updateExpirationDate_when_updateExpirationDateCalled`() {
        viewModel.updateExpirationDate(1000000L)

        assertEquals(1000000L, viewModel.uiState.value.expirationDateMillis)
    }

    @Test
    fun `should_clearNameError_when_nameUpdated`() = runTest {
        viewModel.updateExpirationDate(1000000L)
        viewModel.updateQuantity("5")
        viewModel.saveMedication("kit-1") {}

        assertNotNull(viewModel.uiState.value.nameErrorResId)

        viewModel.updateName("Аспирин")

        assertNull(viewModel.uiState.value.nameErrorResId)
    }

    @Test
    fun `should_clearError_when_clearErrorCalled`() = runTest {
        coEvery { saveMedicationUseCase(any(), any(), any(), any(), any(), any()) } returns
            Result.failure(Exception("Ошибка"))
        fillValidForm()
        viewModel.saveMedication("kit-1") {}

        viewModel.clearError()

        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `should_emitNameError_when_nameIsBlank_on_save`() = runTest {
        viewModel.updateExpirationDate(1000000L)
        viewModel.updateQuantity("5")

        viewModel.saveMedication("kit-1") {}

        assertNotNull(viewModel.uiState.value.nameErrorResId)
    }

    @Test
    fun `should_emitDateError_when_dateIsNull_on_save`() = runTest {
        viewModel.updateName("Аспирин")
        viewModel.updateQuantity("5")

        viewModel.saveMedication("kit-1") {}

        assertNotNull(viewModel.uiState.value.expirationDateErrorResId)
    }

    @Test
    fun `should_emitQuantityError_when_quantityIsInvalid_on_save`() = runTest {
        viewModel.updateName("Аспирин")
        viewModel.updateExpirationDate(1000000L)
        viewModel.updateQuantity("abc")

        viewModel.saveMedication("kit-1") {}

        assertNotNull(viewModel.uiState.value.quantityErrorResId)
    }

    @Test
    fun `should_emitAllErrors_when_allFieldsInvalid_on_save`() = runTest {
        viewModel.saveMedication("kit-1") {}

        assertNotNull(viewModel.uiState.value.nameErrorResId)
        assertNotNull(viewModel.uiState.value.expirationDateErrorResId)
        assertNotNull(viewModel.uiState.value.quantityErrorResId)
    }

    @Test
    fun `should_notCallRepository_when_validationFails`() = runTest {
        viewModel.saveMedication("kit-1") {}

        coVerify(exactly = 0) { saveMedicationUseCase(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `should_callOnSuccess_when_saveSucceeds`() = runTest {
        coEvery { saveMedicationUseCase(any(), any(), any(), any(), any(), any()) } returns
            Result.success(Unit)
        fillValidForm()
        var successCalled = false

        viewModel.saveMedication("kit-1") { successCalled = true }

        assertTrue(successCalled)
    }

    @Test
    fun `should_clearLoadingAfterSuccess`() = runTest {
        coEvery { saveMedicationUseCase(any(), any(), any(), any(), any(), any()) } returns
            Result.success(Unit)
        fillValidForm()

        viewModel.saveMedication("kit-1") {}

        assertTrue(!viewModel.uiState.value.isLoading)
    }

    @Test
    fun `should_passKitIdToUseCase`() = runTest {
        coEvery { saveMedicationUseCase(any(), any(), any(), any(), any(), any()) } returns
            Result.success(Unit)
        fillValidForm()

        viewModel.saveMedication("kit-specific-99") {}

        coVerify { saveMedicationUseCase("kit-specific-99", any(), any(), any(), any(), any()) }
    }

    @Test
    fun `should_passIsNewTrueToUseCase`() = runTest {
        coEvery { saveMedicationUseCase(any(), any(), any(), any(), any(), any()) } returns
            Result.success(Unit)
        fillValidForm()

        viewModel.saveMedication("kit-1") {}

        coVerify { saveMedicationUseCase(any(), any(), any(), any(), any(), isNew = true) }
    }

    @Test
    fun `should_setError_when_saveFails`() = runTest {
        coEvery { saveMedicationUseCase(any(), any(), any(), any(), any(), any()) } returns
            Result.failure(Exception("Нет соединения"))
        fillValidForm()

        viewModel.saveMedication("kit-1") {}

        assertEquals("Нет соединения", viewModel.uiState.value.error)
    }

    @Test
    fun `should_clearLoadingAfterFailure`() = runTest {
        coEvery { saveMedicationUseCase(any(), any(), any(), any(), any(), any()) } returns
            Result.failure(Exception("Ошибка"))
        fillValidForm()

        viewModel.saveMedication("kit-1") {}

        assertTrue(!viewModel.uiState.value.isLoading)
    }

    @Test
    fun `should_notCallOnSuccess_when_saveFails`() = runTest {
        coEvery { saveMedicationUseCase(any(), any(), any(), any(), any(), any()) } returns
            Result.failure(Exception("Ошибка"))
        fillValidForm()
        var successCalled = false

        viewModel.saveMedication("kit-1") { successCalled = true }

        assertTrue(!successCalled)
    }

    @Test
    fun `should_emitLoadingThenResult_when_save`() = runTest {
        coEvery { saveMedicationUseCase(any(), any(), any(), any(), any(), any()) } returns
            Result.success(Unit)
        fillValidForm()

        viewModel.uiState.test {
            awaitItem()

            viewModel.saveMedication("kit-1") {}

            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

            val doneState = awaitItem()
            assertTrue(!doneState.isLoading)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should_populateFields_when_barcodeResolved`() = runTest {
        val suggestion = MedicationSuggestion(
            name = "Ибупрофен",
            category = "Анальгетик",
            quantity = 20,
            unit = "таб"
        )
        coEvery { getMedicationByBarcodeUseCase(any()) } returns Result.success(suggestion)

        viewModel.loadDetailsByBarcode("4607085110015")

        with(viewModel.uiState.value) {
            assertEquals("Ибупрофен", name)
            assertEquals("Анальгетик", category)
            assertEquals("20", quantity)
            assertEquals("таб", unit)
        }
    }

    @Test
    fun `should_clearLoadingAfterBarcodeResolution`() = runTest {
        coEvery { getMedicationByBarcodeUseCase(any()) } returns
            Result.success(MedicationSuggestion("A", "B", 1, "шт"))

        viewModel.loadDetailsByBarcode("1234567890")

        assertTrue(!viewModel.uiState.value.isLoading)
    }

    @Test
    fun `should_emitError_when_barcodeResolutionFails`() = runTest {
        coEvery { getMedicationByBarcodeUseCase(any()) } returns
            Result.failure(Exception("Штрих-код не найден"))

        viewModel.loadDetailsByBarcode("0000000000000")

        assertNotNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.error!!.contains("Штрих-код не найден"))
    }

    @Test
    fun `should_clearLoadingAfterBarcodeFailure`() = runTest {
        coEvery { getMedicationByBarcodeUseCase(any()) } returns
            Result.failure(Exception("Ошибка"))

        viewModel.loadDetailsByBarcode("0000000000000")

        assertTrue(!viewModel.uiState.value.isLoading)
    }

    @Test
    fun `should_notOverwriteExistingFields_when_barcodeResolvesPartially`() = runTest {
        viewModel.updateExpirationDate(999L) // пользователь уже выбрал дату
        val suggestion = MedicationSuggestion("Аспирин", "Жаропонижающее", 10, "таб")
        coEvery { getMedicationByBarcodeUseCase(any()) } returns Result.success(suggestion)

        viewModel.loadDetailsByBarcode("123")

        assertEquals(999L, viewModel.uiState.value.expirationDateMillis)
    }

    private fun fillValidForm() {
        viewModel.updateName("Аспирин")
        viewModel.updateExpirationDate(System.currentTimeMillis() + 86400000L)
        viewModel.updateQuantity("10")
    }

    private fun fakeUser(id: String = "user-1") = User(
        id = id,
        email = "test@test.com",
        name = "Тест",
        avatarURL = null
    )
}
