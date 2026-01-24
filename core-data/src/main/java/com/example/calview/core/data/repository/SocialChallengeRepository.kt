package com.example.calview.core.data.repository

import android.util.Log
import com.example.calview.core.data.local.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for social challenges with Firebase Firestore sync.
 * Enables real-time friend challenges and leaderboards.
 */
@Singleton
class SocialChallengeRepository @Inject constructor(
    private val socialChallengeDao: SocialChallengeDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val challengesCollection = firestore.collection("social_challenges")
    private val participantsCollection = firestore.collection("challenge_participants")
    
    // Active Firestore listeners
    private var challengeListener: ListenerRegistration? = null
    
    /**
     * Get current user ID
     */
    fun getCurrentUserId(): String? = auth.currentUser?.uid
    
    /**
     * Create a new challenge
     * @throws IllegalStateException if user not logged in
     * @throws IllegalArgumentException if input validation fails
     */
    suspend fun createChallenge(
        title: String,
        description: String,
        type: SocialChallengeType,
        targetValue: Int,
        durationDays: Int
    ): SocialChallengeEntity {
        Log.d("SocialChallengeRepo", "Attempting to create challenge: $title, type: $type")
        
        val userId = auth.currentUser?.uid ?: run {
            Log.e("SocialChallengeRepo", "Create challenge failed: User not logged in")
            throw IllegalStateException("User not logged in")
        }
        val userName = auth.currentUser?.displayName ?: "Anonymous"
        
        // Security: Validate and sanitize inputs
        val sanitizedTitle = sanitizeText(title, maxLength = 100)
        val sanitizedDescription = sanitizeText(description, maxLength = 500)
        val clampedTargetValue = targetValue.coerceIn(1, 10000)
        val clampedDuration = durationDays.coerceIn(1, 30)
        
        require(sanitizedTitle.isNotBlank()) { "Challenge title cannot be empty" }
        
        val now = System.currentTimeMillis()
        val endDate = now + (clampedDuration * 24 * 60 * 60 * 1000L)
        val inviteCode = generateInviteCode()
        Log.d("SocialChallengeRepo", "Generated invite code: $inviteCode")
        
        val challenge = SocialChallengeEntity(
            id = UUID.randomUUID().toString(),
            title = sanitizedTitle,
            description = sanitizedDescription,
            type = type.name,
            targetValue = clampedTargetValue,
            startDate = now,
            endDate = endDate,
            creatorId = userId,
            creatorName = sanitizeText(userName, maxLength = 50),
            isActive = true,
            inviteCode = inviteCode
        )
        
        // Save to local DB
        socialChallengeDao.insertChallenge(challenge)
        Log.d("SocialChallengeRepo", "Challenge saved locally. Syncing with Firestore...")
        
        // Sync to Firestore
        try {
            val docRef = challengesCollection.document(challenge.id)
            docRef.set(challenge.toFirestoreMap()).await()
            Log.d("SocialChallengeRepo", "Firestore sync successful for challenge ${challenge.id}")
        } catch (e: Exception) {
            val message = e.message ?: "Unknown error"
            Log.e("SocialChallengeRepo", "Firestore sync FAILED for challenge ${challenge.id}: $message", e)
            if (message.contains("PERMISSION_DENIED")) {
                Log.e("SocialChallengeRepo", "CRITICAL: Permission denied on 'social_challenges'. Check rules for creatorId/userId.")
            }
            // We keep it in local DB even if sync fails (offline support)
        }
        
        // Add creator as first participant
        addParticipant(challenge.id, userId, userName, auth.currentUser?.photoUrl?.toString() ?: "")
        
        return challenge
    }
    
    /**
     * Sanitize text input to prevent injection and limit length
     */
    private fun sanitizeText(input: String, maxLength: Int): String {
        return input
            .take(maxLength)
            .replace(Regex("[\\x00-\\x1F\\x7F]"), "") // Remove control characters
            .trim()
    }
    
    /**
     * Join a challenge using invite code
     */
    suspend fun joinChallenge(inviteCode: String): Result<SocialChallengeEntity> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
        val userName = auth.currentUser?.displayName ?: "Anonymous"
        val photoUrl = auth.currentUser?.photoUrl?.toString() ?: ""
        
        return try {
            // Find challenge by invite code in Firestore
            val querySnapshot = challengesCollection
                .whereEqualTo("inviteCode", inviteCode)
                .whereEqualTo("isActive", true)
                .get()
                .await()
            
            if (querySnapshot.isEmpty) {
                return Result.failure(Exception("Invalid or expired invite code"))
            }
            
            val doc = querySnapshot.documents.first()
            val challenge = doc.toObject(SocialChallengeEntity::class.java)
                ?: return Result.failure(Exception("Failed to parse challenge"))
            
            // Check if already joined
            val existingParticipants = socialChallengeDao.getParticipants(challenge.id)
            if (existingParticipants.any { it.odsmUserId == userId }) {
                return Result.failure(Exception("Already joined this challenge"))
            }
            
            // Save challenge locally
            socialChallengeDao.insertChallenge(challenge)
            
            // Add as participant
            addParticipant(challenge.id, userId, userName, photoUrl)
            
            Result.success(challenge)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Add a participant to a challenge
     */
    private suspend fun addParticipant(
        challengeId: String,
        odsmUserId: String,
        displayName: String,
        photoUrl: String
    ) {
        val participant = ChallengeParticipantEntity(
            challengeId = challengeId,
            odsmUserId = odsmUserId,
            displayName = displayName,
            photoUrl = photoUrl,
            currentProgress = 0,
            progressPercent = 0f,
            hasAccepted = true
        )
        
        // Save locally
        socialChallengeDao.insertParticipant(participant)
        
        // Sync to Firestore
        val docId = "${challengeId}_${odsmUserId}"
        participantsCollection.document(docId).set(participant.toFirestoreMap()).await()
    }
    
    /**
     * Update user's progress in a challenge
     */
    suspend fun updateProgress(challengeId: String, progress: Int, progressPercent: Float) {
        val userId = auth.currentUser?.uid ?: return
        
        // Update locally
        socialChallengeDao.updateProgress(challengeId, userId, progress, progressPercent)
        
        // Sync to Firestore
        val docId = "${challengeId}_${userId}"
        participantsCollection.document(docId).update(
            mapOf(
                "userId" to userId, // Ensure userId is present for security rules
                "odsmUserId" to userId,
                "currentProgress" to progress,
                "progressPercent" to progressPercent,
                "lastUpdated" to System.currentTimeMillis()
            )
        ).await()
    }
    
    /**
     * Observe user's active challenges
     */
    fun observeUserChallenges(): Flow<List<SocialChallengeEntity>> {
        val userId = auth.currentUser?.uid ?: return kotlinx.coroutines.flow.flowOf(emptyList())
        return socialChallengeDao.observeUserChallenges(userId)
    }
    
    /**
     * Observe participants in a challenge (real-time from Firestore)
     */
    fun observeLeaderboard(challengeId: String): Flow<List<ChallengeParticipantEntity>> = callbackFlow {
        // We remove orderBy from the Firestore query to simplify it.
        // Queries with both an equality filter AND an orderBy often require a composite index.
        // If the index is missing, it SOMETIMES reports as PERMISSION_DENIED.
        // Also, sorting in memory is fine for small leaderboards (e.g. < 50-100 friends).
        val listener = participantsCollection
            .whereEqualTo("challengeId", challengeId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    val errorCode = error.code.name
                    val message = error.message ?: ""
                    Log.e("SocialChallengeRepo", "Firestore leaderboard sync failed: $errorCode - $message", error)
                    if (errorCode == "PERMISSION_DENIED") {
                        Log.e("SocialChallengeRepo", "CRITICAL: Permission denied. ACTION REQUIRED:")
                        Log.e("SocialChallengeRepo", "1. Open Firebase Console > Firestore > Rules")
                        Log.e("SocialChallengeRepo", "2. Add this rule for the 'challenge_participants' collection:")
                        Log.e("SocialChallengeRepo", "   match /challenge_participants/{id} { allow read: if request.auth != null; }")
                    }
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val participants = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ChallengeParticipantEntity::class.java)
                }?.sortedByDescending { it.currentProgress } // Sort in memory instead
                ?.mapIndexed { index, p -> p.copy(rank = index + 1) } ?: emptyList()
                
                trySend(participants)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Get share link for a challenge
     */
    fun getShareLink(challenge: SocialChallengeEntity): String {
        return "https://calview.app/challenge/${challenge.inviteCode}"
    }
    
    /**
     * Generate unique invite code
     */
    private fun generateInviteCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars.random() }.joinToString("")
    }
    
    /**
     * Leave a challenge
     */
    suspend fun leaveChallenge(challengeId: String) {
        val userId = auth.currentUser?.uid ?: return
        
        socialChallengeDao.removeParticipant(challengeId, userId)
        
        val docId = "${challengeId}_${userId}"
        participantsCollection.document(docId).delete().await()
    }
    
    /**
     * Update progress for all user's active challenges of a specific type
     */
    suspend fun updateUserProgressForType(type: SocialChallengeType, progress: Int) {
        val userId = auth.currentUser?.uid ?: return
        
        // Find all active challenges for this user
        val challenges = socialChallengeDao.getActiveChallengesSync(userId)
        
        challenges.forEach { challenge ->
            if (challenge.type == type.name) {
                // Calculate progress percent based on target value
                val percent = if (challenge.targetValue > 0) {
                    (progress.toFloat() / challenge.targetValue).coerceIn(0f, 1f)
                } else {
                    1f
                }
                
                updateProgress(challenge.id, progress, percent)
            }
        }
    }
    
    /**
     * End a challenge (creator only)
     */
    suspend fun endChallenge(challengeId: String) {
        val userId = auth.currentUser?.uid ?: return
        val challenge = socialChallengeDao.getChallengeById(challengeId) ?: return
        
        if (challenge.creatorId != userId) return // Only creator can end
        
        socialChallengeDao.updateChallenge(challenge.copy(isActive = false))
        challengesCollection.document(challengeId).update("isActive", false).await()
    }
    
    // Extension functions for Firestore conversion
    private fun SocialChallengeEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "title" to title,
        "description" to description,
        "type" to type,
        "targetValue" to targetValue,
        "startDate" to startDate,
        "endDate" to endDate,
        "creatorId" to creatorId,
        "userId" to creatorId, // Add generic userId for security rules
        "creatorName" to creatorName,
        "isActive" to isActive,
        "inviteCode" to inviteCode
    )
    
    private fun ChallengeParticipantEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
        "challengeId" to challengeId,
        "odsmUserId" to odsmUserId,
        "userId" to odsmUserId, // Add generic userId for security rules
        "displayName" to displayName,
        "photoUrl" to photoUrl,
        "currentProgress" to currentProgress,
        "progressPercent" to progressPercent,
        "lastUpdated" to lastUpdated,
        "hasAccepted" to hasAccepted
    )

    suspend fun clearAllData() {
        socialChallengeDao.deleteAllChallenges()
        socialChallengeDao.deleteAllParticipants()
    }
}
