package com.example.general_first_aid_kit.domain.usecase

import com.example.general_first_aid_kit.domain.model.AppNotification
import com.example.general_first_aid_kit.domain.model.KitNotificationSettings
import com.example.general_first_aid_kit.domain.model.NotificationType
import com.example.general_first_aid_kit.domain.repository.KitNotificationSettingsRepository
import com.example.general_first_aid_kit.domain.repository.NotificationRepository
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SaveNotificationUseCaseTest {

    private val repository = mockk<NotificationRepository>()
    private lateinit var useCase: SaveNotificationUseCase

    @Before
    fun setUp() {
        useCase = SaveNotificationUseCase(repository)
        coJustRun { repository.saveNotification(any(), any()) }
    }

    @Test
    fun `should_callRepositoryWithUserIdAndNotification`() = runTest {
        val notification = fakeNotification()

        useCase("user-1", notification)

        coVerify(exactly = 1) { repository.saveNotification("user-1", notification) }
    }

    @Test
    fun `should_passCorrectUserId_to_repository`() = runTest {
        val notification = fakeNotification()

        useCase("user-42", notification)

        coVerify(exactly = 1) { repository.saveNotification("user-42", any()) }
    }

    private fun fakeNotification() = AppNotification(
        id = "n-1", kitId = "kit-1", type = NotificationType.EXPIRED, message = "Истёк срок"
    )
}

class GetNotificationsUseCaseTest {

    private val repository = mockk<NotificationRepository>()
    private lateinit var useCase: GetNotificationsUseCase

    @Before
    fun setUp() {
        useCase = GetNotificationsUseCase(repository)
    }

    @Test
    fun `should_returnNotificationsFromRepository`() = runTest {
        val notifications = listOf(
            AppNotification(id = "n-1", type = NotificationType.EXPIRED),
            AppNotification(id = "n-2", type = NotificationType.LOW_STOCK)
        )
        every { repository.observeNotifications("user-1") } returns flowOf(notifications)

        val result = useCase("user-1").first()

        assertEquals(2, result.size)
    }

    @Test
    fun `should_passUserIdToRepository`() = runTest {
        every { repository.observeNotifications("user-7") } returns flowOf(emptyList())

        useCase("user-7")

        verify(exactly = 1) { repository.observeNotifications("user-7") }
    }
}

class MarkNotificationsReadUseCaseTest {

    private val repository = mockk<NotificationRepository>()
    private lateinit var useCase: MarkNotificationsReadUseCase

    @Before
    fun setUp() {
        useCase = MarkNotificationsReadUseCase(repository)
        coJustRun { repository.markAllAsRead(any()) }
    }

    @Test
    fun `should_callMarkAllAsReadWithUserId`() = runTest {
        useCase("user-1")

        coVerify(exactly = 1) { repository.markAllAsRead("user-1") }
    }

    @Test
    fun `should_passCorrectUserId`() = runTest {
        useCase("user-99")

        coVerify(exactly = 1) { repository.markAllAsRead("user-99") }
    }
}

class DeleteAllNotificationsUseCaseTest {

    private val repository = mockk<NotificationRepository>()
    private lateinit var useCase: DeleteAllNotificationsUseCase

    @Before
    fun setUp() {
        useCase = DeleteAllNotificationsUseCase(repository)
        coJustRun { repository.deleteAllNotifications(any()) }
    }

    @Test
    fun `should_callDeleteAllNotificationsWithUserId`() = runTest {
        useCase("user-1")

        coVerify(exactly = 1) { repository.deleteAllNotifications("user-1") }
    }

    @Test
    fun `should_passCorrectUserId`() = runTest {
        useCase("user-55")

        coVerify(exactly = 1) { repository.deleteAllNotifications("user-55") }
    }
}

class GetKitNotificationSettingsUseCaseTest {

    private val repository = mockk<KitNotificationSettingsRepository>()
    private lateinit var useCase: GetKitNotificationSettingsUseCase

    @Before
    fun setUp() {
        useCase = GetKitNotificationSettingsUseCase(repository)
    }

    @Test
    fun `should_returnSettingsFromRepository`() = runTest {
        val settings = KitNotificationSettings(kitId = "kit-1", userId = "user-1", notifyExpiry = false)
        coEvery { repository.getSettings("user-1", "kit-1") } returns settings

        val result = useCase("user-1", "kit-1")

        assertEquals(false, result.notifyExpiry)
    }

    @Test
    fun `should_passUserIdAndKitIdToRepository`() = runTest {
        coEvery { repository.getSettings("user-7", "kit-9") } returns KitNotificationSettings()

        useCase("user-7", "kit-9")

        coVerify(exactly = 1) { repository.getSettings("user-7", "kit-9") }
    }
}

class UpdateKitNotificationSettingsUseCaseTest {

    private val repository = mockk<KitNotificationSettingsRepository>()
    private lateinit var useCase: UpdateKitNotificationSettingsUseCase

    @Before
    fun setUp() {
        useCase = UpdateKitNotificationSettingsUseCase(repository)
        coJustRun { repository.saveSettings(any(), any()) }
    }

    @Test
    fun `should_callSaveSettingsWithUserIdAndSettings`() = runTest {
        val settings = KitNotificationSettings(kitId = "kit-1", userId = "user-1", notifyLowStock = false)

        useCase("user-1", settings)

        coVerify(exactly = 1) { repository.saveSettings("user-1", settings) }
    }

    @Test
    fun `should_passCorrectUserId`() = runTest {
        val settings = KitNotificationSettings()

        useCase("user-42", settings)

        coVerify(exactly = 1) { repository.saveSettings("user-42", any()) }
    }
}
