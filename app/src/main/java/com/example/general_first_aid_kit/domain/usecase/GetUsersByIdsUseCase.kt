package com.example.general_first_aid_kit.domain.usecase

import com.example.general_first_aid_kit.domain.model.User
import com.example.general_first_aid_kit.domain.repository.UserRepository
import javax.inject.Inject

class GetUsersByIdsUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userIds: List<String>): List<User> {
        return userRepository.getUsersByIds(userIds)
    }
}