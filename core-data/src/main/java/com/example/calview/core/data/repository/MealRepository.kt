package com.example.calview.core.data.repository

import com.example.calview.core.data.local.MealEntity
import kotlinx.coroutines.flow.Flow

interface MealRepository {
    fun getAllMeals(): Flow<List<MealEntity>>
    fun getMealsForToday(): Flow<List<MealEntity>>
    fun getMealsForDate(dateString: String): Flow<List<MealEntity>>
    fun getRecentUploads(): Flow<List<MealEntity>>
    suspend fun getMealById(id: Long): MealEntity?
    suspend fun logMeal(meal: MealEntity): Long
    suspend fun updateMeal(meal: MealEntity)
    suspend fun deleteMeal(meal: MealEntity)
    suspend fun deleteMealById(id: Long)
    suspend fun restoreFromCloud(): Boolean
    
    /**
     * Check if user has any meals logged.
     * Used to identify returning users even if isOnboardingComplete is false.
     */
    suspend fun hasAnyMeals(): Boolean
    
    /**
     * Clear all meals from local database.
     * Called during account deletion to remove all local data.
     */
    suspend fun clearAllMeals()
}
