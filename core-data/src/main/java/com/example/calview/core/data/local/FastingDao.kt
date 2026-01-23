package com.example.calview.core.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for fasting sessions.
 */
@Dao
interface FastingDao {
    
    @Query("SELECT * FROM fasting_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<FastingSessionEntity>>
    
    @Query("SELECT * FROM fasting_sessions WHERE endTime IS NULL LIMIT 1")
    fun getActiveFast(): Flow<FastingSessionEntity?>
    
    @Query("SELECT * FROM fasting_sessions WHERE endTime IS NULL LIMIT 1")
    suspend fun getActiveFastOnce(): FastingSessionEntity?
    
    @Query("SELECT * FROM fasting_sessions WHERE isCompleted = 1 ORDER BY startTime DESC LIMIT :limit")
    fun getCompletedSessions(limit: Int = 10): Flow<List<FastingSessionEntity>>
    
    @Query("SELECT COUNT(*) FROM fasting_sessions WHERE isCompleted = 1")
    fun getCompletedFastCount(): Flow<Int>
    
    @Query("SELECT * FROM fasting_sessions WHERE startTime >= :startOfDay AND startTime < :endOfDay LIMIT 1")
    suspend fun getFastForDate(startOfDay: Long, endOfDay: Long): FastingSessionEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FastingSessionEntity): Long
    
    @Update
    suspend fun updateSession(session: FastingSessionEntity)
    
    @Delete
    suspend fun deleteSession(session: FastingSessionEntity)
    
    @Query("DELETE FROM fasting_sessions")
    suspend fun deleteAllSessions()
}
