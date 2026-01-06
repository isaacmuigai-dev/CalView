package com.example.calview.core.data.repository

import android.net.Uri

/**
 * Repository interface for Firebase Storage operations.
 */
interface StorageRepository {
    
    /**
     * Upload an image file to Firebase Storage.
     * @param localPath The local file path of the image.
     * @param userId The user's ID for organizing storage.
     * @param firestoreId The meal's firestore ID for unique naming.
     * @return The download URL of the uploaded image, or null if failed.
     */
    suspend fun uploadMealImage(localPath: String, userId: String, firestoreId: String): String?
    
    /**
     * Download an image from Firebase Storage to local storage.
     * @param imageUrl The download URL of the image.
     * @param localPath Where to save the downloaded image.
     * @return True if download succeeded, false otherwise.
     */
    suspend fun downloadMealImage(imageUrl: String, localPath: String): Boolean
    
    /**
     * Delete an image from Firebase Storage.
     * @param imageUrl The download URL of the image to delete.
     */
    suspend fun deleteMealImage(imageUrl: String)
}
