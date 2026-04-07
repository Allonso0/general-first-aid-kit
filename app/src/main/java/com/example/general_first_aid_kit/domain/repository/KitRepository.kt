package com.example.general_first_aid_kit.domain.repository

import com.example.general_first_aid_kit.domain.model.Kit
import kotlinx.coroutines.flow.Flow

interface KitRepository {
    suspend fun getKitById(kitId: String): Result<Kit>

    suspend fun createKit(kit: Kit): Result<Unit>

    suspend fun updateKit(kitId: String, name: String, location: String, colorIndex: Int): Result<Unit>

    fun getKits(userId: String): Flow<List<Kit>>

    suspend fun deleteKit(kitId: String): Result<Unit>

    suspend fun joinKitByCode(userId: String, inviteCode: String): Result<Unit>

    suspend fun refreshInviteCode(kitId: String): Result<String>

    suspend fun removeUserFromKit(kitId: String, userId: String): Result<Unit>

    fun observeKit(kitId: String): Flow<Kit?>
}