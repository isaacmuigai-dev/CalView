package com.example.calview.core.data.repository

import com.google.firebase.auth.FirebaseUser

/**
 * Repository for authentication operations.
 */
interface AuthRepository {
    
    /**
     * Check if a user is currently signed in.
     */
    fun isSignedIn(): Boolean
    
    /**
     * Get the current signed-in user, or null if not signed in.
     */
    fun getCurrentUser(): FirebaseUser?
    
    /**
     * Sign out the current user.
     */
    suspend fun signOut()
}
