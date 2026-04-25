package com.example.general_first_aid_kit.data.repository

import com.example.general_first_aid_kit.domain.model.Kit
import com.example.general_first_aid_kit.domain.model.KitType
import com.example.general_first_aid_kit.domain.repository.KitRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class KitRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : KitRepository {

    override suspend fun getKitById(kitId: String): Result<Kit> {
        return try {
            val document = firestore.collection("kits").document(kitId).get().await()
            val kit = document.toObject(Kit::class.java)?.copy(id = document.id)
            if (kit != null) Result.success(kit) else Result.failure(Exception("Аптечка не найдена"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createKit(kit: Kit): Result<Unit> {
        return try {
            val collection = firestore.collection("kits")
            val document = if (kit.id.isEmpty()) collection.document() else collection.document(kit.id)
            val kitToSave = kit.copy(id = document.id)
            document.set(kitToSave).await()
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
    ): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>(
                "name" to name,
                "location" to location,
                "colorIndex" to colorIndex
            )
            type?.let { updates["type"] = it.name }
            userIds?.let { updates["userIds"] = it }

            firestore.collection("kits").document(kitId)
                .update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getKits(userId: String): Flow<List<Kit>> = callbackFlow {
        val subscription = firestore.collection("kits")
            .whereArrayContains("userIds", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val kits = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Kit::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(kits)
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun deleteKit(kitId: String): Result<Unit> = try {
        firestore.collection("kits").document(kitId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun joinKitByCode(userId: String, inviteCode: String): Result<Kit> {
        return try {
            val snapshot = firestore.collection("kits")
                .whereEqualTo("inviteCode", inviteCode)
                .whereEqualTo("type", "SHARED")
                .get()
                .await()

            val document = snapshot.documents.firstOrNull()
                ?: return Result.failure(Exception("Неверный код приглашения"))

            firestore.collection("kits").document(document.id)
                .update("userIds", com.google.firebase.firestore.FieldValue.arrayUnion(userId))
                .await()

            val kit = document.toObject(Kit::class.java)?.copy(id = document.id)
                ?: return Result.failure(Exception("Не удалось прочитать данные аптечки"))

            Result.success(kit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refreshInviteCode(kitId: String): Result<String> {
        return try {
            val newCode = UUID.randomUUID().toString().substring(0, 8).uppercase()
            firestore.collection("kits").document(kitId)
                .update("inviteCode", newCode)
                .await()
            Result.success(newCode)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeUserFromKit(kitId: String, userId: String): Result<Unit> {
        return try {
            firestore.collection("kits").document(kitId)
                .update("userIds", com.google.firebase.firestore.FieldValue.arrayRemove(userId))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setArchived(kitId: String, archived: Boolean): Result<Unit> = try {
        firestore.collection("kits").document(kitId)
            .update("isArchived", archived).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun observeKit(kitId: String): Flow<Kit?> = callbackFlow {
        val subscription = firestore.collection("kits").document(kitId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    trySend(snapshot.toObject(Kit::class.java)?.copy(id = snapshot.id))
                } else {
                    trySend(null)
                }
            }
        awaitClose { subscription.remove() }
    }
}
