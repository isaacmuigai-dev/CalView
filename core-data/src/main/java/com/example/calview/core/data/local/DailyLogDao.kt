package com.example.calview.core.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for DailyLog operations.
 */
@Dao
interface DailyLogDao {
    
    @Query("SELECT * FROM daily_logs WHERE date = :date")
    fun getLogForDate(date: String): Flow<DailyLogEntity?>
    
    @Query("SELECT * FROM daily_logs WHERE date = :date")
    suspend fun getLogForDateSync(date: String): DailyLogEntity?
    
    @Query("SELECT * FROM daily_logs ORDER BY date DESC")
    fun getAllLogs(): Flow<List<DailyLogEntity>>
    
    @Query("SELECT * FROM daily_logs WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getLogsForRange(startDate: String, endDate: String): Flow<List<DailyLogEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(log: DailyLogEntity)
    
    @Update
    suspend fun update(log: DailyLogEntity)
    
    @Query("UPDATE daily_logs SET steps = :steps WHERE date = :date")
    suspend fun updateSteps(date: String, steps: Int)
    
    @Query("UPDATE daily_logs SET waterIntake = :water WHERE date = :date")
    suspend fun updateWater(date: String, water: Int)
    
    @Query("UPDATE daily_logs SET weight = :weight WHERE date = :date")
    suspend fun updateWeight(date: String, weight: Float)
    
    @Delete
    suspend fun delete(log: DailyLogEntity)

    @Query("DELETE FROM daily_logs")
    suspend fun deleteAll()
}
