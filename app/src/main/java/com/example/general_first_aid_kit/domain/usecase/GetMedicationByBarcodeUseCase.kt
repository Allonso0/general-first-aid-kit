package com.example.general_first_aid_kit.domain.usecase

import com.example.general_first_aid_kit.domain.model.MedicationSuggestion
import com.example.general_first_aid_kit.domain.repository.AiMedicationRepository
import javax.inject.Inject

class GetMedicationByBarcodeUseCase @Inject constructor(
    private val repository: AiMedicationRepository
) {
    suspend operator fun invoke(barcode: String): Result<MedicationSuggestion> =
        repository.getMedicationByBarcode(barcode)
}
