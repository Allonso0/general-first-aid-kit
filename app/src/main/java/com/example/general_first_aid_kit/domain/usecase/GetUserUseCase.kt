package com.example.general_first_aid_kit.domain.usecase

import com.example.general_first_aid_kit.domain.model.User
import com.example.general_first_aid_kit.domain.repository.UserRepository
import javax.inject.Inject

class GetUserUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(): User? = repository.getCurrentUser()
}