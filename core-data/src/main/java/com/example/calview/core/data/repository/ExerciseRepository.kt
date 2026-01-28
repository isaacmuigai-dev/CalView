package com.example.calview.core.data.repository

import com.example.calview.core.data.local.ExerciseEntity
import com.example.calview.core.data.local.ExerciseType
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    fun getAllExercises(): Flow<List<ExerciseEntity>>
    fun getExercisesForToday(): Flow<List<ExerciseEntity>>
    fun getExercisesForDate(dateString: String): Flow<List<ExerciseEntity>>
    fun getTotalCaloriesBurnedForDate(dateString: String): Flow<Int>
    fun getRecentExercises(limit: Int = 10): Flow<List<ExerciseEntity>>
    fun getExercisesByType(type: ExerciseType): Flow<List<ExerciseEntity>>
    
    suspend fun getExerciseById(id: Long): ExerciseEntity?
    suspend fun logExercise(exercise: ExerciseEntity): Long
    suspend fun updateExercise(exercise: ExerciseEntity)
    suspend fun deleteExercise(exercise: ExerciseEntity)
    suspend fun deleteExerciseById(id: Long)
    suspend fun restoreFromCloud(): Boolean
    suspend fun clearAllExercises()
    suspend fun hasAnyExercises(): Boolean
    
    /**
     * Calculate calories burned using MET formula.
     * Calories = MET × weight(kg) × duration(hours)
     */
    fun calculateCaloriesBurned(metValue: Double, weightKg: Double, durationMinutes: Int): Int
}
