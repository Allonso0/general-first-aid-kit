package com.example.general_first_aid_kit.domain.repository

import android.net.Uri
import com.example.general_first_aid_kit.domain.model.User

interface UserRepository {
    fun getCurrentUser(): User?

    suspend fun updateUserProfile(name: String, photoUri: Uri?): Result<Unit>

    suspend fun getUsersByIds(userIds: List<String>): List<User>
}