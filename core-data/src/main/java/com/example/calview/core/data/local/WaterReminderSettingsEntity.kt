package com.example.calview.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity for water reminder settings.
 * Premium users can customize reminder intervals and active hours.
 */
@Entity(tableName = "water_reminder_settings")
data class WaterReminderSettingsEntity(
    @PrimaryKey val id: Int = 1, // Single row table
    val enabled: Boolean = false,
    val intervalHours: Int = 2, // Reminder every X hours
    val startHour: Int = 8,     // Start reminders at 8 AM
    val endHour: Int = 22,      // Stop reminders at 10 PM
    val dailyGoalMl: Int = 2000,
    val reminderSoundEnabled: Boolean = true,
    val reminderVibrationEnabled: Boolean = true
)
