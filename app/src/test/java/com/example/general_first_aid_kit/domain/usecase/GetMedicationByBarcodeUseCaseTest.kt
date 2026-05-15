package com.example.general_first_aid_kit.domain.usecase

import com.example.general_first_aid_kit.domain.model.MedicationSuggestion
import com.example.general_first_aid_kit.domain.repository.AiMedicationRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetMedicationByBarcodeUseCaseTest {

    private val repository = mockk<AiMedicationRepository>()
    private lateinit var useCase: GetMedicationByBarcodeUseCase

    @Before
    fun setUp() {
        useCase = GetMedicationByBarcodeUseCase(repository)
    }

    @Test
    fun `should_passBarcodeToRepository`() = runTest {
        val barcode = "4607085110015"
        coEvery { repository.getMedicationByBarcode(barcode) } returns Result.success(fakeSuggestion())

        useCase(barcode)

        coVerify(exactly = 1) { repository.getMedicationByBarcode(barcode) }
    }

    @Test
    fun `should_notCallRepository_withDifferentBarcode`() = runTest {
        val barcode = "1234567890123"
        coEvery { repository.getMedicationByBarcode(barcode) } returns Result.success(fakeSuggestion())

        useCase(barcode)

        coVerify(exactly = 0) { repository.getMedicationByBarcode("9999999999999") }
    }

    @Test
    fun `should_returnSuccess_when_repositoryReturnsData`() = runTest {
        val suggestion = fakeSuggestion(name = "Аспирин")
        coEvery { repository.getMedicationByBarcode(any()) } returns Result.success(suggestion)

        val result = useCase("4607085110015")

        assertTrue(result.isSuccess)
        assertEquals("Аспирин", result.getOrNull()?.name)
    }

    @Test
    fun `should_returnExactSuggestion_from_repository`() = runTest {
        val expected = fakeSuggestion(name = "Ибупрофен", category = "Анальгетик", quantity = 20, unit = "таб")
        coEvery { repository.getMedicationByBarcode(any()) } returns Result.success(expected)

        val result = useCase("0000000000001")

        val actual = result.getOrNull()
        assertEquals(expected.name, actual?.name)
        assertEquals(expected.category, actual?.category)
        assertEquals(expected.quantity, actual?.quantity)
        assertEquals(expected.unit, actual?.unit)
    }

    @Test
    fun `should_returnFailure_when_repositoryFails`() = runTest {
        coEvery { repository.getMedicationByBarcode(any()) } returns
            Result.failure(Exception("Штрих-код не найден"))

        val result = useCase("0000000000000")

        assertTrue(result.isFailure)
        assertEquals("Штрих-код не найден", result.exceptionOrNull()?.message)
    }

    @Test
    fun `should_propagateExceptionType_when_repositoryThrows`() = runTest {
        coEvery { repository.getMedicationByBarcode(any()) } returns
            Result.failure(IllegalStateException("API недоступен"))

        val result = useCase("1111111111111")

        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }

    private fun fakeSuggestion(
        name: String = "Парацетамол",
        category: String = "Жаропонижающее",
        quantity: Int = 10,
        unit: String = "таб"
    ) = MedicationSuggestion(
        name = name,
        category = category,
        quantity = quantity,
        unit = unit
    )
}
