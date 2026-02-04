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

    /**
     * Save a fasting session to Firestore
     */
    suspend fun saveFastingSession(userId: String, session: com.example.calview.core.data.local.FastingSessionEntity)

    /**
     * Get all fasting sessions for a user from Firestore
     */
    suspend fun getFastingSessions(userId: String): List<com.example.calview.core.data.local.FastingSessionEntity>

    /**
     * Delete a fasting session from Firestore
     */
    suspend fun deleteFastingSession(userId: String, firestoreId: String)

    /**
     * Save a streak freeze entry to Firestore
     */
    suspend fun saveStreakFreeze(userId: String, freeze: com.example.calview.core.data.local.StreakFreezeEntity)

    /**
     * Get all streak freeze entries for a user from Firestore
     */
    suspend fun getStreakFreezes(userId: String): List<com.example.calview.core.data.local.StreakFreezeEntity>

    /**
     * Save a weight history entry to Firestore
     */
    suspend fun saveWeightEntry(userId: String, entry: com.example.calview.core.data.local.WeightHistoryEntity)

    /**
     * Get all weight history entries for a user from Firestore
     */
    suspend fun getWeightHistory(userId: String): List<com.example.calview.core.data.local.WeightHistoryEntity>
    
    // ==================== EXERCISE METHODS ====================
    
    /**
     * Save an exercise to Firestore
     */
    suspend fun saveExercise(userId: String, exercise: com.example.calview.core.data.local.ExerciseEntity)
    
    /**
     * Get all exercises for a user from Firestore
     */
    suspend fun getExercises(userId: String): List<com.example.calview.core.data.local.ExerciseEntity>
    
    /**
     * Delete an exercise from Firestore
     */
    suspend fun deleteExercise(userId: String, firestoreId: String)
    
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
    
    // ==================== WATER REMINDER SETTINGS ====================
    
    /**
     * Save water reminder settings
     */
    suspend fun saveWaterSettings(userId: String, settings: com.example.calview.core.data.local.WaterReminderSettingsEntity)
    
    /**
     * Get water reminder settings
     */
    suspend fun getWaterSettings(userId: String): com.example.calview.core.data.local.WaterReminderSettingsEntity?
    
    // ==================== GAMIFICATION (BADGES & CHALLENGGES) ====================
    
    /**
     * Save a badge (unlocked state)
     */
    suspend fun saveBadge(userId: String, badge: com.example.calview.core.data.local.BadgeEntity)
    
    /**
     * Get all unlocked badges
     */
    suspend fun getBadges(userId: String): List<com.example.calview.core.data.local.BadgeEntity>
    
    /**
     * Save a challenge progress
     */
    suspend fun saveChallenge(userId: String, challenge: com.example.calview.core.data.local.ChallengeEntity)
    
    /**
     * Get all challenges
     */
    suspend fun getChallenges(userId: String): List<com.example.calview.core.data.local.ChallengeEntity>

    // ==================== USER DATA MANAGEMENT ====================
    
    /**
     * Delete all user data from Firestore.
     * This should be called before deleting the Firebase Auth account.
     * Deletes: user document, meals subcollection, daily logs, etc.
     */
    suspend fun deleteUserData(userId: String)
}
