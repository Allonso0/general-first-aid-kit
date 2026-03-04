package com.example.general_first_aid_kit.domain.repository

import com.example.general_first_aid_kit.domain.model.Kit
import kotlinx.coroutines.flow.Flow

interface KitRepository {
    suspend fun createKit(kit: Kit): Result<Unit>

    suspend fun updateKit(kitId: String, name: String, location: String, colorIndex: Int): Result<Unit>

    fun getKits(userId: String): Flow<List<Kit>>

    suspend fun deleteKit(kitId: String): Result<Unit>
}