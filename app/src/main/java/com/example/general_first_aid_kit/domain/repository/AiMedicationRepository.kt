package com.example.general_first_aid_kit.domain.repository

import com.example.general_first_aid_kit.domain.model.MedicationSuggestion

interface AiMedicationRepository {
    suspend fun getMedicationByBarcode(barcode: String): Result<MedicationSuggestion>
}
