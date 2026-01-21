package com.example.general_first_aid_kit.data.repository

import com.example.general_first_aid_kit.domain.model.User
import com.example.general_first_aid_kit.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
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
}