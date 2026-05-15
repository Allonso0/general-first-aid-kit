package com.example.general_first_aid_kit.data.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.example.general_first_aid_kit.data.sync.SyncManager
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SyncWorkerTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val syncManager = mockk<SyncManager>()

    private fun buildWorker() = TestListenableWorkerBuilder<SyncWorker>(context)
        .setWorkerFactory(object : WorkerFactory() {
            override fun createWorker(
                appContext: Context,
                workerClassName: String,
                workerParameters: WorkerParameters
            ): ListenableWorker = SyncWorker(appContext, workerParameters, syncManager)
        })
        .build()

    @Test
    fun doWork_callsProcessQueue() = runTest {
        coJustRun { syncManager.processQueue() }

        buildWorker().doWork()

        coVerify(exactly = 1) { syncManager.processQueue() }
    }

    @Test
    fun doWork_returnsSuccess_when_processQueueCompletes() = runTest {
        coJustRun { syncManager.processQueue() }

        val result = buildWorker().doWork()

        assertEquals(androidx.work.ListenableWorker.Result.success(), result)
    }

    @Test
    fun doWork_returnsRetry_when_processQueueThrows() = runTest {
        coEvery { syncManager.processQueue() } throws RuntimeException("Network error")

        val result = buildWorker().doWork()

        assertEquals(androidx.work.ListenableWorker.Result.retry(), result)
    }
}
