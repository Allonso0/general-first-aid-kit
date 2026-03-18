package com.example.general_first_aid_kit.domain.repository

import com.example.general_first_aid_kit.domain.model.Medication
import kotlinx.coroutines.flow.Flow

interface MedicationRepository {

    fun getMedications(kitId: String): Flow<List<Medication>>

    fun getAllMedications(): Flow<List<Medication>>

    suspend fun saveMedication(
        kitId: String,
        medication: Medication,
        localPhotoUri: String?
    ): Result<Unit>

    suspend fun deleteMedication(kitId: String, medication: Medication): Result<Unit>
}