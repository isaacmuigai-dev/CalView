package com.example.calview.core.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO for water reminder settings.
 */
@Dao
interface WaterReminderDao {
    
    @Query("SELECT * FROM water_reminder_settings WHERE id = 1")
    fun observeSettings(): Flow<WaterReminderSettingsEntity?>
    
    @Query("SELECT * FROM water_reminder_settings WHERE id = 1")
    suspend fun getSettings(): WaterReminderSettingsEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: WaterReminderSettingsEntity)
    
    @Query("UPDATE water_reminder_settings SET enabled = :enabled WHERE id = 1")
    suspend fun setEnabled(enabled: Boolean)
    
    @Query("UPDATE water_reminder_settings SET intervalHours = :hours WHERE id = 1")
    suspend fun setInterval(hours: Int)
}
