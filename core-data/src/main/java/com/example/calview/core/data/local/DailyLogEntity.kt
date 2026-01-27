package com.example.calview.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity for storing daily activity logs.
 * Each record represents one day's summary of user activity.
 */
@Entity(tableName = "daily_logs")
data class DailyLogEntity(
    @PrimaryKey val date: String = "", // Format: YYYY-MM-DD
    val steps: Int = 0,
    val waterIntake: Int = 0, // in ml
    val weight: Float = 0f, // in kg
    val caloriesConsumed: Int = 0,
    val proteinConsumed: Int = 0,
    val carbsConsumed: Int = 0,
    val fatsConsumed: Int = 0,
    val fiberConsumed: Int = 0,
    val sugarConsumed: Int = 0,
    val sodiumConsumed: Int = 0,
    val caloriesBurned: Int = 0,
    val exerciseMinutes: Int = 0,
    val firestoreId: String = "" // For sync tracking
)
