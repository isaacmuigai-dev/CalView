package com.example.calview.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity for tracking weight history over time.
 * Each entry represents a weight measurement at a specific date.
 */
@Entity(tableName = "weight_history")
data class WeightHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weight: Float,           // Weight in kg
    val timestamp: Long = System.currentTimeMillis(),  // When this weight was recorded
    val note: String? = null     // Optional note (e.g., "Morning weight", "After workout")
)
