package com.example.calview.core.data.repository

import com.example.calview.core.data.model.UserData
import kotlinx.coroutines.flow.Flow

/**
 * Data Transfer Object for Feature Requests in Firestore
 */
data class FeatureRequestDto(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorPhotoUrl: String = "",
    val votes: Int = 0,
    val votedBy: List<String> = emptyList(),
    val commentCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "OPEN",
    val tags: List<String> = emptyList()
)

/**
 * Data Transfer Object for Comments in Firestore
 */
data class CommentDto(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorPhotoUrl: String = "",
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val likes: Int = 0,
    val likedBy: List<String> = emptyList()
)

/**
 * Repository interface for Firestore operations
 */
interface FirestoreRepository {
    /**
     * Save user data to Firestore
     */
    suspend fun saveUserData(userId: String, userData: UserData)
    
    /**
     * Get user data from Firestore (one-time fetch)
     */
    suspend fun getUserData(userId: String): UserData?
    
    /**
     * Observe user data changes from Firestore in real-time
     */
    fun observeUserData(userId: String): Flow<UserData?>

    /**
     * Save a meal to Firestore
     */
    suspend fun saveMeal(userId: String, meal: com.example.calview.core.data.local.MealEntity)

    /**
     * Get all meals for a user from Firestore
     */
    suspend fun getMeals(userId: String): List<com.example.calview.core.data.local.MealEntity>

    /**
     * Delete a meal from Firestore
     */
    suspend fun deleteMeal(userId: String, firestoreId: String)
    
    // ==================== FEATURE REQUEST METHODS ====================
    
    /**
     * Observe all feature requests in real-time (globally shared across all users)
     */
    fun observeFeatureRequests(): Flow<List<FeatureRequestDto>>
    
    /**
     * Post a new feature request
     */
    suspend fun postFeatureRequest(request: FeatureRequestDto): String
    
    /**
     * Vote on a feature request (toggle vote)
     */
    suspend fun voteOnRequest(requestId: String, userId: String, vote: Boolean)
    
    /**
     * Observe comments for a specific feature request in real-time
     */
    fun observeComments(requestId: String): Flow<List<CommentDto>>
    
    /**
     * Post a comment on a feature request
     */
    suspend fun postComment(requestId: String, comment: CommentDto): String
    
    /**
     * Like a comment (toggle like)
     */
    suspend fun likeComment(requestId: String, commentId: String, userId: String, like: Boolean)
    
    /**
     * Update comment count for a request
     */
    suspend fun updateCommentCount(requestId: String, count: Int)
    
    // ==================== USER DATA MANAGEMENT ====================
    
    /**
     * Delete all user data from Firestore.
     * This should be called before deleting the Firebase Auth account.
     * Deletes: user document, meals subcollection, daily logs, etc.
     */
    suspend fun deleteUserData(userId: String)
}
