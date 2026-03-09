package com.example.general_first_aid_kit.data.repository

import com.example.general_first_aid_kit.domain.model.Kit
import com.example.general_first_aid_kit.domain.repository.KitRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class KitRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : KitRepository {

    override suspend fun createKit(kit: Kit): Result<Unit> {
        return try {
            val collection = firestore.collection("kits")

            val document = if (kit.id.isEmpty()) {
                collection.document()
            } else {
                collection.document(kit.id)
            }

            val kitToSave = kit.copy(id = document.id)
            document.set(kitToSave).await()

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun updateKit(
        kitId: String,
        name: String,
        location: String,
        colorIndex: Int
    ): Result<Unit> {
        return try {
            firestore.collection("kits").document(kitId)
                .update(
                    "name", name,
                    "location", location,
                    "colorIndex", colorIndex
                ).await()
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
}