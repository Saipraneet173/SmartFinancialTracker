package com.example.smartfinancialtracker.domain.repository

import com.example.smartfinancialtracker.domain.model.AuthResult
import com.example.smartfinancialtracker.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    suspend fun signUp(email: String, password: String, displayName: String): AuthResult<User>
    suspend fun signIn(email: String, password: String): AuthResult<User>
    suspend fun signOut()
    suspend fun resetPassword(email: String): AuthResult<Unit>
    suspend fun deleteAccount(): AuthResult<Unit>
}