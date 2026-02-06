package com.example.calview.core.data.repository

import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepositoryImpl @Inject constructor(
    private val storage: FirebaseStorage
) : StorageRepository {
    
    companion object {
        private const val TAG = "StorageRepo"
        private const val MEALS_FOLDER = "meal_images"
    }
    
    override suspend fun uploadMealImage(localPath: String, userId: String, firestoreId: String): String? {
        if (localPath.isEmpty() || userId.isEmpty()) return null
        
        val file = File(localPath)
        if (!file.exists()) {
            Log.w(TAG, "Local file does not exist: $localPath")
            return null
        }
        
        return try {
            val storageRef = storage.reference
                .child(MEALS_FOLDER)
                .child(userId)
                .child("$firestoreId.jpg")
            
            Log.d(TAG, "Uploading image from $localPath")
            storageRef.putFile(android.net.Uri.fromFile(file)).await()
            
            val downloadUrl = storageRef.downloadUrl.await().toString()
            Log.d(TAG, "Upload successful: $downloadUrl")
            downloadUrl
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading image", e)
            null
        }
    }
    
    override suspend fun downloadMealImage(imageUrl: String, localPath: String): Boolean {
        if (imageUrl.isEmpty()) return false
        
        return try {
            val storageRef = storage.getReferenceFromUrl(imageUrl)
            val localFile = File(localPath)
            
            // Ensure parent directory exists
            localFile.parentFile?.mkdirs()
            
            Log.d(TAG, "Downloading image to $localPath")
            storageRef.getFile(localFile).await()
            Log.d(TAG, "Download successful")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading image", e)
            false
        }
    }
    
    override suspend fun deleteMealImage(imageUrl: String) {
        if (imageUrl.isEmpty()) return
        
        try {
            val storageRef = storage.getReferenceFromUrl(imageUrl)
            storageRef.delete().await()
            Log.d(TAG, "Deleted image: $imageUrl")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting image", e)
        }
    }

    override suspend fun deleteAllUserImages(userId: String) {
        if (userId.isEmpty()) return

        try {
            val userStorageRef = storage.reference
                .child(MEALS_FOLDER)
                .child(userId)

            // List all items in the user's folder and delete them
            // Note: Firebase Storage doesn't support deleting a folder directly, 
            // you must delete all files within it.
            val result = userStorageRef.listAll().await()
            result.items.forEach { fileRef ->
                fileRef.delete().await()
                Log.d(TAG, "Deleted file: ${fileRef.path}")
            }
            Log.d(TAG, "Successfully deleted all storage images for user: $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting all user images from storage", e)
        }
    }
}
