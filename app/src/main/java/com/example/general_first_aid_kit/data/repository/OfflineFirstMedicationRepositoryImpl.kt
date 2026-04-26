package com.example.general_first_aid_kit.data.repository

import android.content.Context
import androidx.core.net.toUri
import com.example.general_first_aid_kit.data.connectivity.ConnectivityMonitor
import com.example.general_first_aid_kit.data.local.dao.MedicationDao
import com.example.general_first_aid_kit.data.local.dao.SyncOperationDao
import com.example.general_first_aid_kit.data.local.entity.SyncOperationEntity
import com.example.general_first_aid_kit.data.local.mapper.toMedication
import com.example.general_first_aid_kit.data.local.mapper.toMedicationEntity
import com.example.general_first_aid_kit.domain.model.Medication
import com.example.general_first_aid_kit.domain.repository.MedicationRepository
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.storage.Storage
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
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineFirstMedicationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val supabaseStorage: Storage,
    private val medicationDao: MedicationDao,
    private val syncOperationDao: SyncOperationDao,
    private val connectivityMonitor: ConnectivityMonitor,
    @ApplicationContext private val context: Context
) : MedicationRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val allMedicationsListenerStarted = AtomicBoolean(false)
    private val bucketName = "medications"

    // ── Firebase → Room sync ─────────────────────────────────────────────────

    // One collection-group listener covers all kits — no per-kit listeners needed.
    private fun ensureAllMedicationsSyncing() {
        if (!allMedicationsListenerStarted.compareAndSet(false, true)) return
        scope.launch {
            callbackFlow {
                val reg = firestore.collectionGroup("medications")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) { close(error); return@addSnapshotListener }
                        snapshot?.documentChanges?.let { trySend(it) }
                    }
                awaitClose { reg.remove() }
            }
                .catch { allMedicationsListenerStarted.set(false) }
                .collect { changes ->
                    for (change in changes) {
                        val kitId = change.document.reference.parent.parent?.id ?: continue
                        val med = change.document.toObject(Medication::class.java)
                            ?.copy(id = change.document.id, kitId = kitId) ?: continue
                        try {
                            when (change.type) {
                                DocumentChange.Type.ADDED,
                                DocumentChange.Type.MODIFIED -> medicationDao.upsert(med.toMedicationEntity())
                                DocumentChange.Type.REMOVED -> medicationDao.deleteById(med.id)
                            }
                        } catch (_: Exception) {}
                    }
                }
        }
    }

    // ── Reads ────────────────────────────────────────────────────────────────

    override fun getMedications(kitId: String): Flow<List<Medication>> {
        ensureAllMedicationsSyncing()
        return medicationDao.observeByKitId(kitId).map { entities -> entities.map { it.toMedication() } }
    }

    override fun getAllMedications(): Flow<List<Medication>> {
        ensureAllMedicationsSyncing()
        return medicationDao.observeAll().map { entities -> entities.map { it.toMedication() } }
    }

    override fun getMedication(kitId: String, medicationId: String): Flow<Medication?> {
        ensureAllMedicationsSyncing()
        return medicationDao.observeById(kitId, medicationId).map { it?.toMedication() }
    }

    // ── Writes ───────────────────────────────────────────────────────────────

    override suspend fun saveMedication(
        kitId: String,
        medication: Medication,
        localPhotoUri: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (connectivityMonitor.isOnline.value) {
            saveMedicationOnline(kitId, medication, localPhotoUri)
        } else {
            saveMedicationOffline(kitId, medication, localPhotoUri)
        }
    }

    private suspend fun saveMedicationOnline(
        kitId: String,
        medication: Medication,
        localPhotoUri: String?
    ): Result<Unit> {
        return try {
            val collection = firestore.collection("kits").document(kitId).collection("medications")
            val docRef = if (medication.id.isEmpty()) collection.document() else collection.document(medication.id)

            var finalPhotoUrl = medication.photoUrl
            if (localPhotoUri != null) {
                val fileName = "$kitId/${docRef.id}_${UUID.randomUUID()}.jpg"
                val bucket = supabaseStorage.from(bucketName)
                val bytes = context.contentResolver.openInputStream(localPhotoUri.toUri())?.use { it.readBytes() }
                    ?: throw Exception("Не удалось прочитать изображение")
                bucket.upload(path = fileName, data = bytes) { upsert = true }
                finalPhotoUrl = bucket.publicUrl(fileName)
            }

            val medicationToSave = medication.copy(id = docRef.id, photoUrl = finalPhotoUrl)
            docRef.set(medicationToSave).await()
            // Upsert to Room immediately for responsive UI; Firestore listener will confirm later
            medicationDao.upsert(medicationToSave.toMedicationEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun saveMedicationOffline(
        kitId: String,
        medication: Medication,
        localPhotoUri: String?
    ): Result<Unit> {
        val id = if (medication.id.isEmpty()) UUID.randomUUID().toString() else medication.id
        val medicationWithId = medication.copy(id = id, kitId = kitId)
        medicationDao.upsert(medicationWithId.toMedicationEntity(localPhotoUri = localPhotoUri))

        val operationType = if (medication.id.isEmpty()) "CREATE" else "UPDATE"
        syncOperationDao.insert(
            SyncOperationEntity(
                id = UUID.randomUUID().toString(),
                entityType = "MEDICATION",
                operationType = operationType,
                entityId = id,
                kitId = kitId,
                payload = Json.encodeToString(medicationWithId.toMedicationEntity(localPhotoUri = localPhotoUri)),
                localPhotoUri = localPhotoUri,
                createdAt = System.currentTimeMillis()
            )
        )
        return Result.success(Unit)
    }

    override suspend fun deleteMedication(
        kitId: String,
        medication: Medication
    ): Result<Unit> = withContext(Dispatchers.IO) {
        medicationDao.deleteById(medication.id)

        if (connectivityMonitor.isOnline.value) {
            try {
                firestore.collection("kits")
                    .document(kitId)
                    .collection("medications")
                    .document(medication.id)
                    .delete().await()

                medication.photoUrl?.let { url ->
                    try {
                        val marker = "$bucketName/"
                        val start = url.indexOf(marker)
                        if (start != -1) supabaseStorage.from(bucketName).delete(url.substring(start + marker.length))
                    } catch (_: Exception) {}
                }
                Result.success(Unit)
            } catch (e: Exception) {
                // Firestore delete failed — restore to Room and let caller handle the error
                medicationDao.upsert(medication.toMedicationEntity())
                Result.failure(e)
            }
        } else {
            syncOperationDao.insert(
                SyncOperationEntity(
                    id = UUID.randomUUID().toString(),
                    entityType = "MEDICATION",
                    operationType = "DELETE",
                    entityId = medication.id,
                    kitId = kitId,
                    payload = Json.encodeToString(medication.toMedicationEntity()),
                    localPhotoUri = null,
                    createdAt = System.currentTimeMillis()
                )
            )
            Result.success(Unit)
        }
    }
}
