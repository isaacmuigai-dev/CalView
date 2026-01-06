package com.example.calview.core.data.repository

import android.util.Log
import com.example.calview.core.data.model.UserData
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of FirestoreRepository using Firebase Firestore
 */
@Singleton
class FirestoreRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : FirestoreRepository {
    
    companion object {
        private const val USERS_COLLECTION = "users"
        private const val FEATURE_REQUESTS_COLLECTION = "feature_requests"
        private const val COMMENTS_COLLECTION = "comments"
        private const val TAG = "FirestoreRepo"
    }
    
    override suspend fun saveUserData(userId: String, userData: UserData) {
        try {
            Log.d(TAG, "Attempting to save data for user: $userId to collection: $USERS_COLLECTION")
            Log.d(TAG, "Firestore instance: ${firestore.app.name}, database: ${firestore.firestoreSettings}")
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .set(userData)
                .await()
            Log.d(TAG, "Successfully saved user data to Firestore")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving to Firestore: ${e.message}", e)
            e.printStackTrace()
        }
    }
    
    override suspend fun getUserData(userId: String): UserData? {
        return try {
            val snapshot = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .await()
            
            snapshot.toObject(UserData::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    override fun observeUserData(userId: String): Flow<UserData?> = callbackFlow {
        val listener = firestore.collection(USERS_COLLECTION)
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    error.printStackTrace()
                    trySend(null)
                    return@addSnapshotListener
                }
                
                val userData = snapshot?.toObject(UserData::class.java)
                trySend(userData)
            }
        
        awaitClose { listener.remove() }
    }

    override suspend fun saveMeal(userId: String, meal: com.example.calview.core.data.local.MealEntity) {
        try {
            Log.d(TAG, "Saving meal to Firestore: ${meal.name}, ID: ${meal.firestoreId}")
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection("meals")
                .document(meal.firestoreId)
                .set(meal)
                .await()
            Log.d(TAG, "Meal saved successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving meal to Firestore", e)
            throw e
        }
    }

    override suspend fun getMeals(userId: String): List<com.example.calview.core.data.local.MealEntity> {
        return try {
            val snapshot = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection("meals")
                .get()
                .await()
            
            snapshot.toObjects(com.example.calview.core.data.local.MealEntity::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting meals from Firestore", e)
            emptyList()
        }
    }

    override suspend fun deleteMeal(userId: String, firestoreId: String) {
        try {
            Log.d(TAG, "Deleting meal from Firestore: $firestoreId")
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection("meals")
                .document(firestoreId)
                .delete()
                .await()
            Log.d(TAG, "Meal deleted successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting meal from Firestore", e)
            throw e
        }
    }
    
    // ==================== FEATURE REQUEST IMPLEMENTATIONS ====================
    
    override fun observeFeatureRequests(): Flow<List<FeatureRequestDto>> = callbackFlow {
        Log.d(TAG, "Starting to observe feature requests")
        val listener = firestore.collection(FEATURE_REQUESTS_COLLECTION)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error observing feature requests", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val requests = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(FeatureRequestDto::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing feature request", e)
                        null
                    }
                } ?: emptyList()
                
