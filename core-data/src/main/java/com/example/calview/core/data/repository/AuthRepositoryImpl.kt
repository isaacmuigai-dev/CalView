package com.example.calview.core.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Custom exception indicating re-authentication is required.
 */
class ReAuthenticationRequiredException(message: String = "Re-authentication required") : Exception(message)

/**
 * Implementation of AuthRepository using Firebase Authentication.
 */
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {
    
    override val authState: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose {
            firebaseAuth.removeAuthStateListener(listener)
        }
    }
    
    override fun isSignedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }
    
    override fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }
    
    override fun getUserEmail(): String {
        return firebaseAuth.currentUser?.email ?: ""
    }
    
    override fun getUserId(): String {
        return firebaseAuth.currentUser?.uid ?: ""
    }
    
    override suspend fun signOut() {
        firebaseAuth.signOut()
    }
    
    override suspend fun deleteAccount(): Result<Unit> {
        android.util.Log.d("AuthRepository", "Attempting to delete account under getCurrentUser: ${firebaseAuth.currentUser?.uid}")
        return try {
            val user = firebaseAuth.currentUser
            if (user != null) {
                user.delete().await()
                android.util.Log.d("AuthRepository", "Account deleted successfully")
                Result.success(Unit)
            } else {
                android.util.Log.e("AuthRepository", "No user signed in during delete attempt")
                Result.failure(Exception("No user signed in"))
            }
        } catch (e: FirebaseAuthRecentLoginRequiredException) {
            android.util.Log.w("AuthRepository", "Recent login required for deletion", e)
            // Rethrow or return custom exception so ViewModel knows
            Result.failure(e) 
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Error deleting account", e)
            Result.failure(e)
        }
    }
    
    override suspend fun reauthenticateWithGoogle(idToken: String): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser
            if (user != null) {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                user.reauthenticate(credential).await()
                Result.success(Unit)
            } else {
                Result.failure(Exception("No user signed in"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

