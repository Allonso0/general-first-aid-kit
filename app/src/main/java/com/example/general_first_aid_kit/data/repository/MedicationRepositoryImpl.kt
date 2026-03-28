package com.example.general_first_aid_kit.data.repository

import android.content.Context
import com.example.general_first_aid_kit.domain.model.Medication
import com.example.general_first_aid_kit.domain.repository.MedicationRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import androidx.core.net.toUri
import kotlinx.coroutines.tasks.await

class MedicationRepositoryImpl @Inject constructor(
    private val fireStore: FirebaseFirestore,
    private val supabaseStorage: Storage,
    @ApplicationContext private val context: Context
) : MedicationRepository {

    private val bucketName = "medications"

    override fun getMedications(kitId: String): Flow<List<Medication>> = callbackFlow {
        val subscription = fireStore.collection("kits")
            .document(kitId)
            .collection("medications")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val medications = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Medication::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(medications)
            }
        awaitClose { subscription.remove() }
    }

    override fun getAllMedications(): Flow<List<Medication>> = callbackFlow {
        val subscription = fireStore.collectionGroup("medications")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val medications = snapshot?.documents?.mapNotNull { doc ->
                    val kitId = doc.reference.parent.parent?.id ?: ""
                    doc.toObject(Medication::class.java)?.copy(
                        id = doc.id,
                        kitId = kitId
                    )
                } ?: emptyList()
                trySend(medications)
            }

        awaitClose { subscription.remove() }
    }

    override suspend fun saveMedication(
        kitId: String,
        medication: Medication,
        localPhotoUri: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val collection = fireStore.collection("kits").document(kitId).collection("medications")
            val docRef = if (medication.id.isEmpty()) collection.document() else collection.document(medication.id)

            var finalPhotoUrl = medication.photoUrl

            if (localPhotoUri != null) {
                val uri = localPhotoUri.toUri()
                val fileName = "$kitId/${docRef.id}_${UUID.randomUUID()}.jpg"
                val bucket = supabaseStorage.from(bucketName)

                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: throw Exception("Не удалось прочитать изображение")

                bucket.upload(path = fileName, data = bytes) { upsert = true }
                finalPhotoUrl = bucket.publicUrl(fileName)
            }
            val medicationToSave = medication.copy(
                id = docRef.id,
                photoUrl = finalPhotoUrl
            )

            docRef.set(medicationToSave).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteMedication(
        kitId: String,
        medication: Medication
    ): Result<Unit>  = withContext(Dispatchers.IO) {
        try {
            fireStore.collection("kits")
                .document(kitId)
                .collection("medications")
                .document(medication.id)
                .delete()
                .await()

            medication.photoUrl?.let { url ->
                try {
                    val bucketMarker = "$bucketName/"
                    val startIndex = url.indexOf(bucketMarker)
                    if (startIndex != -1) {
                        val filePath = url.substring(startIndex + bucketMarker.length)
                        supabaseStorage.from(bucketName).delete(filePath)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}