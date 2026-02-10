package com.example.general_first_aid_kit.domain.repository

import com.example.general_first_aid_kit.domain.model.Kit
import kotlinx.coroutines.flow.Flow

interface KitRepository {
    suspend fun createKit(kit: Kit): Result<Unit>

    fun getKits(userId: String): Flow<List<Kit>>
}