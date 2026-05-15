package com.example.general_first_aid_kit.data.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.example.general_first_aid_kit.domain.model.AppNotification
import com.example.general_first_aid_kit.domain.model.AppSettings
import com.example.general_first_aid_kit.domain.model.Kit
import com.example.general_first_aid_kit.domain.model.KitNotificationSettings
import com.example.general_first_aid_kit.domain.model.Medication
import com.example.general_first_aid_kit.domain.model.NotificationType
import com.example.general_first_aid_kit.domain.usecase.GetAllMedicationsUseCase
import com.example.general_first_aid_kit.domain.usecase.GetAppSettingsUseCase
import com.example.general_first_aid_kit.domain.usecase.GetKitNotificationSettingsUseCase
import com.example.general_first_aid_kit.domain.usecase.GetKitUseCase
import com.example.general_first_aid_kit.domain.usecase.SaveNotificationUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExpiryCheckWorkerTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    private val auth = mockk<FirebaseAuth>()
    private val getAllMedications = mockk<GetAllMedicationsUseCase>()
    private val getKit = mockk<GetKitUseCase>()
    private val saveNotification = mockk<SaveNotificationUseCase>()
    private val getSettings = mockk<GetKitNotificationSettingsUseCase>()
    private val getAppSettings = mockk<GetAppSettingsUseCase>()

    private fun buildWorker() = TestListenableWorkerBuilder<ExpiryCheckWorker>(context)
        .setWorkerFactory(object : WorkerFactory() {
            override fun createWorker(
                appContext: Context,
                workerClassName: String,
                workerParameters: WorkerParameters
            ): ListenableWorker = ExpiryCheckWorker(
                appContext, workerParameters,
                auth, getAllMedications, getKit, saveNotification, getSettings, getAppSettings
            )
        })
        .build()

    @Before
    fun setUp() {
        val user = mockk<FirebaseUser>()
        every { user.uid } returns "user-1"
        every { auth.currentUser } returns user

        every { getAllMedications() } returns flowOf(emptyList())
        every { getAppSettings() } returns AppSettings(expiryWarningDays = 7, lowStockThreshold = 2)
        coJustRun { saveNotification(any(), any()) }
        coEvery { getKit(any()) } returns Result.success(fakeKit())
        coEvery { getSettings(any(), any()) } returns KitNotificationSettings(notifyExpiry = false)
    }

    @Test
    fun doWork_returnsSuccess_when_userNotLoggedIn() = runTest {
        every { auth.currentUser } returns null

        val result = buildWorker().doWork()

        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun doWork_doesNotSaveNotification_when_userNotLoggedIn() = runTest {
        every { auth.currentUser } returns null

        buildWorker().doWork()

        coVerify(exactly = 0) { saveNotification(any(), any()) }
    }

    @Test
    fun doWork_returnsSuccess_when_noMedications() = runTest {
        every { getAllMedications() } returns flowOf(emptyList())

        val result = buildWorker().doWork()

        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun doWork_doesNotSaveNotification_when_expirationDateIsZero() = runTest {
        every { getAllMedications() } returns flowOf(listOf(fakeMedication(expirationDate = 0L)))

        buildWorker().doWork()

        coVerify(exactly = 0) { saveNotification(any(), any()) }
    }

    @Test
    fun doWork_savesExpiredNotification_when_medicationExpired() = runTest {
        val yesterday = System.currentTimeMillis() - 86_400_000L
        every { getAllMedications() } returns flowOf(listOf(fakeMedication(expirationDate = yesterday)))

        buildWorker().doWork()

        coVerify(exactly = 1) {
            saveNotification("user-1", match { it.type == NotificationType.EXPIRED })
        }
    }

    @Test
    fun doWork_savesExpiryWarningNotification_when_medicationExpiresSoon() = runTest {
        val threeDaysFromNow = System.currentTimeMillis() + 3 * 86_400_000L
        every { getAllMedications() } returns flowOf(listOf(fakeMedication(expirationDate = threeDaysFromNow)))

        buildWorker().doWork()

        coVerify(exactly = 1) {
            saveNotification("user-1", match { it.type == NotificationType.EXPIRY_WARNING })
        }
    }

    @Test
    fun doWork_doesNotSaveNotification_when_expirationFarInFuture() = runTest {
        val thirtyDaysFromNow = System.currentTimeMillis() + 30 * 86_400_000L
        every { getAllMedications() } returns flowOf(listOf(fakeMedication(expirationDate = thirtyDaysFromNow)))

        buildWorker().doWork()

        coVerify(exactly = 0) { saveNotification(any(), any()) }
    }

    @Test
    fun doWork_doesNotSaveNotification_when_userIsArchived() = runTest {
        val yesterday = System.currentTimeMillis() - 86_400_000L
        every { getAllMedications() } returns flowOf(listOf(fakeMedication(expirationDate = yesterday)))
        coEvery { getKit(any()) } returns Result.success(fakeKit(archivedUserIds = listOf("user-1")))

        buildWorker().doWork()

        coVerify(exactly = 0) { saveNotification(any(), any()) }
    }

    @Test
    fun doWork_includesMedicationNameInNotificationMessage() = runTest {
        val yesterday = System.currentTimeMillis() - 86_400_000L
        every { getAllMedications() } returns flowOf(listOf(fakeMedication(name = "Парацетамол", expirationDate = yesterday)))
        val slot = slot<AppNotification>()
        coEvery { saveNotification(any(), capture(slot)) } just Runs

        buildWorker().doWork()

        assertTrue(slot.captured.message.contains("Парацетамол"))
    }

    @Test
    fun doWork_includesKitNameInNotificationMessage() = runTest {
        val yesterday = System.currentTimeMillis() - 86_400_000L
        every { getAllMedications() } returns flowOf(listOf(fakeMedication(expirationDate = yesterday)))
        coEvery { getKit(any()) } returns Result.success(fakeKit(name = "Домашняя"))
        val slot = slot<AppNotification>()
        coEvery { saveNotification(any(), capture(slot)) } just Runs

        buildWorker().doWork()

        assertTrue(slot.captured.message.contains("Домашняя"))
    }

    @Test
    fun doWork_returnsSuccess_on_happyPath() = runTest {
        val yesterday = System.currentTimeMillis() - 86_400_000L
        every { getAllMedications() } returns flowOf(listOf(fakeMedication(expirationDate = yesterday)))

        val result = buildWorker().doWork()

        assertEquals(ListenableWorker.Result.success(), result)
    }

    private fun fakeMedication(
        id: String = "med-1",
        name: String = "Аспирин",
        kitId: String = "kit-1",
        expirationDate: Long = System.currentTimeMillis() + 86_400_000L
    ) = Medication(
        id = id,
        name = name,
        kitId = kitId,
        expirationDate = expirationDate,
        quantity = 10
    )

    private fun fakeKit(
        id: String = "kit-1",
        name: String = "Аптечка",
        archivedUserIds: List<String> = emptyList()
    ) = Kit(
        id = id,
        name = name,
        archivedUserIds = archivedUserIds,
        userIds = listOf("user-1")
    )
}
