package com.example.general_first_aid_kit.domain.usecase

import com.example.general_first_aid_kit.domain.repository.KitRepository
import javax.inject.Inject

class GetKitUseCase @Inject constructor(
    private val repository: KitRepository
) {
    suspend operator fun invoke(kitId: String) = repository.getKitById(kitId)
}
