package com.example.general_first_aid_kit.di

import com.example.general_first_aid_kit.data.repository.AppSettingsRepositoryImpl
import com.example.general_first_aid_kit.data.repository.AuthRepositoryImpl
import com.example.general_first_aid_kit.data.repository.KitNotificationSettingsRepositoryImpl
import com.example.general_first_aid_kit.data.repository.OfflineFirstKitRepositoryImpl
import com.example.general_first_aid_kit.data.repository.OfflineFirstMedicationRepositoryImpl
import com.example.general_first_aid_kit.data.repository.NotificationRepositoryImpl
import com.example.general_first_aid_kit.data.repository.UserRepositoryImpl
import com.example.general_first_aid_kit.domain.repository.AppSettingsRepository
import com.example.general_first_aid_kit.domain.repository.AuthRepository
import com.example.general_first_aid_kit.domain.repository.KitNotificationSettingsRepository
import com.example.general_first_aid_kit.domain.repository.KitRepository
import com.example.general_first_aid_kit.domain.repository.MedicationRepository
import com.example.general_first_aid_kit.domain.repository.NotificationRepository
import com.example.general_first_aid_kit.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAppSettingsRepository(
        appSettingsRepositoryImpl: AppSettingsRepositoryImpl
    ): AppSettingsRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ) : AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindKitRepository(
        impl: OfflineFirstKitRepositoryImpl
    ): KitRepository

    @Binds
    @Singleton
    abstract fun bindMedicationRepository(
        impl: OfflineFirstMedicationRepositoryImpl
    ): MedicationRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        notificationRepositoryImpl: NotificationRepositoryImpl
    ): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindKitNotificationSettingsRepository(
        kitNotificationSettingsRepositoryImpl: KitNotificationSettingsRepositoryImpl
    ): KitNotificationSettingsRepository
}