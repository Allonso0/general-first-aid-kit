package com.example.general_first_aid_kit.domain.usecase

import com.example.general_first_aid_kit.domain.repository.AuthRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String, username: String): Result<Unit> =
        repository.signUp(email, password, username)
}