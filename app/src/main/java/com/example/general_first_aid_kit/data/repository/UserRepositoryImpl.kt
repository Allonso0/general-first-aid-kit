package com.example.general_first_aid_kit.data.repository

import android.content.Context
import android.net.Uri
import com.example.general_first_aid_kit.domain.model.User
import com.example.general_first_aid_kit.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import androidx.core.net.toUri

class UserRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val supabaseStorage: Storage,
    @ApplicationContext private val context: Context
) : UserRepository {

    override fun getCurrentUser(): User? {
        val firebaseUser = firebaseAuth.currentUser ?: return null
        return User(
            id = firebaseUser.uid,
            name = firebaseUser.displayName ?: "Пользователь",
            email = firebaseUser.email ?: "",
            avatarURL = firebaseUser.photoUrl?.toString()
        )
    }

    override suspend fun updateUserProfile(name: String, photoUri: Uri?): Result<Unit> = withContext(
        Dispatchers.IO) {
        return@withContext try {
            val user = firebaseAuth.currentUser
                ?: return@withContext Result.failure(Exception("Пользователь не найден"))

            var finalPhotoUri: Uri? = null

            if (photoUri != null) {
                val fileName = "${user.uid}.jpg"
                val bucket = supabaseStorage.from("avatars")

                val bytes = context.contentResolver.openInputStream(photoUri)?.use { it.readBytes() }
                    ?: throw Exception("Не удалось прочитать изображение")

                bucket.upload(path = fileName, data = bytes) {
                    upsert = true
                }

                val urlString = bucket.publicUrl(fileName)
                finalPhotoUri = urlString.toUri()
            }

            val profileUpdates = userProfileChangeRequest {
                displayName = name

                if (finalPhotoUri != null) {
                    this.photoUri = finalPhotoUri
                }
            }

            user.updateProfile(profileUpdates).await()

            firestore.collection("users").document(user.uid).set(
                mapOf(
                    "name" to name,
                    "email" to (user.email ?: ""),
                    "avatarURL" to (finalPhotoUri?.toString() ?: "")
                ),
                com.google.firebase.firestore.SetOptions.merge()
            ).await()

            user.reload().await()

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getUsersByIds(userIds: List<String>): List<User> = withContext(Dispatchers.IO) {
        if (userIds.isEmpty()) return@withContext emptyList()
        try {
            val documents = firestore.collection("users")
                .whereIn(com.google.firebase.firestore.FieldPath.documentId(), userIds)
                .get()
                .await()
            
            documents.map { doc ->
                User(
                    id = doc.id,
                    email = doc.getString("email") ?: "",
                    name = doc.getString("name") ?: "Пользователь",
                    avatarURL = doc.getString("avatarURL")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}