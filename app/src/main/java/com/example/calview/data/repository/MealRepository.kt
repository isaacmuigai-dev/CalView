package com.example.calview.data.repository

import com.example.calview.data.local.MealEntity
import kotlinx.coroutines.flow.Flow

interface MealRepository {
    fun getAllMeals(): Flow<List<MealEntity>>
    fun getMealsForToday(): Flow<List<MealEntity>>
    fun getMealsForDate(startOfDay: Long, endOfDay: Long): Flow<List<MealEntity>>
    suspend fun logMeal(meal: MealEntity)
    suspend fun deleteMeal(meal: MealEntity)
}
