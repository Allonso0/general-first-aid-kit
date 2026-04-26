package com.example.general_first_aid_kit.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medications",
    foreignKeys = [
        ForeignKey(
            entity = KitEntity::class,
            parentColumns = ["id"],
            childColumns = ["kitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("kitId")]
)
data class MedicationEntity(
    @PrimaryKey val id: String,
    val kitId: String,
    val name: String,
    val expirationDate: Long,
    val quantity: Int,
    val unit: String,
    val category: String,
    val description: String,
    val photoUrl: String?,
    val localPhotoUri: String?,
    val updatedAt: Long = System.currentTimeMillis()
)
