package com.example.calview.core.data.repository

import com.example.calview.core.data.local.MealEntity
import kotlinx.coroutines.flow.Flow

interface MealRepository {
    fun getAllMeals(): Flow<List<MealEntity>>
    fun getMealsForToday(): Flow<List<MealEntity>>
    suspend fun logMeal(meal: MealEntity)
    suspend fun deleteMeal(meal: MealEntity)
}
