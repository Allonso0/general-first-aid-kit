package com.example.general_first_aid_kit.domain.usecase

import com.example.general_first_aid_kit.domain.model.Kit
import com.example.general_first_aid_kit.domain.repository.AuthRepository
import com.example.general_first_aid_kit.domain.repository.KitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class GetKitsUseCase @Inject constructor(
    private val repository: KitRepository,
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<List<Kit>> {
        val userId = authRepository.getCurrentUserId()
        return if (userId != null) {
            repository.getKits(userId)
        } else {
            flowOf(emptyList())
        }
    }
}