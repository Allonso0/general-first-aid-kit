package com.example.general_first_aid_kit.data.local.mapper

import com.example.general_first_aid_kit.data.local.entity.KitEntity
import com.example.general_first_aid_kit.domain.model.Kit
import com.example.general_first_aid_kit.domain.model.KitType

fun KitEntity.toKit(): Kit = Kit(
    id = id,
    name = name,
    location = location,
    colorIndex = colorIndex,
    ownerId = ownerId,
    userIds = userIds,
    type = runCatching { KitType.valueOf(type) }.getOrDefault(KitType.PERSONAL),
    archivedUserIds = archivedUserIds,
    inviteCode = inviteCode
)

fun Kit.toKitEntity(): KitEntity = KitEntity(
    id = id,
    name = name,
    location = location,
    colorIndex = colorIndex,
    ownerId = ownerId,
    userIds = userIds,
    type = type.name,
    archivedUserIds = archivedUserIds,
    inviteCode = inviteCode,
    updatedAt = System.currentTimeMillis()
)
