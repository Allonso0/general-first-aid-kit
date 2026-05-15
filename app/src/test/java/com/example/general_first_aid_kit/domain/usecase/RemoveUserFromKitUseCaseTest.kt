package com.example.general_first_aid_kit.domain.usecase

import com.example.general_first_aid_kit.domain.model.Kit
import com.example.general_first_aid_kit.domain.model.KitType
import com.example.general_first_aid_kit.domain.model.NotificationType
import com.example.general_first_aid_kit.domain.repository.KitRepository
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RemoveUserFromKitUseCaseTest {

    private val repository = mockk<KitRepository>()
    private val getKit = mockk<GetKitUseCase>()
    private val fanOutNotification = mockk<FanOutNotificationUseCase>()
    private lateinit var useCase: RemoveUserFromKitUseCase

    @Before
    fun setUp() {
        useCase = RemoveUserFromKitUseCase(repository, getKit, fanOutNotification)

        coEvery { getKit(any()) } returns Result.success(fakeKit())
        coEvery { repository.removeUserFromKit(any(), any()) } returns Result.success(Unit)
        coJustRun { fanOutNotification(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `should_returnSuccess_when_removalSucceeds`() = runTest {
        val result = useCase(kitId = "kit-1", userId = "user-1", actorName = "Иван")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `should_returnFailure_when_repositoryFails`() = runTest {
        coEvery { repository.removeUserFromKit(any(), any()) } returns
            Result.failure(Exception("Ошибка удаления"))

        val result = useCase("kit-1", "user-1", "Иван")

        assertTrue(result.isFailure)
        assertEquals("Ошибка удаления", result.exceptionOrNull()?.message)
    }

    @Test
    fun `should_fanOutNotification_when_removalSucceeds`() = runTest {
        useCase("kit-1", "user-1", "Иван")

        coVerify(exactly = 1) { fanOutNotification(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `should_notFanOut_when_removalFails`() = runTest {
        coEvery { repository.removeUserFromKit(any(), any()) } returns
            Result.failure(Exception("Ошибка"))

        useCase("kit-1", "user-1", "Иван")

        coVerify(exactly = 0) { fanOutNotification(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `should_useCorrectNotificationType`() = runTest {
        useCase("kit-1", "user-1", "Иван")

        coVerify(exactly = 1) {
            fanOutNotification(any(), any(), eq(NotificationType.MEMBER_LEFT), any(), any())
        }
    }

    @Test
    fun `should_includeActorNameInMessage`() = runTest {
        var capturedMessage = ""
        coEvery { fanOutNotification(any(), any(), any(), any(), any()) } coAnswers {
            capturedMessage = arg(3)
        }

        useCase("kit-1", "user-1", "Мария")

        assertTrue(capturedMessage.contains("Мария"))
    }

    @Test
    fun `should_useKitNameInMessage_when_kitExists`() = runTest {
        coEvery { getKit("kit-1") } returns Result.success(fakeKit(name = "Семейная"))
        var capturedMessage = ""
        coEvery { fanOutNotification(any(), any(), any(), any(), any()) } coAnswers {
            capturedMessage = arg(3)
        }

        useCase("kit-1", "user-1", "Иван")

        assertTrue(capturedMessage.contains("Семейная"))
    }

    @Test
    fun `should_fallbackToKitId_when_kitNotFound`() = runTest {
        coEvery { getKit("kit-99") } returns Result.failure(Exception("not found"))
        var capturedMessage = ""
        coEvery { fanOutNotification(any(), any(), any(), any(), any()) } coAnswers {
            capturedMessage = arg(3)
        }

        useCase("kit-99", "user-1", "Иван")

        assertTrue(capturedMessage.contains("kit-99"))
    }

    @Test
    fun `should_passUserIdAsActorUserId_to_fanOut`() = runTest {
        useCase("kit-1", userId = "removed-user", actorName = "Иван")

        coVerify(exactly = 1) { fanOutNotification(any(), eq("removed-user"), any(), any(), any()) }
    }

    @Test
    fun `should_passCorrectKitIdToRepository`() = runTest {
        useCase(kitId = "kit-77", userId = "user-1", actorName = "Иван")

        coVerify(exactly = 1) { repository.removeUserFromKit("kit-77", any()) }
    }

    private fun fakeKit(id: String = "kit-1", name: String = "Аптечка") =
        Kit(id = id, name = name, type = KitType.SHARED, userIds = listOf("user-1", "user-2"))
}
