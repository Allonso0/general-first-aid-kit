package com.example.general_first_aid_kit.data.repository

import com.example.general_first_aid_kit.data.connectivity.ConnectivityMonitor
import com.example.general_first_aid_kit.data.local.dao.KitDao
import com.example.general_first_aid_kit.data.local.dao.SyncOperationDao
import com.example.general_first_aid_kit.data.local.entity.SyncOperationEntity
import com.example.general_first_aid_kit.data.local.mapper.toKit
import com.example.general_first_aid_kit.data.local.mapper.toKitEntity
import com.example.general_first_aid_kit.domain.model.Kit
import com.example.general_first_aid_kit.domain.model.KitType
import com.example.general_first_aid_kit.domain.repository.KitRepository
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Collections
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineFirstKitRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val kitDao: KitDao,
    private val syncOperationDao: SyncOperationDao,
    private val connectivityMonitor: ConnectivityMonitor
) : KitRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val startedSyncForUser = Collections.synchronizedSet(mutableSetOf<String>())
    private val startedSyncForKit = Collections.synchronizedSet(mutableSetOf<String>())

    override fun getKits(userId: String): Flow<List<Kit>> {
        startUserKitsSync(userId)
        return kitDao.observeByUserId(userId).map { entities -> entities.map { it.toKit() } }
    }

    private fun startUserKitsSync(userId: String) {
        if (!startedSyncForUser.add(userId)) return
        scope.launch {
            callbackFlow {
                val reg = firestore.collection("kits")
                    .whereArrayContains("userIds", userId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) { close(error); return@addSnapshotListener }
                        snapshot?.documentChanges?.let { trySend(it) }
                    }
                awaitClose { reg.remove() }
            }
                .catch { startedSyncForUser.remove(userId) }
                .collect { changes ->
                    for (change in changes) {
                        val kit = change.document.toObject(Kit::class.java)
                            ?.copy(id = change.document.id) ?: continue
                        try {
                            when (change.type) {
                                DocumentChange.Type.ADDED,
                                DocumentChange.Type.MODIFIED -> kitDao.upsert(kit.toKitEntity())
                                DocumentChange.Type.REMOVED -> {
                                    kitDao.deleteById(change.document.id)
                                    syncOperationDao.deleteAllForKit(change.document.id)
                                }
                            }
                        } catch (_: Exception) { }
                    }
                }
        }
    }

    override fun observeKit(kitId: String): Flow<Kit?> {
        startKitSync(kitId)
        return kitDao.observeById(kitId).map { it?.toKit() }
    }

    private fun startKitSync(kitId: String) {
        if (!startedSyncForKit.add(kitId)) return
        scope.launch {
            callbackFlow {
                val reg = firestore.collection("kits").document(kitId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) { close(error); return@addSnapshotListener }
                        trySend(snapshot)
                    }
                awaitClose { reg.remove() }
            }
                .catch { startedSyncForKit.remove(kitId) }
                .collect { snapshot ->
                    try {
                        if (snapshot != null && snapshot.exists()) {
                            snapshot.toObject(Kit::class.java)?.copy(id = snapshot.id)
                                ?.let { kitDao.upsert(it.toKitEntity()) }
                        } else if (snapshot != null) {
                            kitDao.deleteById(kitId)
                            syncOperationDao.deleteAllForKit(kitId)
                        }
                    } catch (_: Exception) {}
                }
        }
    }

    override suspend fun getKitById(kitId: String): Result<Kit> = withContext(Dispatchers.IO) {
        kitDao.getById(kitId)?.let { return@withContext Result.success(it.toKit()) }
        try {
            val doc = firestore.collection("kits").document(kitId).get().await()
            val kit = doc.toObject(Kit::class.java)?.copy(id = doc.id)
                ?: return@withContext Result.failure(Exception("Аптечка не найдена"))
            kitDao.upsert(kit.toKitEntity())
            Result.success(kit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createKit(kit: Kit): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val collection = firestore.collection("kits")
            val document = if (kit.id.isEmpty()) collection.document() else collection.document(kit.id)
            val kitToSave = kit.copy(id = document.id)
            document.set(kitToSave).await()
            kitDao.upsert(kitToSave.toKitEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateKit(
        kitId: String,
        name: String,
        location: String,
        colorIndex: Int,
        type: KitType,
        userIds: List<String>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val existing = kitDao.getById(kitId)
            ?: return@withContext Result.failure(Exception("Аптечка не найдена"))
        val updated = existing.copy(
            name = name,
            location = location,
            colorIndex = colorIndex,
            type = type.name,
            userIds = userIds,
            updatedAt = System.currentTimeMillis()
        )
        kitDao.upsert(updated)

        if (connectivityMonitor.isOnline.value) {
            try {
                firestore.collection("kits").document(kitId)
                    .update(
                        "name", name,
                        "location", location,
                        "colorIndex", colorIndex,
                        "type", type.name,
                        "userIds", userIds
                    ).await()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            queueOperation("KIT", "UPDATE", kitId, null, Json.encodeToString(updated), null)
            Result.success(Unit)
        }
    }

    override suspend fun deleteKit(kitId: String): Result<Unit> = withContext(Dispatchers.IO) {
        kitDao.deleteById(kitId)
        if (connectivityMonitor.isOnline.value) {
            try {
                firestore.collection("kits").document(kitId).delete().await()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            queueOperation("KIT", "DELETE", kitId, null, "", null)
            Result.success(Unit)
        }
    }

    override suspend fun setArchived(kitId: String, userId: String, archived: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        val existing = kitDao.getById(kitId)
            ?: return@withContext Result.failure(Exception("Аптечка не найдена"))
        val newArchivedIds = if (archived) {
            (existing.archivedUserIds + userId).distinct()
        } else {
            existing.archivedUserIds.filterNot { it == userId }
        }
        val updated = existing.copy(
            archivedUserIds = newArchivedIds,
            updatedAt = System.currentTimeMillis()
        )
        kitDao.upsert(updated)

        if (connectivityMonitor.isOnline.value) {
            try {
                val fieldValue = if (archived) FieldValue.arrayUnion(userId) else FieldValue.arrayRemove(userId)
                firestore.collection("kits").document(kitId)
                    .update("archivedUserIds", fieldValue).await()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            queueOperation("KIT", "UPDATE", kitId, null, Json.encodeToString(updated), null)
            Result.success(Unit)
        }
    }

    override suspend fun joinKitByCode(userId: String, inviteCode: String): Result<Kit> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("kits")
                .whereEqualTo("inviteCode", inviteCode)
                .whereEqualTo("type", "SHARED")
                .get().await()
            val document = snapshot.documents.firstOrNull()
                ?: return@withContext Result.failure(Exception("Неверный код приглашения"))
            firestore.collection("kits").document(document.id)
                .update("userIds", FieldValue.arrayUnion(userId)).await()
            val kit = document.toObject(Kit::class.java)?.copy(id = document.id)
                ?: return@withContext Result.failure(Exception("Не удалось прочитать данные аптечки"))
            kitDao.upsert(kit.copy(userIds = (kit.userIds + userId).distinct()).toKitEntity())
            Result.success(kit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refreshInviteCode(kitId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val newCode = UUID.randomUUID().toString().substring(0, 8).uppercase()
            firestore.collection("kits").document(kitId).update("inviteCode", newCode).await()
            kitDao.getById(kitId)?.let { kitDao.upsert(it.copy(inviteCode = newCode)) }
            Result.success(newCode)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeUserFromKit(kitId: String, userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore.collection("kits").document(kitId)
                .update("userIds", FieldValue.arrayRemove(userId)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun queueOperation(
        entityType: String,
        operationType: String,
        entityId: String,
        kitId: String?,
        payload: String,
        localPhotoUri: String?
    ) {
        syncOperationDao.insert(
            SyncOperationEntity(
                id = UUID.randomUUID().toString(),
                entityType = entityType,
                operationType = operationType,
                entityId = entityId,
                kitId = kitId,
                payload = payload,
                localPhotoUri = localPhotoUri,
                createdAt = System.currentTimeMillis()
            )
        )
    }
}
