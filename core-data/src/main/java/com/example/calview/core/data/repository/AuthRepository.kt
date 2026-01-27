package com.example.calview.core.data.repository

import com.google.firebase.auth.FirebaseUser

/**
 * Repository for authentication operations.
 */
interface AuthRepository {
    
    /**
     * Flow of the current signed-in user.
     * Emits the user object when signed in, or null when signed out.
     */
    val authState: kotlinx.coroutines.flow.Flow<com.google.firebase.auth.FirebaseUser?>

    /**
     * Check if a user is currently signed in.
     */
    fun isSignedIn(): Boolean
    
    /**
     * Get the current signed-in user, or null if not signed in.
     */
    fun getCurrentUser(): FirebaseUser?
    
    /**
     * Get the current user's email, or empty string if not signed in.
     */
    fun getUserEmail(): String
    
    /**
     * Get the current user's ID, or empty string if not signed in.
     */
    fun getUserId(): String
    
    /**
     * Sign out the current user.
     */
    suspend fun signOut()
    
    /**
     * Permanently delete the current user's account.
     * This will delete the Firebase Auth account.
     * @return Result indicating success or failure with error message
     */
    suspend fun deleteAccount(): Result<Unit>
    
    /**
     * Re-authenticate the user with Google credentials.
     * Required before sensitive operations like account deletion if session is stale.
     * @param idToken The Google ID token from sign-in
     * @return Result indicating success or failure
     */
    suspend fun reauthenticateWithGoogle(idToken: String): Result<Unit>
}
