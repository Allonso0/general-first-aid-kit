package com.example.general_first_aid_kit.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.general_first_aid_kit.data.local.entity.SyncOperationEntity

@Dao
interface SyncOperationDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(operation: SyncOperationEntity)

    @Query("SELECT * FROM sync_operations ORDER BY createdAt ASC")
    suspend fun getAll(): List<SyncOperationEntity>

    @Query("UPDATE sync_operations SET retryCount = :retryCount WHERE id = :id")
    suspend fun updateRetryCount(id: String, retryCount: Int): Int

    @Query("DELETE FROM sync_operations WHERE id = :id")
    suspend fun deleteById(id: String): Int

    @Query("DELETE FROM sync_operations WHERE entityId = :kitId OR kitId = :kitId")
    suspend fun deleteAllForKit(kitId: String): Int

    @Query("DELETE FROM sync_operations")
    suspend fun deleteAll(): Int
}
