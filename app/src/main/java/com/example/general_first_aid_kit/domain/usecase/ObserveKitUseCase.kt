package com.example.general_first_aid_kit.domain.usecase

import com.example.general_first_aid_kit.domain.repository.KitRepository
import javax.inject.Inject

class ObserveKitUseCase @Inject constructor(
    private val repository: KitRepository
) {
    operator fun invoke(kitId: String) = repository.observeKit(kitId)
}