package com.example.calview.core.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Implementation of AuthRepository using Firebase Authentication.
 */
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {
    
    override fun isSignedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }
    
    override fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }
    
    override suspend fun signOut() {
        firebaseAuth.signOut()
    }
}
