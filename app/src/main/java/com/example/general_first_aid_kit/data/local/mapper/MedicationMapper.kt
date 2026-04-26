package com.example.general_first_aid_kit.data.local.mapper

import com.example.general_first_aid_kit.data.local.entity.MedicationEntity
import com.example.general_first_aid_kit.domain.model.Medication

fun MedicationEntity.toMedication(): Medication = Medication(
    id = id,
    kitId = kitId,
    name = name,
    expirationDate = expirationDate,
    quantity = quantity,
    unit = unit,
    category = category,
    description = description,
    photoUrl = photoUrl ?: localPhotoUri
)

fun Medication.toMedicationEntity(localPhotoUri: String? = null): MedicationEntity = MedicationEntity(
    id = id,
    kitId = kitId,
    name = name,
    expirationDate = expirationDate,
    quantity = quantity,
    unit = unit,
    category = category,
    description = description,
    photoUrl = photoUrl,
    localPhotoUri = localPhotoUri,
    updatedAt = System.currentTimeMillis()
)
