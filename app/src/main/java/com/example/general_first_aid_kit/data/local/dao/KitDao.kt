package com.example.general_first_aid_kit.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.general_first_aid_kit.data.local.entity.KitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KitDao {

    @Upsert
    suspend fun upsert(kit: KitEntity)

    @Upsert
    suspend fun upsertAll(kits: List<KitEntity>)

    @Query("SELECT * FROM kits WHERE id = :kitId")
    fun observeById(kitId: String): Flow<KitEntity?>

    @Query("SELECT * FROM kits WHERE id = :kitId")
    suspend fun getById(kitId: String): KitEntity?

    @Query("SELECT * FROM kits WHERE userIds LIKE '%\"' || :userId || '\"%'")
    fun observeByUserId(userId: String): Flow<List<KitEntity>>

    @Query("DELETE FROM kits WHERE id = :kitId")
    suspend fun deleteById(kitId: String): Int

    @Query("DELETE FROM kits")
    suspend fun deleteAll(): Int
}
