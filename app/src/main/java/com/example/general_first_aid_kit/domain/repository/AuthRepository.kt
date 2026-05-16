package com.example.general_first_aid_kit.domain.repository

interface AuthRepository {
    fun isUserAuthenticated(): Boolean
    fun isEmailVerified(): Boolean
    suspend fun signIn(email: String, password: String): Result<Unit>
    suspend fun signUp(email: String, password: String, username: String): Result<Unit>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun sendEmailVerification(): Result<Unit>
    suspend fun reloadUser(): Result<Unit>
    fun signOut()
    fun getCurrentUserId(): String?
}