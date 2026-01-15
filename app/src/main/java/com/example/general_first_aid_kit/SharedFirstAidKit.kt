package com.example.general_first_aid_kit

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SharedFirstAidKit : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}