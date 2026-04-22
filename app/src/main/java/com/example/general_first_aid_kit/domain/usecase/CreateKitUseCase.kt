package com.example.general_first_aid_kit.domain.usecase

import com.example.general_first_aid_kit.domain.model.Kit
import com.example.general_first_aid_kit.domain.model.KitType
import com.example.general_first_aid_kit.domain.repository.KitRepository
import com.example.general_first_aid_kit.domain.repository.UserRepository
import java.util.UUID
import javax.inject.Inject

class CreateKitUseCase @Inject constructor(
    private val kitRepository: KitRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        name: String,
        location: String,
        colorIndex: Int,
        type: KitType
    ): Result<Unit> {
        if (name.isBlank()) return Result.failure(Exception("Название аптечки не может быть пустым"))

        val userId = userRepository.getCurrentUser()?.id
            ?: return Result.failure(Exception("Пользователь не найден"))

        val kit = Kit(
            name = name,
            location = location,
            colorIndex = colorIndex,
            type = type,
            inviteCode = if (type == KitType.SHARED) generateInviteCode() else null,
            ownerId = userId,
            userIds = listOf(userId)
        )
        return kitRepository.createKit(kit)
    }

    private fun generateInviteCode() = UUID.randomUUID().toString().substring(0, 8).uppercase()
}
