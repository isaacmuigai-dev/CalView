package com.example.calview.core.data.repository

import com.example.calview.core.data.local.DailyLogEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for daily log operations.
 */
interface DailyLogRepository {
    
    /**
     * Get log for a specific date.
     */
    fun getLogForDate(date: String): Flow<DailyLogEntity?>
    
    /**
     * Get log for a specific date (synchronous).
     */
    suspend fun getLogForDateSync(date: String): DailyLogEntity?
    
    /**
     * Get all logs.
     */
    fun getAllLogs(): Flow<List<DailyLogEntity>>
    
    /**
     * Get logs for a date range.
     */
    fun getLogsForRange(startDate: String, endDate: String): Flow<List<DailyLogEntity>>
    
    /**
     * Insert or update a daily log.
     */
    suspend fun saveLog(log: DailyLogEntity)
    
    /**
     * Update steps for a date.
     */
    suspend fun updateSteps(date: String, steps: Int)
    
    /**
     * Update water intake for a date.
     */
    suspend fun updateWater(date: String, water: Int)
    
    /**
     * Update weight for a date.
     */
    suspend fun updateWeight(date: String, weight: Float)
    
    /**
     * Get or create today's log.
     */
    suspend fun getOrCreateTodayLog(): DailyLogEntity
    
    /**
     * Sync all daily logs to cloud.
     */
    suspend fun syncToCloud()
    
    /**
     * Restore daily logs from cloud.
     */
    suspend fun restoreFromCloud(): Boolean

    /**
     * Clear all daily logs from local database.
     */
    suspend fun clearAllLogs()
}
