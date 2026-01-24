package com.example.calview.core.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO for social challenge operations.
 */
@Dao
interface SocialChallengeDao {
    
    // Challenge CRUD
    @Query("SELECT * FROM social_challenges WHERE isActive = 1 ORDER BY startDate DESC")
    fun observeActiveChallenges(): Flow<List<SocialChallengeEntity>>
    
    @Query("SELECT * FROM social_challenges WHERE id = :id")
    suspend fun getChallengeById(id: String): SocialChallengeEntity?
    
    @Query("SELECT * FROM social_challenges WHERE inviteCode = :code")
    suspend fun getChallengeByInviteCode(code: String): SocialChallengeEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenge(challenge: SocialChallengeEntity)
    
    @Update
    suspend fun updateChallenge(challenge: SocialChallengeEntity)
    
    @Query("DELETE FROM social_challenges WHERE id = :id")
    suspend fun deleteChallenge(id: String)
    
    // Participants
    @Query("SELECT * FROM challenge_participants WHERE challengeId = :challengeId ORDER BY currentProgress DESC")
    fun observeParticipants(challengeId: String): Flow<List<ChallengeParticipantEntity>>
    
    @Query("SELECT * FROM challenge_participants WHERE challengeId = :challengeId ORDER BY currentProgress DESC")
    suspend fun getParticipants(challengeId: String): List<ChallengeParticipantEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipant(participant: ChallengeParticipantEntity)
    
    @Query("UPDATE challenge_participants SET currentProgress = :progress, progressPercent = :percent, lastUpdated = :timestamp WHERE challengeId = :challengeId AND odsmUserId = :odsmUserId")
    suspend fun updateProgress(challengeId: String, odsmUserId: String, progress: Int, percent: Float, timestamp: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM challenge_participants WHERE challengeId = :challengeId AND odsmUserId = :odsmUserId")
    suspend fun removeParticipant(challengeId: String, odsmUserId: String)
    
    // User's challenges
    @Query("""
        SELECT sc.* FROM social_challenges sc
        INNER JOIN challenge_participants cp ON sc.id = cp.challengeId
        WHERE cp.odsmUserId = :userId AND sc.isActive = 1
        ORDER BY sc.startDate DESC
    """)
    fun observeUserChallenges(userId: String): Flow<List<SocialChallengeEntity>>

    @Query("""
        SELECT sc.* FROM social_challenges sc
        INNER JOIN challenge_participants cp ON sc.id = cp.challengeId
        WHERE cp.odsmUserId = :userId AND sc.isActive = 1
        ORDER BY sc.startDate DESC
    """)
    suspend fun getActiveChallengesSync(userId: String): List<SocialChallengeEntity>

    @Query("DELETE FROM social_challenges")
    suspend fun deleteAllChallenges()

    @Query("DELETE FROM challenge_participants")
    suspend fun deleteAllParticipants()
}
