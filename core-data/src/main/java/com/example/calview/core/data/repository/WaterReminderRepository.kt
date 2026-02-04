package com.example.calview.core.data.repository

import com.example.calview.core.data.local.WaterReminderDao
import com.example.calview.core.data.local.WaterReminderSettingsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.launch


/**
 * Repository for water reminder settings.
 * Premium feature for scheduled hydration reminders.
 */
@Singleton
class WaterReminderRepository @Inject constructor(
    private val waterReminderDao: WaterReminderDao,
    private val firestoreRepository: FirestoreRepository,
    private val authRepository: AuthRepository
) {
    
    // Scope for background sync operations
    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.SupervisorJob())

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
        syncToCloud(settings)
    }
    
    /**
     * Toggle reminders on/off
     */
    suspend fun setEnabled(enabled: Boolean) {
        val current = getSettings()
        saveSettings(current.copy(enabled = enabled))
    }
    
    /**
     * Set reminder interval in hours
     */
    suspend fun setInterval(hours: Int) {
        val current = getSettings()
        saveSettings(current.copy(intervalHours = hours.coerceIn(1, 6)))
    }
    
    /**
     * Set active hours for reminders
     */
    suspend fun setActiveHours(startHour: Int, endHour: Int) {
        val current = getSettings()
        saveSettings(
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
        saveSettings(current.copy(dailyGoalMl = goalMl.coerceIn(500, 5000)))
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
            dailyGoalMl = 2500,
            reminderSoundEnabled = true,
            reminderVibrationEnabled = true
        )
    }

    suspend fun clearAllData() {
        waterReminderDao.deleteAll()
    }
    
    private fun syncToCloud(settings: WaterReminderSettingsEntity) {
        scope.launch {
            val userId = authRepository.getUserId()
            if (userId.isEmpty()) return@launch
            
            try {
                firestoreRepository.saveWaterSettings(userId, settings)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    suspend fun restoreFromCloud(): Boolean {
        val userId = authRepository.getUserId()
        if (userId.isEmpty()) return false
        
        return try {
            val cloudSettings = firestoreRepository.getWaterSettings(userId)
            if (cloudSettings != null) {
                // Keep local ID 1
                waterReminderDao.saveSettings(cloudSettings.copy(id = 1))
                android.util.Log.d("WaterReminderRepo", "Restored water settings from cloud")
                true
            } else {
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("WaterReminderRepo", "Error restoring water settings", e)
            false
        }
    }
}
