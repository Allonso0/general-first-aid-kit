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

class JoinKitByCodeUseCaseTest {

    private val kitRepository = mockk<KitRepository>()
    private val fanOutNotification = mockk<FanOutNotificationUseCase>()
    private lateinit var useCase: JoinKitByCodeUseCase

    @Before
    fun setUp() {
        useCase = JoinKitByCodeUseCase(kitRepository, fanOutNotification)
        coJustRun { fanOutNotification(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `should_returnKit_when_joinSucceeds`() = runTest {
        val kit = fakeKit(name = "Семейная аптечка")
        coEvery { kitRepository.joinKitByCode("user-1", "ABC12345") } returns Result.success(kit)

        val result = useCase(userId = "user-1", inviteCode = "ABC12345", actorName = "Иван")

        assertTrue(result.isSuccess)
        assertEquals(kit.name, result.getOrNull()?.name)
    }

    @Test
    fun `should_fanOutMemberJoinedNotification_when_joinSucceeds`() = runTest {
        val kit = fakeKit(id = "kit-99")
        coEvery { kitRepository.joinKitByCode(any(), any()) } returns Result.success(kit)

        useCase(userId = "user-1", inviteCode = "CODE1234", actorName = "Мария")

        coVerify(exactly = 1) {
            fanOutNotification(
                kitId = "kit-99",
                actorUserId = "user-1",
                type = NotificationType.MEMBER_JOINED,
                message = any(),
                includeActor = any()
            )
        }
    }

    @Test
    fun `should_includeMemberNameInMessage_when_fanningOut`() = runTest {
        val kit = fakeKit(name = "Общая")
        coEvery { kitRepository.joinKitByCode(any(), any()) } returns Result.success(kit)

        var capturedMessage = ""
        coEvery {
            fanOutNotification(any(), any(), any(), any(), any())
        } coAnswers {
            capturedMessage = arg(3)
        }

        useCase(userId = "user-1", inviteCode = "CODE1234", actorName = "Мария")

        assertTrue(capturedMessage.contains("Мария"))
        assertTrue(capturedMessage.contains("Общая"))
    }

    @Test
    fun `should_returnFailure_when_repositoryFails`() = runTest {
        coEvery { kitRepository.joinKitByCode(any(), any()) } returns
            Result.failure(Exception("Неверный код приглашения"))

        val result = useCase("user-1", "BADCODE", "Иван")

        assertTrue(result.isFailure)
        assertEquals("Неверный код приглашения", result.exceptionOrNull()?.message)
    }

    @Test
    fun `should_notFanOut_when_joinFails`() = runTest {
        coEvery { kitRepository.joinKitByCode(any(), any()) } returns
            Result.failure(Exception("Ошибка"))

        useCase("user-1", "BADCODE", "Иван")

        coVerify(exactly = 0) { fanOutNotification(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `should_passUserIdAndCodeToRepository`() = runTest {
        coEvery { kitRepository.joinKitByCode("user-42", "INVITE01") } returns
            Result.success(fakeKit())

        useCase("user-42", "INVITE01", "Тест")

        coVerify(exactly = 1) { kitRepository.joinKitByCode("user-42", "INVITE01") }
    }

    private fun fakeKit(
        id: String = "kit-1",
        name: String = "Аптечка",
        type: KitType = KitType.SHARED,
        userIds: List<String> = listOf("user-1", "user-2")
    ) = Kit(id = id, name = name, type = type, userIds = userIds)
}
