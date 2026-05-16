package com.example.general_first_aid_kit.data.repository

import com.example.general_first_aid_kit.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override fun isUserAuthenticated(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    override suspend fun signIn(email: String, password: String): Result<Unit> = try {
        firebaseAuth.signInWithEmailAndPassword(email, password).await()
        saveFcmToken(firebaseAuth.currentUser?.uid)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun signUp(email: String, password: String, username: String): Result<Unit> = try {
        val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user ?: throw Exception("Ошибка создания пользователя")

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(username)
            .build()
        user.updateProfile(profileUpdates).await()

        firestore.collection("users").document(user.uid).set(
            mapOf(
                "name" to username,
                "email" to email,
                "avatarURL" to ""
            )
        ).await()

        saveFcmToken(user.uid)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    private fun saveFcmToken(userId: String?) {
        if (userId == null) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                firestore.collection("users").document(userId)
                    .set(mapOf("fcmToken" to token), SetOptions.merge())
                    .await()
            } catch (_: Exception) { }
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = try {
        firebaseAuth.sendPasswordResetEmail(email).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }
}