                Log.d(TAG, "Received ${requests.size} feature requests")
                trySend(requests)
            }
        
        awaitClose { 
            Log.d(TAG, "Stopping feature request observation")
            listener.remove() 
        }
    }
    
    override suspend fun postFeatureRequest(request: FeatureRequestDto): String {
        return try {
            Log.d(TAG, "Posting feature request: ${request.title}")
            val docRef = firestore.collection(FEATURE_REQUESTS_COLLECTION)
                .add(request)
                .await()
            Log.d(TAG, "Feature request posted with ID: ${docRef.id}")
            docRef.id
        } catch (e: Exception) {
            Log.e(TAG, "Error posting feature request", e)
            throw e
        }
    }
    
    override suspend fun voteOnRequest(requestId: String, userId: String, vote: Boolean) {
        try {
            Log.d(TAG, "Vote on request $requestId: $vote by user $userId")
            val docRef = firestore.collection(FEATURE_REQUESTS_COLLECTION).document(requestId)
            
            if (vote) {
                // Add vote
                docRef.update(
                    mapOf(
                        "votes" to FieldValue.increment(1),
                        "votedBy" to FieldValue.arrayUnion(userId)
                    )
                ).await()
            } else {
                // Remove vote
                docRef.update(
                    mapOf(
                        "votes" to FieldValue.increment(-1),
                        "votedBy" to FieldValue.arrayRemove(userId)
                    )
                ).await()
            }
            Log.d(TAG, "Vote updated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error voting on request", e)
            throw e
        }
    }
    
    override fun observeComments(requestId: String): Flow<List<CommentDto>> = callbackFlow {
        Log.d(TAG, "Starting to observe comments for request: $requestId")
        val listener = firestore.collection(FEATURE_REQUESTS_COLLECTION)
            .document(requestId)
            .collection(COMMENTS_COLLECTION)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error observing comments", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val comments = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(CommentDto::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing comment", e)
                        null
                    }
                } ?: emptyList()
                
                Log.d(TAG, "Received ${comments.size} comments")
                trySend(comments)
            }
        
        awaitClose { 
            Log.d(TAG, "Stopping comment observation")
            listener.remove() 
        }
    }
    
    override suspend fun postComment(requestId: String, comment: CommentDto): String {
        return try {
            Log.d(TAG, "Posting comment on request $requestId: ${comment.content.take(50)}")
            val docRef = firestore.collection(FEATURE_REQUESTS_COLLECTION)
                .document(requestId)
                .collection(COMMENTS_COLLECTION)
                .add(comment)
                .await()
            Log.d(TAG, "Comment posted with ID: ${docRef.id}")
            docRef.id
        } catch (e: Exception) {
            Log.e(TAG, "Error posting comment", e)
            throw e
        }
    }
    
    override suspend fun likeComment(requestId: String, commentId: String, userId: String, like: Boolean) {
        try {
            Log.d(TAG, "Like comment $commentId: $like by user $userId")
            val docRef = firestore.collection(FEATURE_REQUESTS_COLLECTION)
                .document(requestId)
                .collection(COMMENTS_COLLECTION)
                .document(commentId)
            
            if (like) {
                docRef.update(
                    mapOf(
                        "likes" to FieldValue.increment(1),
                        "likedBy" to FieldValue.arrayUnion(userId)
                    )
                ).await()
            } else {
                docRef.update(
                    mapOf(
                        "likes" to FieldValue.increment(-1),
                        "likedBy" to FieldValue.arrayRemove(userId)
                    )
                ).await()
            }
            Log.d(TAG, "Like updated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error liking comment", e)
            throw e
        }
    }
    
    override suspend fun updateCommentCount(requestId: String, count: Int) {
        try {
            Log.d(TAG, "Updating comment count for request $requestId to $count")
            firestore.collection(FEATURE_REQUESTS_COLLECTION)
                .document(requestId)
                .update("commentCount", count)
                .await()
            Log.d(TAG, "Comment count updated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating comment count", e)
            throw e
        }
    }
    
    // ==================== USER DATA MANAGEMENT ====================
    
    override suspend fun deleteUserData(userId: String) {
        try {
            Log.d(TAG, "Deleting all Firestore data for user: $userId")
            
            // 1. Delete all meals in the meals subcollection
            val mealsCollection = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection("meals")
            
            val mealsSnapshot = mealsCollection.get().await()
            for (mealDoc in mealsSnapshot.documents) {
                mealDoc.reference.delete().await()
                Log.d(TAG, "Deleted meal: ${mealDoc.id}")
            }
            Log.d(TAG, "Deleted ${mealsSnapshot.size()} meals")
            
            // 2. Delete all daily logs in the dailyLogs subcollection
            val dailyLogsCollection = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection("dailyLogs")
            
            val dailyLogsSnapshot = dailyLogsCollection.get().await()
            for (logDoc in dailyLogsSnapshot.documents) {
                logDoc.reference.delete().await()
                Log.d(TAG, "Deleted daily log: ${logDoc.id}")
            }
            Log.d(TAG, "Deleted ${dailyLogsSnapshot.size()} daily logs")
            
            // 3. Delete the user document itself
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .delete()
                .await()
            Log.d(TAG, "Deleted user document")
            
            Log.d(TAG, "Successfully deleted all Firestore data for user: $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting user data from Firestore", e)
            throw e
        }
    }
}
