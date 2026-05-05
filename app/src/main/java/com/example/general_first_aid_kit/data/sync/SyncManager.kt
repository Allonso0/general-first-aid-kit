package com.example.general_first_aid_kit.data.sync

import android.content.Context
import androidx.core.net.toUri
import com.example.general_first_aid_kit.data.connectivity.ConnectivityMonitor
import com.example.general_first_aid_kit.data.local.dao.MedicationDao
import com.example.general_first_aid_kit.data.local.dao.SyncOperationDao
import com.example.general_first_aid_kit.data.local.entity.KitEntity
import com.example.general_first_aid_kit.data.local.entity.MedicationEntity
import com.example.general_first_aid_kit.data.local.entity.SyncOperationEntity
import com.example.general_first_aid_kit.domain.model.Medication
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    private val syncOperationDao: SyncOperationDao,
    private val medicationDao: MedicationDao,
    private val firestore: FirebaseFirestore,
    private val supabaseStorage: Storage,
    private val connectivityMonitor: ConnectivityMonitor,
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val isProcessing = AtomicBoolean(false)

    fun start() {
        scope.launch {
            connectivityMonitor.isOnline
                .filter { it }
                .collect { processQueue() }
        }
    }

    suspend fun processQueue() {
        if (!isProcessing.compareAndSet(false, true)) return
        try {
            val operations = syncOperationDao.getAll()
            for (op in operations) {
                try {
                    when (op.entityType) {
                        "KIT" -> processKitOperation(op)
                        "MEDICATION" -> processMedicationOperation(op)
                    }
                    syncOperationDao.deleteById(op.id)
                } catch (_: Exception) {
                    val newCount = op.retryCount + 1
                    if (newCount >= MAX_RETRIES) {
                        syncOperationDao.deleteById(op.id)
                    } else {
                        syncOperationDao.updateRetryCount(op.id, newCount)
                    }
                }
            }
        } finally {
            isProcessing.set(false)
        }
    }

    private suspend fun processKitOperation(op: SyncOperationEntity) {
        when (op.operationType) {
            "UPDATE" -> {
                val entity = Json.decodeFromString<KitEntity>(op.payload)
                firestore.collection("kits").document(op.entityId)
                    .update(
                        mapOf(
                            "name" to entity.name,
                            "location" to entity.location,
                            "colorIndex" to entity.colorIndex,
                            "archivedUserIds" to entity.archivedUserIds
                        )
                    ).await()
            }
            "DELETE" -> {
                firestore.collection("kits").document(op.entityId).delete().await()
            }
        }
    }

    private suspend fun processMedicationOperation(op: SyncOperationEntity) {
        val kitId = op.kitId ?: return
        when (op.operationType) {
            "CREATE", "UPDATE" -> {
                val entity = Json.decodeFromString<MedicationEntity>(op.payload)
                var finalPhotoUrl = entity.photoUrl

                if (op.localPhotoUri != null) {
                    val fileName = "$kitId/${entity.id}_${UUID.randomUUID()}.jpg"
                    val bucket = supabaseStorage.from(BUCKET_NAME)
                    val bytes = context.contentResolver
                        .openInputStream(op.localPhotoUri.toUri())
                        ?.use { it.readBytes() }
                        ?: throw Exception("Не удалось прочитать изображение")
                    bucket.upload(path = fileName, data = bytes) { upsert = true }
                    finalPhotoUrl = bucket.publicUrl(fileName)
                }

                val medication = Medication(
                    id = entity.id,
                    kitId = kitId,
                    name = entity.name,
                    expirationDate = entity.expirationDate,
                    quantity = entity.quantity,
                    unit = entity.unit,
                    category = entity.category,
                    description = entity.description,
                    photoUrl = finalPhotoUrl
                )
                firestore.collection("kits").document(kitId)
                    .collection("medications").document(entity.id)
                    .set(medication).await()

                // Clear localPhotoUri and persist the final Supabase URL
                medicationDao.upsert(entity.copy(photoUrl = finalPhotoUrl, localPhotoUri = null))
            }
            "DELETE" -> {
                firestore.collection("kits").document(kitId)
                    .collection("medications").document(op.entityId)
                    .delete().await()

                // Delete Supabase photo if present
                val entity = Json.decodeFromString<MedicationEntity>(op.payload)
                entity.photoUrl?.let { url ->
                    try {
                        val marker = "$BUCKET_NAME/"
                        val start = url.indexOf(marker)
                        if (start != -1) supabaseStorage.from(BUCKET_NAME).delete(url.substring(start + marker.length))
                    } catch (_: Exception) {}
                }
            }
        }
    }

    private companion object {
        const val MAX_RETRIES = 5
        const val BUCKET_NAME = "medications"
    }
}
