package com.example.general_first_aid_kit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_operations")
data class SyncOperationEntity(
    @PrimaryKey val id: String,
    val entityType: String,
    val operationType: String,
    val entityId: String,
    val kitId: String?,
    val payload: String,
    val localPhotoUri: String?,
    val createdAt: Long,
    val retryCount: Int = 0
)
