package com.example.general_first_aid_kit.domain.repository

import com.example.general_first_aid_kit.domain.model.Kit
import com.example.general_first_aid_kit.domain.model.KitType
import kotlinx.coroutines.flow.Flow

interface KitRepository {
    suspend fun getKitById(kitId: String): Result<Kit>

    suspend fun createKit(kit: Kit): Result<Unit>

    suspend fun updateKit(
        kitId: String,
        name: String,
        location: String,
        colorIndex: Int,
        type: KitType,
        userIds: List<String>
    ): Result<Unit>

    fun getKits(userId: String): Flow<List<Kit>>

    suspend fun deleteKit(kitId: String): Result<Unit>

    suspend fun joinKitByCode(userId: String, inviteCode: String): Result<Kit>

    suspend fun refreshInviteCode(kitId: String): Result<String>

    suspend fun removeUserFromKit(kitId: String, userId: String): Result<Unit>

    suspend fun setArchived(kitId: String, archived: Boolean): Result<Unit>

    fun observeKit(kitId: String): Flow<Kit?>
}