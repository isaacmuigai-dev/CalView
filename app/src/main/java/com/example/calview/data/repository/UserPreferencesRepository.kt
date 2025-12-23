package com.example.calview.data.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val isOnboardingComplete: Flow<Boolean>
    val userGoal: Flow<String>
    val gender: Flow<String>
    val age: Flow<Int>
    val weight: Flow<Float>
    val height: Flow<Int>
    val recommendedCalories: Flow<Int>
    val recommendedProtein: Flow<Int>
    val recommendedCarbs: Flow<Int>
    val recommendedFats: Flow<Int>

    suspend fun setOnboardingComplete(complete: Boolean)
    suspend fun saveUserProfile(
        goal: String,
        gender: String,
        age: Int,
        weight: Float,
        height: Int
    )
    suspend fun saveRecommendedMacros(calories: Int, protein: Int, carbs: Int, fats: Int)
}
