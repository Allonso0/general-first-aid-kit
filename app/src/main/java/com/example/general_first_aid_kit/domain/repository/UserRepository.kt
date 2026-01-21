package com.example.general_first_aid_kit.domain.repository

import com.example.general_first_aid_kit.domain.model.User

interface UserRepository {
    fun getCurrentUser(): User?
}