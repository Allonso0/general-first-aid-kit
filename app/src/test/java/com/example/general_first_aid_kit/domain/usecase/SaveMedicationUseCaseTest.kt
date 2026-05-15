package com.example.general_first_aid_kit.domain.usecase

import com.example.general_first_aid_kit.domain.model.AppSettings
import com.example.general_first_aid_kit.domain.model.Kit
import com.example.general_first_aid_kit.domain.model.KitType
import com.example.general_first_aid_kit.domain.model.Medication
import com.example.general_first_aid_kit.domain.model.NotificationType
import com.example.general_first_aid_kit.domain.repository.MedicationRepository
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SaveMedicationUseCaseTest {

    private val medicationRepository = mockk<MedicationRepository>()
    private val getKit = mockk<GetKitUseCase>()
    private val fanOutNotification = mockk<FanOutNotificationUseCase>()
    private val getAppSettings = mockk<GetAppSettingsUseCase>()
    private lateinit var useCase: SaveMedicationUseCase

    @Before
    fun setUp() {
        useCase = SaveMedicationUseCase(medicationRepository, getKit, fanOutNotification, getAppSettings)

        coEvery { medicationRepository.saveMedication(any(), any(), any()) } returns Result.success(Unit)
        coEvery { getKit(any()) } returns Result.success(fakeKit())
        every { getAppSettings() } returns AppSettings(lowStockThreshold = 5)
        coJustRun { fanOutNotification(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `should_returnSuccess_when_saveSucceeds`() = runTest {
        val result = useCase(
            kitId = "kit-1",
            medication = fakeMedication(quantity = 10),
            localPhotoUri = null,
            actorUserId = "user-1",
            actorName = "Иван",
            isNew = true
        )

        assertTrue(result.isSuccess)
    }

    @Test
    fun `should_fanOutAddedNotification_when_isNewIsTrue`() = runTest {
        useCase("kit-1", fakeMedication(quantity = 10), null, "user-1", "Иван", isNew = true)

        coVerify(exactly = 1) {
            fanOutNotification(any(), any(), eq(NotificationType.MEMBER_ADDED_MEDICATION), any(), any())
        }
    }

    @Test
    fun `should_fanOutEditedNotification_when_isNewIsFalse`() = runTest {
        useCase("kit-1", fakeMedication(quantity = 10), null, "user-1", "Иван", isNew = false)

        coVerify(exactly = 1) {
            fanOutNotification(any(), any(), eq(NotificationType.MEMBER_EDITED_MEDICATION), any(), any())
        }
    }

    @Test
    fun `should_includeActorNameInActivityMessage`() = runTest {
        var capturedMessage = ""
        coEvery { fanOutNotification(any(), any(), any(), any(), any()) } coAnswers {
            capturedMessage = arg(3)
        }

        useCase("kit-1", fakeMedication(name = "Аспирин", quantity = 10), null, "user-1", "Мария", true)

        assertTrue(capturedMessage.contains("Мария"))
        assertTrue(capturedMessage.contains("Аспирин"))
    }

    @Test
    fun `should_fanOutLowStockNotification_when_quantityBelowThreshold`() = runTest {
        every { getAppSettings() } returns AppSettings(lowStockThreshold = 5)

        useCase("kit-1", fakeMedication(quantity = 3), null, "user-1", "Иван", true)

        coVerify(exactly = 2) { fanOutNotification(any(), any(), any(), any(), any()) }
        coVerify(exactly = 1) {
            fanOutNotification(any(), any(), eq(NotificationType.LOW_STOCK), any(), any())
        }
    }

    @Test
    fun `should_fanOutLowStockNotification_when_quantityIsZero`() = runTest {
        every { getAppSettings() } returns AppSettings(lowStockThreshold = 5)

        useCase("kit-1", fakeMedication(quantity = 0), null, "user-1", "Иван", true)

        coVerify(exactly = 1) {
            fanOutNotification(any(), any(), eq(NotificationType.LOW_STOCK), any(), any())
        }
    }

    @Test
    fun `should_fanOutLowStockNotification_when_quantityEqualsThreshold`() = runTest {
        every { getAppSettings() } returns AppSettings(lowStockThreshold = 5)

        useCase("kit-1", fakeMedication(quantity = 5), null, "user-1", "Иван", true)

        coVerify(exactly = 1) {
            fanOutNotification(any(), any(), eq(NotificationType.LOW_STOCK), any(), any())
        }
    }

    @Test
    fun `should_notFanOutLowStock_when_quantityAboveThreshold`() = runTest {
        every { getAppSettings() } returns AppSettings(lowStockThreshold = 5)

        useCase("kit-1", fakeMedication(quantity = 6), null, "user-1", "Иван", true)

        coVerify(exactly = 0) {
            fanOutNotification(any(), any(), eq(NotificationType.LOW_STOCK), any(), any())
        }
    }

    @Test
    fun `should_includeMedicationNameInLowStockMessage`() = runTest {
        every { getAppSettings() } returns AppSettings(lowStockThreshold = 5)
        val messages = mutableListOf<String>()
        coEvery { fanOutNotification(any(), any(), any(), any(), any()) } coAnswers {
            messages.add(arg(3))
        }

        useCase("kit-1", fakeMedication(name = "Ибупрофен", quantity = 2), null, "u", "Иван", true)

        val lowStockMsg = messages.find { it.contains("заканчивается") || it.contains("Ибупрофен") }
        assertTrue("Сообщение low stock должно содержать имя лекарства",
            lowStockMsg?.contains("Ибупрофен") == true)
    }

    @Test
    fun `should_useLowStockWithIncludeActorTrue`() = runTest {
        every { getAppSettings() } returns AppSettings(lowStockThreshold = 5)

        var capturedIncludeActor = false
        coEvery { fanOutNotification(any(), any(), eq(NotificationType.LOW_STOCK), any(), any()) } coAnswers {
            capturedIncludeActor = arg(4)
        }

        useCase("kit-1", fakeMedication(quantity = 2), null, "user-1", "Иван", true)

        assertTrue("Low stock уведомление должно включать actor", capturedIncludeActor)
    }

    @Test
    fun `should_returnFailure_when_repositoryFails`() = runTest {
        coEvery { medicationRepository.saveMedication(any(), any(), any()) } returns
            Result.failure(Exception("Ошибка записи"))

        val result = useCase("kit-1", fakeMedication(), null, "user-1", "Иван", true)

        assertTrue(result.isFailure)
        assertEquals("Ошибка записи", result.exceptionOrNull()?.message)
    }

    @Test
    fun `should_notFanOut_when_repositoryFails`() = runTest {
        coEvery { medicationRepository.saveMedication(any(), any(), any()) } returns
            Result.failure(Exception("Ошибка"))

        useCase("kit-1", fakeMedication(), null, "user-1", "Иван", true)

        coVerify(exactly = 0) { fanOutNotification(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `should_passKitIdAndMedicationToRepository`() = runTest {
        val medication = fakeMedication(id = "med-42", name = "Парацетамол")

        useCase("kit-99", medication, localPhotoUri = "/uri/photo.jpg", "u", "u", true)

        coVerify(exactly = 1) {
            medicationRepository.saveMedication("kit-99", eq(medication), "/uri/photo.jpg")
        }
    }

    @Test
    fun `should_fallbackToKitId_in_activityMessage_when_kitNotFound`() = runTest {
        coEvery { getKit(any()) } returns Result.failure(Exception("not found"))
        var capturedMessage = ""
        coEvery { fanOutNotification(any(), any(), any(), any(), any()) } coAnswers {
            capturedMessage = arg(3)
        }

        useCase("kit-fallback", fakeMedication(), null, "user-1", "Иван", true)

        assertTrue(capturedMessage.contains("kit-fallback"))
    }

    @Test
    fun `should_includeKitNameInActivityMessage`() = runTest {
        coEvery { getKit(any()) } returns Result.success(fakeKit(name = "Домашняя"))
        var capturedMessage = ""
        coEvery { fanOutNotification(any(), any(), any(), any(), any()) } coAnswers {
            capturedMessage = arg(3)
        }

        useCase("kit-1", fakeMedication(name = "Аспирин"), null, "user-1", "Иван", true)

        assertTrue(capturedMessage.contains("Домашняя"))
    }

    @Test
    fun `should_includeKitNameInLowStockMessage`() = runTest {
        every { getAppSettings() } returns AppSettings(lowStockThreshold = 5)
        coEvery { getKit(any()) } returns Result.success(fakeKit(name = "Семейная"))
        val messages = mutableListOf<String>()
        coEvery { fanOutNotification(any(), any(), any(), any(), any()) } coAnswers {
            messages.add(arg(3))
        }

        useCase("kit-1", fakeMedication(name = "Ибупрофен", quantity = 2), null, "user-1", "Иван", true)

        val lowStockMsg = messages.find { it.contains("заканчивается") }
        assertTrue("Сообщение low stock должно содержать имя аптечки",
            lowStockMsg?.contains("Семейная") == true)
    }

    private fun fakeMedication(
        id: String = "med-1",
        name: String = "Аспирин",
        quantity: Int = 10,
        kitId: String = "kit-1"
    ) = Medication(id = id, name = name, quantity = quantity, kitId = kitId)

    private fun fakeKit(id: String = "kit-1", name: String = "Аптечка") =
        Kit(id = id, name = name, type = KitType.SHARED, userIds = listOf("user-1"))
}
