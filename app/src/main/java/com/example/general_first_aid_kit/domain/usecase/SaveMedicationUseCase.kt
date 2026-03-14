package com.example.general_first_aid_kit.domain.usecase

import com.example.general_first_aid_kit.domain.model.Medication
import com.example.general_first_aid_kit.domain.repository.MedicationRepository
import javax.inject.Inject

class SaveMedicationUseCase @Inject constructor(
    private val repository: MedicationRepository
) {

    suspend operator fun invoke(
        kitId: String,
        medication: Medication,
        localPhotoUri: String?
    ): Result<Unit> {
        // TODO: настроить валидацию
        if (medication.name.isBlank()) {
            return Result.failure(IllegalArgumentException("Название лекарства не может быть пустым"))
        }

        return repository.saveMedication(kitId, medication, localPhotoUri)
    }
}