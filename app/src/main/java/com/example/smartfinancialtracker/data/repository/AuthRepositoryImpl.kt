package com.example.smartfinancialtracker.data.repository

import com.example.smartfinancialtracker.domain.model.AuthResult
import com.example.smartfinancialtracker.domain.model.User
import com.example.smartfinancialtracker.domain.repository.AuthRepository
import com.yourname.smartfinancialtracker.data.remote.firebase.FirebaseAuthService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authService: FirebaseAuthService
) : AuthRepository {

    override val currentUser: Flow<User?> = authService.currentUser.map { firebaseUser ->
        firebaseUser?.let {
            User(
                id = it.uid,
                email = it.email ?: "",
                displayName = it.displayName ?: "",
                photoUrl = it.photoUrl?.toString(),
                isEmailVerified = it.isEmailVerified
            )
        }
    }

    override suspend fun signUp(
        email: String,
        password: String,
        displayName: String
    ): AuthResult<User> {
        return authService.signUp(email, password, displayName)
    }

    override suspend fun signIn(email: String, password: String): AuthResult<User> {
        return authService.signIn(email, password)
    }

    override suspend fun signOut() {
        authService.signOut()
    }

    override suspend fun resetPassword(email: String): AuthResult<Unit> {
        return authService.resetPassword(email)
    }

    override suspend fun deleteAccount(): AuthResult<Unit> {
        return authService.deleteAccount()
    }
}