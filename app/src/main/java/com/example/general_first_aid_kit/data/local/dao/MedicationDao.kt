package com.example.general_first_aid_kit.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.general_first_aid_kit.data.local.entity.MedicationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {

    @Upsert
    suspend fun upsert(medication: MedicationEntity)

    @Upsert
    suspend fun upsertAll(medications: List<MedicationEntity>)

    @Query("SELECT * FROM medications WHERE kitId = :kitId")
    fun observeByKitId(kitId: String): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications")
    fun observeAll(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE id = :medicationId AND kitId = :kitId")
    fun observeById(kitId: String, medicationId: String): Flow<MedicationEntity?>

    @Query("DELETE FROM medications WHERE id = :medicationId")
    suspend fun deleteById(medicationId: String): Int

    @Query("DELETE FROM medications WHERE kitId = :kitId")
    suspend fun deleteByKitId(kitId: String): Int

    @Query("DELETE FROM medications")
    suspend fun deleteAll(): Int
}
