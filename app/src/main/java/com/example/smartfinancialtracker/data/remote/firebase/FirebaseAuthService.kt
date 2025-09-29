package com.yourname.smartfinancialtracker.data.remote.firebase

import com.example.smartfinancialtracker.domain.model.User
import com.example.smartfinancialtracker.utils.constants.Constants
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.example.smartfinancialtracker.domain.model.AuthResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthService @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    // Current user as Flow
    val currentUser: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signUp(
        email: String,
        password: String,
        displayName: String
    ): AuthResult<User> {
        return try {
            // Create auth account
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("User creation failed")

            // Update display name
            val profileUpdates = userProfileChangeRequest {
                this.displayName = displayName
            }
            firebaseUser.updateProfile(profileUpdates).await()

            // Send verification email
            firebaseUser.sendEmailVerification().await()

            // Create user document in Firestore
            val user = User(
                id = firebaseUser.uid,
                email = email,
                displayName = displayName,
                isEmailVerified = false,
                categories = Constants.DEFAULT_CATEGORIES
            )

            firestore.collection(Constants.USERS_COLLECTION)
                .document(firebaseUser.uid)
                .set(user)
                .await()

            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error(handleAuthException(e))
        }
    }

    suspend fun signIn(email: String, password: String): AuthResult<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("Sign in failed")

            // Fetch user data from Firestore
            val userDoc = firestore.collection(Constants.USERS_COLLECTION)
                .document(firebaseUser.uid)
                .get()
                .await()

            val user = if (userDoc.exists()) {
                userDoc.toObject(User::class.java) ?: createUserFromFirebaseUser(firebaseUser)
            } else {
                // First time sign in after account creation
                createAndSaveUser(firebaseUser)
            }

            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error(handleAuthException(e))
        }
    }

    suspend fun signOut() {
        auth.signOut()
    }

    suspend fun resetPassword(email: String): AuthResult<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error(handleAuthException(e))
        }
    }

    suspend fun deleteAccount(): AuthResult<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("No user logged in")

            // Delete user data from Firestore
            firestore.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .delete()
                .await()

            // Delete auth account
            auth.currentUser?.delete()?.await()

            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error(handleAuthException(e))
        }
    }

    private suspend fun createAndSaveUser(firebaseUser: FirebaseUser): User {
        val user = createUserFromFirebaseUser(firebaseUser)
        firestore.collection(Constants.USERS_COLLECTION)
            .document(firebaseUser.uid)
            .set(user)
            .await()
        return user
    }

    private fun createUserFromFirebaseUser(firebaseUser: FirebaseUser): User {
        return User(
            id = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            displayName = firebaseUser.displayName ?: "",
            photoUrl = firebaseUser.photoUrl?.toString(),
            isEmailVerified = firebaseUser.isEmailVerified,
            categories = Constants.DEFAULT_CATEGORIES
        )
    }

    private fun handleAuthException(e: Exception): Exception {
        return when (e) {
            is FirebaseAuthWeakPasswordException ->
                Exception("Password should be at least 6 characters")
            is FirebaseAuthInvalidCredentialsException ->
                Exception("Invalid email or password")
            is FirebaseAuthUserCollisionException ->
                Exception("An account already exists with this email")
            is FirebaseAuthInvalidUserException ->
                Exception("User account has been disabled or deleted")
            else -> Exception(e.message ?: "Authentication failed")
        }
    }
}