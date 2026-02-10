package com.example.general_first_aid_kit.domain.usecase

import com.example.general_first_aid_kit.domain.model.Kit
import com.example.general_first_aid_kit.domain.repository.KitRepository
import javax.inject.Inject

class CreateKitUseCase @Inject constructor(
    private val repository: KitRepository
) {
    suspend operator fun invoke(kit: Kit): Result<Unit> {
        // TODO: добавить валидацию аптечки при ее создании
        if (kit.name.isBlank()) {
            return Result.failure(Exception("Название аптечки не может быть пустым"))
        }
        return repository.createKit(kit)
    }
}