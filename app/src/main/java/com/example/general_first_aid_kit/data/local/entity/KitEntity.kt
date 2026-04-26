package com.example.general_first_aid_kit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "kits")
data class KitEntity(
    @PrimaryKey val id: String,
    val name: String,
    val location: String,
    val colorIndex: Int,
    val ownerId: String,
    val userIds: List<String>,
    val type: String,
    val archivedUserIds: List<String>,
    val inviteCode: String?,
    val updatedAt: Long = System.currentTimeMillis()
)
