package com.example.calview.core.data.repository

import com.example.calview.core.data.local.WaterReminderDao
import com.example.calview.core.data.local.WaterReminderSettingsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for water reminder settings.
 * Premium feature for scheduled hydration reminders.
 */
@Singleton
class WaterReminderRepository @Inject constructor(
    private val waterReminderDao: WaterReminderDao
) {
    
    /**
     * Observe water reminder settings
     */
    fun observeSettings(): Flow<WaterReminderSettingsEntity> {
        return waterReminderDao.observeSettings().map { it ?: getDefaultSettings() }
    }
    
    /**
     * Get current settings or defaults
     */
    suspend fun getSettings(): WaterReminderSettingsEntity {
        return waterReminderDao.getSettings() ?: getDefaultSettings()
    }
    
    /**
     * Save all settings
     */
    suspend fun saveSettings(settings: WaterReminderSettingsEntity) {
        waterReminderDao.saveSettings(settings)
    }
    
    /**
     * Toggle reminders on/off
     */
    suspend fun setEnabled(enabled: Boolean) {
        val current = getSettings()
        waterReminderDao.saveSettings(current.copy(enabled = enabled))
    }
    
    /**
     * Set reminder interval in hours
     */
    suspend fun setInterval(hours: Int) {
        val current = getSettings()
        waterReminderDao.saveSettings(current.copy(intervalHours = hours.coerceIn(1, 6)))
    }
    
    /**
     * Set active hours for reminders
     */
    suspend fun setActiveHours(startHour: Int, endHour: Int) {
        val current = getSettings()
        waterReminderDao.saveSettings(
            current.copy(
                startHour = startHour.coerceIn(0, 23),
                endHour = endHour.coerceIn(0, 23)
            )
        )
    }
    
    /**
     * Set daily water goal in ml
     */
    suspend fun setDailyGoal(goalMl: Int) {
        val current = getSettings()
        waterReminderDao.saveSettings(current.copy(dailyGoalMl = goalMl.coerceIn(500, 5000)))
    }
    
    /**
     * Check if reminders are enabled
     */
    suspend fun isEnabled(): Boolean {
        return getSettings().enabled
    }
    
    private fun getDefaultSettings(): WaterReminderSettingsEntity {
        return WaterReminderSettingsEntity(
            id = 1,
            enabled = false,
            intervalHours = 2,
            startHour = 8,
            endHour = 22,
            dailyGoalMl = 2000,
            reminderSoundEnabled = true,
            reminderVibrationEnabled = true
        )
    }

    suspend fun clearAllData() {
        waterReminderDao.deleteAll()
    }
}
