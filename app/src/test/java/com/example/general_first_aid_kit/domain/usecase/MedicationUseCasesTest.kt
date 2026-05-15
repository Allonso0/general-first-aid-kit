package com.example.general_first_aid_kit.domain.usecase

import com.example.general_first_aid_kit.domain.model.Medication
import com.example.general_first_aid_kit.domain.repository.MedicationRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class GetMedicationsUseCaseTest {

    private val repository = mockk<MedicationRepository>()
    private lateinit var useCase: GetMedicationsUseCase

    @Before
    fun setUp() {
        useCase = GetMedicationsUseCase(repository)
    }

    @Test
    fun `should_returnMedicationsFromRepository`() = runTest {
        val meds = listOf(fakeMedication("med-1"), fakeMedication("med-2"))
        every { repository.getMedications("kit-1") } returns flowOf(meds)

        val result = useCase("kit-1").first()

        assertEquals(2, result.size)
    }

    @Test
    fun `should_passKitIdToRepository`() = runTest {
        every { repository.getMedications("kit-42") } returns flowOf(emptyList())

        useCase("kit-42")

        verify(exactly = 1) { repository.getMedications("kit-42") }
    }

    @Test
    fun `should_returnEmptyList_when_noMedications`() = runTest {
        every { repository.getMedications(any()) } returns flowOf(emptyList())

        val result = useCase("kit-1").first()

        assertEquals(0, result.size)
    }

    private fun fakeMedication(id: String) = Medication(id = id, name = "Аспирин", kitId = "kit-1", quantity = 5)
}

class GetAllMedicationsUseCaseTest {

    private val repository = mockk<MedicationRepository>()
    private lateinit var useCase: GetAllMedicationsUseCase

    @Before
    fun setUp() {
        useCase = GetAllMedicationsUseCase(repository)
    }

    @Test
    fun `should_returnAllMedicationsFromRepository`() = runTest {
        val meds = listOf(
            Medication("m1", name = "Аспирин", kitId = "kit-1", quantity = 5),
            Medication("m2", name = "Ибупрофен", kitId = "kit-2", quantity = 3)
        )
        every { repository.getAllMedications() } returns flowOf(meds)

        val result = useCase().first()

        assertEquals(2, result.size)
    }

    @Test
    fun `should_callRepositoryGetAllMedications`() = runTest {
        every { repository.getAllMedications() } returns flowOf(emptyList())

        useCase()

        verify(exactly = 1) { repository.getAllMedications() }
    }
}

class GetMedicationUseCaseTest {

    private val repository = mockk<MedicationRepository>()
    private lateinit var useCase: GetMedicationUseCase

    @Before
    fun setUp() {
        useCase = GetMedicationUseCase(repository)
    }

    @Test
    fun `should_returnMedicationFromRepository`() = runTest {
        val med = Medication("med-1", name = "Аспирин", kitId = "kit-1", quantity = 5)
        every { repository.getMedication("kit-1", "med-1") } returns flowOf(med)

        val result = useCase("kit-1", "med-1").first()

        assertEquals("med-1", result?.id)
    }

    @Test
    fun `should_returnNull_when_medicationNotFound`() = runTest {
        every { repository.getMedication(any(), any()) } returns flowOf(null)

        val result = useCase("kit-1", "med-1").first()

        assertNull(result)
    }

    @Test
    fun `should_passKitIdAndMedicationIdToRepository`() = runTest {
        every { repository.getMedication("kit-7", "med-99") } returns flowOf(null)

        useCase("kit-7", "med-99")

        verify(exactly = 1) { repository.getMedication("kit-7", "med-99") }
    }
}
