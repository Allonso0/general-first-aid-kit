package com.example.general_first_aid_kit.domain.usecase

import com.example.general_first_aid_kit.domain.repository.KitRepository
import javax.inject.Inject

class SetKitArchivedUseCase @Inject constructor(
    private val repository: KitRepository
) {
    suspend operator fun invoke(kitId: String, userId: String, archived: Boolean): Result<Unit> =
        repository.setArchived(kitId, userId, archived)
}
