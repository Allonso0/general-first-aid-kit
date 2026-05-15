package com.example.general_first_aid_kit.domain.usecase

import com.example.general_first_aid_kit.domain.model.Kit
import com.example.general_first_aid_kit.domain.model.KitType
import com.example.general_first_aid_kit.domain.model.Medication
import com.example.general_first_aid_kit.domain.model.NotificationType
import com.example.general_first_aid_kit.domain.repository.MedicationRepository
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DeleteMedicationUseCaseTest {

    private val repository = mockk<MedicationRepository>()
    private val getKit = mockk<GetKitUseCase>()
    private val fanOutNotification = mockk<FanOutNotificationUseCase>()
    private lateinit var useCase: DeleteMedicationUseCase

    @Before
    fun setUp() {
        useCase = DeleteMedicationUseCase(repository, getKit, fanOutNotification)

        coEvery { repository.deleteMedication(any(), any()) } returns Result.success(Unit)
        coEvery { getKit(any()) } returns Result.success(fakeKit())
        coJustRun { fanOutNotification(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `should_returnSuccess_when_deletionSucceeds`() = runTest {
        val result = useCase("kit-1", fakeMedication(), actorUserId = "user-1", actorName = "Иван")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `should_returnFailure_when_repositoryFails`() = runTest {
        coEvery { repository.deleteMedication(any(), any()) } returns
            Result.failure(Exception("Ошибка удаления"))

        val result = useCase("kit-1", fakeMedication(), "user-1", "Иван")

        assertTrue(result.isFailure)
        assertEquals("Ошибка удаления", result.exceptionOrNull()?.message)
    }

    @Test
    fun `should_fanOutNotification_when_deletionSucceeds`() = runTest {
        useCase("kit-1", fakeMedication(), "user-1", "Иван")

        coVerify(exactly = 1) { fanOutNotification(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `should_notFanOut_when_deletionFails`() = runTest {
        coEvery { repository.deleteMedication(any(), any()) } returns
            Result.failure(Exception("Ошибка"))

        useCase("kit-1", fakeMedication(), "user-1", "Иван")

        coVerify(exactly = 0) { fanOutNotification(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `should_useCorrectNotificationType`() = runTest {
        useCase("kit-1", fakeMedication(), "user-1", "Иван")

        coVerify(exactly = 1) {
            fanOutNotification(any(), any(), eq(NotificationType.MEMBER_REMOVED_MEDICATION), any(), any())
        }
    }

    @Test
    fun `should_includeMedicationNameInMessage`() = runTest {
        var capturedMessage = ""
        coEvery { fanOutNotification(any(), any(), any(), any(), any()) } coAnswers {
            capturedMessage = arg(3)
        }

        useCase("kit-1", fakeMedication(name = "Аспирин"), "user-1", "Иван")

        assertTrue(capturedMessage.contains("Аспирин"))
    }

    @Test
    fun `should_includeActorNameInMessage`() = runTest {
        var capturedMessage = ""
        coEvery { fanOutNotification(any(), any(), any(), any(), any()) } coAnswers {
            capturedMessage = arg(3)
        }

        useCase("kit-1", fakeMedication(), "user-1", "Мария")

        assertTrue(capturedMessage.contains("Мария"))
    }

    @Test
    fun `should_useKitNameInMessage_when_kitExists`() = runTest {
        coEvery { getKit("kit-1") } returns Result.success(fakeKit(name = "Домашняя"))
        var capturedMessage = ""
        coEvery { fanOutNotification(any(), any(), any(), any(), any()) } coAnswers {
            capturedMessage = arg(3)
        }

        useCase("kit-1", fakeMedication(), "user-1", "Иван")

        assertTrue(capturedMessage.contains("Домашняя"))
    }

    @Test
    fun `should_fallbackToKitId_when_kitNotFound`() = runTest {
        coEvery { getKit("kit-42") } returns Result.failure(Exception("not found"))
        var capturedMessage = ""
        coEvery { fanOutNotification(any(), any(), any(), any(), any()) } coAnswers {
            capturedMessage = arg(3)
        }

        useCase("kit-42", fakeMedication(), "user-1", "Иван")

        assertTrue(capturedMessage.contains("kit-42"))
    }

    @Test
    fun `should_passActorUserIdToFanOut`() = runTest {
        useCase("kit-1", fakeMedication(), actorUserId = "actor-99", actorName = "Иван")

        coVerify(exactly = 1) { fanOutNotification(any(), eq("actor-99"), any(), any(), any()) }
    }

    private fun fakeMedication(
        id: String = "med-1",
        name: String = "Аспирин",
        kitId: String = "kit-1"
    ) = Medication(id = id, name = name, kitId = kitId, quantity = 10)

    private fun fakeKit(id: String = "kit-1", name: String = "Аптечка") =
        Kit(id = id, name = name, type = KitType.SHARED, userIds = listOf("user-1"))
}
