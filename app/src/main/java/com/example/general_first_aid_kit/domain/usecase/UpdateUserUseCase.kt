package com.example.general_first_aid_kit.domain.usecase

import android.net.Uri
import com.example.general_first_aid_kit.domain.repository.UserRepository
import javax.inject.Inject

class UpdateUserUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(name: String, photoUri: Uri?): Result<Unit> = repository.updateUserProfile(name, photoUri)
}