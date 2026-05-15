package com.example.general_first_aid_kit.data.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.example.general_first_aid_kit.domain.model.AppNotification
import com.example.general_first_aid_kit.domain.model.AppSettings
import com.example.general_first_aid_kit.domain.model.Kit
import com.example.general_first_aid_kit.domain.model.KitNotificationSettings
import com.example.general_first_aid_kit.domain.model.NotificationType
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
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LowStockCheckWorkerTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    private val auth = mockk<FirebaseAuth>()
    private val saveNotification = mockk<SaveNotificationUseCase>()
    private val getSettings = mockk<GetKitNotificationSettingsUseCase>()
    private val getKit = mockk<GetKitUseCase>()
    private val getAppSettings = mockk<GetAppSettingsUseCase>()

    private fun buildWorker(
        kitId: String? = "kit-1",
        medicationId: String? = "med-1",
        medicationName: String? = "Аспирин",
        quantity: Int = 3
    ) = TestListenableWorkerBuilder<LowStockCheckWorker>(context)
        .setWorkerFactory(object : WorkerFactory() {
            override fun createWorker(
                appContext: Context,
                workerClassName: String,
                workerParameters: WorkerParameters
            ): ListenableWorker = LowStockCheckWorker(
                appContext, workerParameters,
                auth, saveNotification, getSettings, getKit, getAppSettings
            )
        })
        .setInputData(
            workDataOf(
                LowStockCheckWorker.KEY_KIT_ID to kitId,
                LowStockCheckWorker.KEY_MEDICATION_ID to medicationId,
                LowStockCheckWorker.KEY_MEDICATION_NAME to medicationName,
                LowStockCheckWorker.KEY_QUANTITY to quantity
            )
        )
        .build()

    @Before
    fun setUp() {
        val user = mockk<FirebaseUser>()
        every { user.uid } returns "user-1"
        every { auth.currentUser } returns user

        every { getAppSettings() } returns AppSettings(lowStockThreshold = 5, expiryWarningDays = 7)
        coJustRun { saveNotification(any(), any()) }
        coEvery { getKit(any()) } returns Result.success(fakeKit())
        coEvery { getSettings(any(), any()) } returns KitNotificationSettings(notifyLowStock = false)
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
    fun doWork_returnsFailure_when_kitIdMissing() = runTest {
        val result = buildWorker(kitId = null).doWork()

        assertEquals(ListenableWorker.Result.failure(), result)
    }

    @Test
    fun doWork_returnsFailure_when_medicationIdMissing() = runTest {
        val result = buildWorker(medicationId = null).doWork()

        assertEquals(ListenableWorker.Result.failure(), result)
    }

    @Test
    fun doWork_returnsFailure_when_medicationNameMissing() = runTest {
        val result = buildWorker(medicationName = null).doWork()

        assertEquals(ListenableWorker.Result.failure(), result)
    }

    @Test
    fun doWork_returnsSuccess_when_quantityAboveThreshold() = runTest {
        every { getAppSettings() } returns AppSettings(lowStockThreshold = 5)

        val result = buildWorker(quantity = 6).doWork()

        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun doWork_doesNotSaveNotification_when_quantityAboveThreshold() = runTest {
        every { getAppSettings() } returns AppSettings(lowStockThreshold = 5)

        buildWorker(quantity = 6).doWork()

        coVerify(exactly = 0) { saveNotification(any(), any()) }
    }

    @Test
    fun doWork_savesNotification_when_quantityBelowThreshold() = runTest {
        every { getAppSettings() } returns AppSettings(lowStockThreshold = 5)

        buildWorker(quantity = 3).doWork()

        coVerify(exactly = 1) {
            saveNotification("user-1", match { it.type == NotificationType.LOW_STOCK })
        }
    }

    @Test
    fun doWork_savesNotification_when_quantityEqualsThreshold() = runTest {
        every { getAppSettings() } returns AppSettings(lowStockThreshold = 5)

        buildWorker(quantity = 5).doWork()

        coVerify(exactly = 1) { saveNotification(any(), any()) }
    }

    @Test
    fun doWork_doesNotSaveNotification_when_userIsArchived() = runTest {
        coEvery { getKit(any()) } returns Result.success(fakeKit(archivedUserIds = listOf("user-1")))

        buildWorker(quantity = 3).doWork()

        coVerify(exactly = 0) { saveNotification(any(), any()) }
    }

    @Test
    fun doWork_includesMedicationNameInNotificationMessage() = runTest {
        val slot = slot<AppNotification>()
        coEvery { saveNotification(any(), capture(slot)) } just Runs

        buildWorker(medicationName = "Ибупрофен", quantity = 2).doWork()

        assertTrue(slot.captured.message.contains("Ибупрофен"))
    }

    @Test
    fun doWork_includesKitNameInNotificationMessage() = runTest {
        coEvery { getKit(any()) } returns Result.success(fakeKit(name = "Семейная"))
        val slot = slot<AppNotification>()
        coEvery { saveNotification(any(), capture(slot)) } just Runs

        buildWorker(quantity = 2).doWork()

        assertTrue(slot.captured.message.contains("Семейная"))
    }

    @Test
    fun doWork_returnsSuccess_on_happyPath() = runTest {
        val result = buildWorker(quantity = 3).doWork()

        assertEquals(ListenableWorker.Result.success(), result)
    }

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
