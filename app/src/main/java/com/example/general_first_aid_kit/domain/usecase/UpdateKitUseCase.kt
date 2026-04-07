package com.example.general_first_aid_kit.domain.usecase

import com.example.general_first_aid_kit.domain.repository.KitRepository
import javax.inject.Inject

class UpdateKitUseCase @Inject constructor(
    private val repository: KitRepository
) {
    suspend operator fun invoke(kitId: String, name: String, location: String, colorIndex: Int): Result<Unit> {
        return repository.updateKit(kitId, name, location, colorIndex)
    }
}