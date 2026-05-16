package com.example.general_first_aid_kit.domain.usecase

import com.example.general_first_aid_kit.domain.repository.AuthRepository
import javax.inject.Inject

class SendEmailVerificationUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> =
        repository.sendEmailVerification()
}
