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
        private const val GROUPS_FOLDER = "group_photos"
        private const val PROFILES_FOLDER = "profile_photos"
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
    
    override suspend fun uploadGroupPhoto(localPath: String, groupId: String): String? {
        if (localPath.isEmpty()) return null
        val file = File(localPath)
        if (!file.exists()) return null

        return try {
            val storageRef = storage.reference
                .child(GROUPS_FOLDER)
                .child("$groupId.jpg")
            
            storageRef.putFile(android.net.Uri.fromFile(file)).await()
            storageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading group photo", e)
            null
        }
    }

    override suspend fun uploadProfilePhoto(localPath: String, userId: String): String? {
        if (localPath.isEmpty()) return null
        val file = File(localPath)
        if (!file.exists()) return null

        return try {
            val storageRef = storage.reference
                .child(PROFILES_FOLDER)
                .child("$userId.jpg")
            
            storageRef.putFile(android.net.Uri.fromFile(file)).await()
            storageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading profile photo to $PROFILES_FOLDER/$userId.jpg", e)
            if (e is com.google.firebase.storage.StorageException) {
                Log.e(TAG, "Storage Error Code: ${e.errorCode}, Http Result: ${e.httpResultCode}")
            }
            null
        }
    }

    override suspend fun uploadMessageImage(localPath: String, groupId: String): String? {
        if (localPath.isEmpty()) return null
        val file = File(localPath)
        if (!file.exists()) return null

        return try {
            val uuid = java.util.UUID.randomUUID().toString()
            val storageRef = storage.reference
                .child("message_images")
                .child(groupId)
                .child("$uuid.jpg")
            
            storageRef.putFile(android.net.Uri.fromFile(file)).await()
            storageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading message image", e)
            null
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

    override suspend fun uploadVoiceNote(localPath: String, groupId: String): String? {
        if (localPath.isEmpty()) return null
        val file = File(localPath)
        if (!file.exists()) return null

        return try {
            val uuid = java.util.UUID.randomUUID().toString()
            val storageRef = storage.reference
                .child("voice_notes")
                .child(groupId)
                .child("$uuid.m4a")
            
            storageRef.putFile(android.net.Uri.fromFile(file)).await()
            storageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading voice note", e)
            null
        }
    }

    override suspend fun deleteMessageImage(imageUrl: String) {
        if (imageUrl.isBlank()) return
        try {
            storage.getReferenceFromUrl(imageUrl).delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting message image: $imageUrl", e)
        }
    }

    override suspend fun deleteVoiceNote(audioUrl: String) {
        if (audioUrl.isBlank()) return
        try {
            storage.getReferenceFromUrl(audioUrl).delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting voice note: $audioUrl", e)
        }
    }
}
