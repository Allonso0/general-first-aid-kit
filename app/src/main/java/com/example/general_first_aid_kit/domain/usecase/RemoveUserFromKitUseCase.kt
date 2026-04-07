package com.example.general_first_aid_kit.domain.usecase

import com.example.general_first_aid_kit.domain.repository.KitRepository
import javax.inject.Inject

class RemoveUserFromKitUseCase @Inject constructor(
    private val repository: KitRepository
) {
    suspend operator fun invoke(kitId: String, userId: String): Result<Unit> {
        return repository.removeUserFromKit(kitId, userId)
    }
}