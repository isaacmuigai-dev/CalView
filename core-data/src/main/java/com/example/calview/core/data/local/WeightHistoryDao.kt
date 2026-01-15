package com.example.calview.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for weight history operations.
 */
@Dao
interface WeightHistoryDao {
    
    /**
     * Insert a new weight entry
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeight(entry: WeightHistoryEntity): Long
    
    /**
     * Get all weight history entries ordered by timestamp (newest first)
     */
    @Query("SELECT * FROM weight_history ORDER BY timestamp DESC")
    fun getAllWeightHistory(): Flow<List<WeightHistoryEntity>>
    
    /**
     * Get weight history for the last N days
     */
    @Query("SELECT * FROM weight_history WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getWeightHistorySince(startTime: Long): Flow<List<WeightHistoryEntity>>
    
    /**
     * Get the most recent weight entry
     */
    @Query("SELECT * FROM weight_history ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestWeight(): WeightHistoryEntity?
    
    /**
     * Get weight entries for a specific date range
     */
    @Query("SELECT * FROM weight_history WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp ASC")
    fun getWeightHistoryBetween(startTime: Long, endTime: Long): Flow<List<WeightHistoryEntity>>
    
    /**
     * Delete all weight history (for account deletion)
     */
    @Query("DELETE FROM weight_history")
    suspend fun deleteAll()
    
    /**
     * Get count of weight entries
     */
    @Query("SELECT COUNT(*) FROM weight_history")
    suspend fun getCount(): Int
}
