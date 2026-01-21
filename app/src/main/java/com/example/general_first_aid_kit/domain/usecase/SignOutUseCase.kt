package com.example.general_first_aid_kit.domain.usecase

import com.example.general_first_aid_kit.domain.repository.AuthRepository
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke() = repository.signOut()
}