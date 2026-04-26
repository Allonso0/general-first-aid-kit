package com.example.general_first_aid_kit.di

import android.content.Context
import androidx.room.Room
import com.example.general_first_aid_kit.data.local.AppDatabase
import com.example.general_first_aid_kit.data.local.dao.KitDao
import com.example.general_first_aid_kit.data.local.dao.MedicationDao
import com.example.general_first_aid_kit.data.local.dao.SyncOperationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "firstaidkit.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideKitDao(db: AppDatabase): KitDao = db.kitDao()

    @Provides
    fun provideMedicationDao(db: AppDatabase): MedicationDao = db.medicationDao()

    @Provides
    fun provideSyncOperationDao(db: AppDatabase): SyncOperationDao = db.syncOperationDao()
}
