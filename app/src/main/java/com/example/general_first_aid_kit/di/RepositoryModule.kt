package com.example.general_first_aid_kit.di

import com.example.general_first_aid_kit.data.repository.AuthRepositoryImpl
import com.example.general_first_aid_kit.data.repository.KitRepositoryImpl
import com.example.general_first_aid_kit.data.repository.UserRepositoryImpl
import com.example.general_first_aid_kit.domain.repository.AuthRepository
import com.example.general_first_aid_kit.domain.repository.KitRepository
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
        kitRepositoryImpl: KitRepositoryImpl
    ): KitRepository
}