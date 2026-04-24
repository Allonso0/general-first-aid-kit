package com.example.general_first_aid_kit

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.general_first_aid_kit.data.worker.ExpiryCheckWorker
import com.example.general_first_aid_kit.presentation.service.NotificationHelper
import com.example.general_first_aid_kit.presentation.service.NotificationPushObserver
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class SharedFirstAidKit : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var notificationPushObserver: NotificationPushObserver

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
        notificationPushObserver.start()
        scheduleExpiryCheck()
    }

    private fun scheduleExpiryCheck() {
        val request = PeriodicWorkRequestBuilder<ExpiryCheckWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "expiry_check",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}