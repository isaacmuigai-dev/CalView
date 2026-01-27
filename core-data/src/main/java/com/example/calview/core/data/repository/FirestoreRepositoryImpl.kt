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
            Log.d(TAG, "════════════════════════════════════════")
            Log.d(TAG, "📝 saveUserData called")
            Log.d(TAG, "   userId: '$userId'")
            Log.d(TAG, "   collection: '$USERS_COLLECTION'")
            Log.d(TAG, "   Firestore instance: ${firestore.app.name}")
            Log.d(TAG, "════════════════════════════════════════")
            
            val docRef = firestore.collection(USERS_COLLECTION).document(userId)
            Log.d(TAG, "📄 Document path: ${docRef.path}")
            
            Log.d(TAG, "⏳ Calling .set() on Firestore...")
            val startTime = System.currentTimeMillis()
            
            docRef.set(userData).await()
            
            val elapsed = System.currentTimeMillis() - startTime
            Log.d(TAG, "✅ Firestore write SUCCESS in ${elapsed}ms")
            Log.d(TAG, "   Document written to: ${docRef.path}")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Firestore write FAILED!")
            Log.e(TAG, "   Error message: ${e.message}")
            Log.e(TAG, "   Exception type: ${e.javaClass.simpleName}")
            Log.e(TAG, "   Full stack trace:")
            e.printStackTrace()
        }
    }
    
    override suspend fun getUserData(userId: String): UserData? {
        return try {
            Log.d(TAG, "getUserData called for userId: $userId")
            val snapshot = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .await()
            
            if (snapshot.exists()) {
                Log.d(TAG, "✅ Document EXISTS for user: $userId")
                val userData = snapshot.toObject(UserData::class.java)
                Log.d(TAG, "   isOnboardingComplete = ${userData?.isOnboardingComplete}")
                userData
            } else {
                Log.w(TAG, "⚠️ Document DOES NOT EXIST for user: $userId")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting user data for $userId", e)
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

    override suspend fun saveFastingSession(userId: String, session: com.example.calview.core.data.local.FastingSessionEntity) {
        try {
            Log.d(TAG, "Saving fasting session to Firestore: ${session.fastingType}, ID: ${session.firestoreId}")
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection("fasting_sessions")
                .document(session.firestoreId)
                .set(session)
                .await()
            Log.d(TAG, "Fasting session saved successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving fasting session to Firestore", e)
            throw e
        }
    }

    override suspend fun getFastingSessions(userId: String): List<com.example.calview.core.data.local.FastingSessionEntity> {
        return try {
            val snapshot = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection("fasting_sessions")
                .get()
                .await()
            
            snapshot.toObjects(com.example.calview.core.data.local.FastingSessionEntity::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting fasting sessions from Firestore", e)
            emptyList()
        }
    }

    override suspend fun deleteFastingSession(userId: String, firestoreId: String) {
        try {
            Log.d(TAG, "Deleting fasting session from Firestore: $firestoreId")
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection("fasting_sessions")
                .document(firestoreId)
                .delete()
                .await()
            Log.d(TAG, "Fasting session deleted successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting fasting session from Firestore", e)
            throw e
        }
    }

    override suspend fun saveStreakFreeze(userId: String, freeze: com.example.calview.core.data.local.StreakFreezeEntity) {
        try {
            Log.d(TAG, "Saving streak freeze to Firestore: ${freeze.month}, ID: ${freeze.month}")
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection("streak_freezes")
                .document(freeze.month)
                .set(freeze)
                .await()
            Log.d(TAG, "Streak freeze saved successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving streak freeze to Firestore", e)
            throw e
        }
    }

    override suspend fun getStreakFreezes(userId: String): List<com.example.calview.core.data.local.StreakFreezeEntity> {
        return try {
            val snapshot = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection("streak_freezes")
                .get()
                .await()
            
            snapshot.toObjects(com.example.calview.core.data.local.StreakFreezeEntity::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting streak freezes from Firestore", e)
            emptyList()
        }
    }

    override suspend fun saveWeightEntry(userId: String, entry: com.example.calview.core.data.local.WeightHistoryEntity) {
        try {
            Log.d(TAG, "Saving weight entry to Firestore: ${entry.weight}, ID: ${entry.firestoreId}")
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection("weight_history")
                .document(entry.firestoreId)
                .set(entry)
                .await()
            Log.d(TAG, "Weight entry saved successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving weight entry to Firestore", e)
            throw e
        }
    }

    override suspend fun getWeightHistory(userId: String): List<com.example.calview.core.data.local.WeightHistoryEntity> {
        return try {
            val snapshot = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection("weight_history")
                .get()
                .await()
            
            snapshot.toObjects(com.example.calview.core.data.local.WeightHistoryEntity::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting weight history from Firestore", e)
            emptyList()
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
            
            // 3. Delete all fasting sessions in the fasting_sessions subcollection
            val fastingCollection = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection("fasting_sessions")
            
            val fastingSnapshot = fastingCollection.get().await()
            for (fastDoc in fastingSnapshot.documents) {
                fastDoc.reference.delete().await()
                Log.d(TAG, "Deleted fasting session: ${fastDoc.id}")
            }
            Log.d(TAG, "Deleted ${fastingSnapshot.size()} fasting sessions")
            
            // 4. Delete all streak freezes in the streak_freezes subcollection
            val streakFreezesCollection = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection("streak_freezes")
            
            val streakFreezesSnapshot = streakFreezesCollection.get().await()
            for (freezeDoc in streakFreezesSnapshot.documents) {
                freezeDoc.reference.delete().await()
                Log.d(TAG, "Deleted streak freeze: ${freezeDoc.id}")
            }
            Log.d(TAG, "Deleted ${streakFreezesSnapshot.size()} streak freezes")
            
            // 5. Delete all weight entries in the weight_history subcollection
            val weightHistoryCollection = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection("weight_history")
            
            val weightHistorySnapshot = weightHistoryCollection.get().await()
            for (weightDoc in weightHistorySnapshot.documents) {
                weightDoc.reference.delete().await()
                Log.d(TAG, "Deleted weight entry: ${weightDoc.id}")
            }
            Log.d(TAG, "Deleted ${weightHistorySnapshot.size()} weight entries")

            // 6. Delete challenge participation
            val participationQuery = firestore.collection("challenge_participants")
                .whereEqualTo("odsmUserId", userId)
            val participationSnapshot = participationQuery.get().await()
            for (pDoc in participationSnapshot.documents) {
                pDoc.reference.delete().await()
                Log.d(TAG, "Deleted challenge participation: ${pDoc.id}")
            }
            Log.d(TAG, "Deleted ${participationSnapshot.size()} challenge participations")

            // 7. Delete social challenges created by user
            val challengesQuery = firestore.collection("social_challenges")
                .whereEqualTo("creatorId", userId)
            val challengesSnapshot = challengesQuery.get().await()
            for (cDoc in challengesSnapshot.documents) {
                cDoc.reference.delete().await()
                Log.d(TAG, "Deleted social challenge: ${cDoc.id}")
            }
            Log.d(TAG, "Deleted ${challengesSnapshot.size()} social challenges")
            
            // 8. Delete the user document itself
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
