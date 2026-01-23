package com.example.calview.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GamificationDao {

    // --- Challenges ---
    @Query("SELECT * FROM challenges WHERE isCompleted = 0")
    fun getActiveChallenges(): Flow<List<ChallengeEntity>>

    @Query("SELECT * FROM challenges WHERE isCompleted = 1")
    fun getCompletedChallenges(): Flow<List<ChallengeEntity>>

    @Query("SELECT * FROM challenges WHERE id = :id")
    suspend fun getChallengeById(id: String): ChallengeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenges(challenges: List<ChallengeEntity>)

    @Update
    suspend fun updateChallenge(challenge: ChallengeEntity)

    @Query("DELETE FROM challenges")
    suspend fun clearAllChallenges()


    // --- Badges ---
    @Query("SELECT * FROM badges ORDER BY dateUnlocked DESC")
    fun getAllBadges(): Flow<List<BadgeEntity>>
    
    @Query("SELECT * FROM badges WHERE id = :id")
    suspend fun getBadgeById(id: String): BadgeEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBadge(badge: BadgeEntity)
}
