package com.example.general_first_aid_kit.di

import com.example.general_first_aid_kit.data.repository.AuthRepositoryImpl
import com.example.general_first_aid_kit.data.repository.UserRepositoryImpl
import com.example.general_first_aid_kit.domain.repository.AuthRepository
import com.example.general_first_aid_kit.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideAuthRepository(auth: FirebaseAuth): AuthRepository {
        return AuthRepositoryImpl(auth)
    }

    @Provides
    @Singleton
    fun provideUserRepository(auth: FirebaseAuth): UserRepository {
        return UserRepositoryImpl(auth)
    }
}