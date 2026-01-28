package com.example.calview.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Exercise type categories for organizing exercises in the database and UI.
 */
enum class ExerciseType {
    CARDIO,      // Running, cycling, swimming, etc.
    STRENGTH,    // Weight training, resistance exercises
    FLEXIBILITY, // Yoga, stretching, pilates
    SPORT,       // Basketball, soccer, tennis, etc.
    OTHER        // Miscellaneous activities
}

/**
 * Room entity for storing logged exercise sessions.
 * Supports both manual entry and AI-parsed exercises.
 */
@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val firestoreId: String = UUID.randomUUID().toString(),
    val name: String = "",
    val type: ExerciseType = ExerciseType.OTHER,
    val durationMinutes: Int = 0,
    val intensity: Float = 0.5f,  // 0.0 (very light) to 1.0 (maximum effort)
    val caloriesBurned: Int = 0,
    val sets: Int? = null,        // For strength training
    val reps: Int? = null,        // For strength training
    val weightKg: Double? = null, // Weight used (for strength) or user weight (for MET calc)
    val distanceKm: Double? = null, // For cardio exercises like running/cycling
    val metValue: Double = 4.0,   // Metabolic Equivalent of Task for calorie calculation
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String? = null,
    val isAiGenerated: Boolean = false,  // True if parsed from natural language
    val fitnessInsight: String? = null   // AI-generated insight about the exercise
)
