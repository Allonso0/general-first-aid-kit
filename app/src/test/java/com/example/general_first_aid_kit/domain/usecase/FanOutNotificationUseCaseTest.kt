package com.example.general_first_aid_kit.domain.usecase

import com.example.general_first_aid_kit.domain.model.AppNotification
import com.example.general_first_aid_kit.domain.model.Kit
import com.example.general_first_aid_kit.domain.model.KitType
import com.example.general_first_aid_kit.domain.model.NotificationType
import com.example.general_first_aid_kit.domain.repository.KitRepository
import com.example.general_first_aid_kit.domain.repository.NotificationRepository
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FanOutNotificationUseCaseTest {

    private val kitRepository = mockk<KitRepository>()
    private val notificationRepository = mockk<NotificationRepository>()
    private lateinit var useCase: FanOutNotificationUseCase

    @Before
    fun setUp() {
        useCase = FanOutNotificationUseCase(kitRepository, notificationRepository)
        coJustRun { notificationRepository.saveNotification(any(), any()) }
    }

    @Test
    fun `should_saveNotificationToAllMembersExceptActor_when_sharedKit`() = runTest {
        val kit = fakeKit(
            type = KitType.SHARED,
            userIds = listOf("actor", "user2", "user3")
        )
        coEvery { kitRepository.getKitById(kit.id) } returns Result.success(kit)

        useCase(kit.id, actorUserId = "actor", type = NotificationType.MEMBER_JOINED, message = "text")

        // actor исключён → 2 уведомления
        coVerify(exactly = 2) { notificationRepository.saveNotification(any(), any()) }
        coVerify(exactly = 0) { notificationRepository.saveNotification("actor", any()) }
    }

    @Test
    fun `should_includeActor_when_includeActorIsTrue`() = runTest {
        val kit = fakeKit(
            type = KitType.SHARED,
            userIds = listOf("actor", "user2", "user3")
        )
        coEvery { kitRepository.getKitById(kit.id) } returns Result.success(kit)

        useCase(kit.id, actorUserId = "actor", type = NotificationType.LOW_STOCK,
            message = "text", includeActor = true)

        // все 3 члена включая actor
        coVerify(exactly = 3) { notificationRepository.saveNotification(any(), any()) }
        coVerify(exactly = 1) { notificationRepository.saveNotification("actor", any()) }
    }

    @Test
    fun `should_skipAll_when_personalKitAndMemberJoined`() = runTest {
        val kit = fakeKit(type = KitType.PERSONAL, userIds = listOf("user1"))
        coEvery { kitRepository.getKitById(kit.id) } returns Result.success(kit)

        useCase(kit.id, "user1", NotificationType.MEMBER_JOINED, "text")

        coVerify(exactly = 0) { notificationRepository.saveNotification(any(), any()) }
    }

    @Test
    fun `should_skipAll_when_personalKitAndMemberLeft`() = runTest {
        val kit = fakeKit(type = KitType.PERSONAL, userIds = listOf("user1"))
        coEvery { kitRepository.getKitById(kit.id) } returns Result.success(kit)

        useCase(kit.id, "user1", NotificationType.MEMBER_LEFT, "text")

        coVerify(exactly = 0) { notificationRepository.saveNotification(any(), any()) }
    }

    @Test
    fun `should_skipAll_when_personalKitAndMemberAddedMedication`() = runTest {
        val kit = fakeKit(type = KitType.PERSONAL, userIds = listOf("user1", "user2"))
        coEvery { kitRepository.getKitById(kit.id) } returns Result.success(kit)

        useCase(kit.id, "user1", NotificationType.MEMBER_ADDED_MEDICATION, "text")

        coVerify(exactly = 0) { notificationRepository.saveNotification(any(), any()) }
    }

    @Test
    fun `should_sendNotification_when_personalKitAndExpiredType`() = runTest {
        val kit = fakeKit(
            type = KitType.PERSONAL,
            userIds = listOf("owner", "actor"),
            ownerId = "owner"
        )
        coEvery { kitRepository.getKitById(kit.id) } returns Result.success(kit)

        useCase(kit.id, actorUserId = "actor", type = NotificationType.EXPIRED, message = "text")

        coVerify(exactly = 1) { notificationRepository.saveNotification("owner", any()) }
    }

    @Test
    fun `should_sendNotification_when_personalKitAndLowStockType`() = runTest {
        val kit = fakeKit(type = KitType.PERSONAL, userIds = listOf("user1"))
        coEvery { kitRepository.getKitById(kit.id) } returns Result.success(kit)

        useCase(kit.id, actorUserId = "other", type = NotificationType.LOW_STOCK, message = "text")

        coVerify(exactly = 1) { notificationRepository.saveNotification("user1", any()) }
    }

    @Test
    fun `should_notNotifyArchivedUsers`() = runTest {
        val kit = fakeKit(
            type = KitType.SHARED,
            userIds = listOf("user1", "user2"),
            archivedUserIds = listOf("user2")
        )
        coEvery { kitRepository.getKitById(kit.id) } returns Result.success(kit)

        useCase(kit.id, actorUserId = "actor", type = NotificationType.EXPIRED, message = "text")

        coVerify(exactly = 1) { notificationRepository.saveNotification("user1", any()) }
        coVerify(exactly = 0) { notificationRepository.saveNotification("user2", any()) }
    }

    @Test
    fun `should_sendNoNotifications_when_allUsersArchivedOrActor`() = runTest {
        val kit = fakeKit(
            type = KitType.SHARED,
            userIds = listOf("actor", "user2"),
            archivedUserIds = listOf("user2")
        )
        coEvery { kitRepository.getKitById(kit.id) } returns Result.success(kit)

        useCase(kit.id, actorUserId = "actor", type = NotificationType.EXPIRED, message = "text")

        coVerify(exactly = 0) { notificationRepository.saveNotification(any(), any()) }
    }

    @Test
    fun `should_doNothing_when_kitNotFound`() = runTest {
        coEvery { kitRepository.getKitById(any()) } returns Result.failure(Exception("not found"))

        useCase("nonexistent-kit", "actor", NotificationType.EXPIRED, "text")

        coVerify(exactly = 0) { notificationRepository.saveNotification(any(), any()) }
    }

    @Test
    fun `should_saveNotificationWithCorrectKitIdAndType`() = runTest {
        val notificationSlot = slot<AppNotification>()
        val kit = fakeKit(type = KitType.SHARED, userIds = listOf("actor", "user2"))
        coEvery { kitRepository.getKitById(kit.id) } returns Result.success(kit)
        coEvery { notificationRepository.saveNotification(any(), capture(notificationSlot)) } returns Unit

        useCase(kit.id, actorUserId = "actor", type = NotificationType.MEMBER_JOINED, message = "Иван вступил")

        assertEquals(kit.id, notificationSlot.captured.kitId)
        assertEquals(NotificationType.MEMBER_JOINED, notificationSlot.captured.type)
        assertEquals("Иван вступил", notificationSlot.captured.message)
        assertEquals(false, notificationSlot.captured.isRead)
    }

    private fun fakeKit(
        id: String = "kit-1",
        type: KitType = KitType.SHARED,
        userIds: List<String> = listOf("user1"),
        archivedUserIds: List<String> = emptyList(),
        ownerId: String = "user1",
        name: String = "Аптечка"
    ) = Kit(
        id = id,
        name = name,
        ownerId = ownerId,
        userIds = userIds,
        type = type,
        archivedUserIds = archivedUserIds
    )
}
