package com.example.general_first_aid_kit.domain.usecase

import com.example.general_first_aid_kit.domain.repository.KitRepository
import javax.inject.Inject

class JoinKitByCodeUseCase @Inject constructor(
    private val repository: KitRepository
) {
    suspend operator fun invoke(userId: String, inviteCode: String) =
        repository.joinKitByCode(userId, inviteCode)
}