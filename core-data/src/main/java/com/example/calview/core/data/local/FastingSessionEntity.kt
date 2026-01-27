package com.example.calview.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a fasting session for intermittent fasting tracking.
 */
@Entity(tableName = "fasting_sessions")
data class FastingSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val firestoreId: String = java.util.UUID.randomUUID().toString(),
    val startTime: Long = 0,                    // Epoch millis when fast started
    val endTime: Long? = null,              // Epoch millis when fast ended (null if ongoing)
    val targetDurationMinutes: Int = 0,         // Target fast duration in minutes
    val fastingType: String = "",                // "16:8", "18:6", "20:4", "Custom"
    val isCompleted: Boolean = false,       // True if user completed the full target
    val notes: String? = null               // Optional user notes
)
