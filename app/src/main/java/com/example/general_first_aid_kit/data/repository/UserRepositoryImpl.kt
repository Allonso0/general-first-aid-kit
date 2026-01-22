package com.example.general_first_aid_kit.data.repository

import android.net.Uri
import com.example.general_first_aid_kit.domain.model.User
import com.example.general_first_aid_kit.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val storage: FirebaseStorage
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

    override suspend fun updateUserProfile(name: String, photoUri: Uri?): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser
                ?: return Result.failure(Exception("Пользователь не найден"))

            var finalPhotoUri: Uri? = null

            if (photoUri != null) {
                val storageRef = storage.reference.child("avatars/${user.uid}.jpg")
                storageRef.putFile(photoUri).await()
                finalPhotoUri = storageRef.downloadUrl.await()
            }

            val profileUpdates = userProfileChangeRequest {
                displayName = name

                // TODO: подумать над логикой проверки
                if (finalPhotoUri != null) {
                    this.photoUri = finalPhotoUri
                }
            }

            user.updateProfile(profileUpdates).await()
            user.reload().await()

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}