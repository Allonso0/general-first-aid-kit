package com.example.general_first_aid_kit

import android.app.Application
import com.example.general_first_aid_kit.presentation.service.NotificationHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SharedFirstAidKit : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
    }
}