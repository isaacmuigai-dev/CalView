package com.example.calview.core.data.repository

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
    
    // User profile from Google sign-in
    val userName: Flow<String>
    val photoUrl: Flow<String>
    
    // Referral system
    val referralCode: Flow<String>  // User's unique promo code
    val usedReferralCode: Flow<String>  // Referral code user entered during onboarding
    
    // Calorie settings for dashboard
    val addCaloriesBack: Flow<Boolean>
    val rolloverExtraCalories: Flow<Boolean>
    val rolloverCaloriesAmount: Flow<Int>  // Yesterday's leftover calories (max 200)
    val maxRolloverCalories: Int // Always 200 as per design
    
    // Appearance settings
    val appearanceMode: Flow<String>  // "light", "dark", "automatic"
    
    // Personal details
    val goalWeight: Flow<Float>  // Target weight in lbs
    val dailyStepsGoal: Flow<Int>  // Daily step target (default 10000)
    val birthMonth: Flow<String>  // "January", "February", etc.
    val birthDay: Flow<Int>  // 1-31
    val birthYear: Flow<Int>  // 1940-2010

    suspend fun setOnboardingComplete(complete: Boolean)
    suspend fun saveUserProfile(
        goal: String,
        gender: String,
        age: Int,
        weight: Float,
        height: Int
    )
    suspend fun saveRecommendedMacros(calories: Int, protein: Int, carbs: Int, fats: Int)
    
    // User profile methods
    suspend fun setUserName(name: String)
    suspend fun setPhotoUrl(url: String)
    
    // Referral methods
    suspend fun setReferralCode(code: String)
    suspend fun setUsedReferralCode(code: String)
    
    // Calorie settings methods
    suspend fun setAddCaloriesBack(enabled: Boolean)
    suspend fun setRolloverExtraCalories(enabled: Boolean)
    suspend fun setRolloverCaloriesAmount(amount: Int)
    
    // Appearance settings method
    suspend fun setAppearanceMode(mode: String)
    
    // Personal details methods
    suspend fun setGoalWeight(weight: Float)
    suspend fun setDailyStepsGoal(steps: Int)
    suspend fun setBirthDate(month: String, day: Int, year: Int)
    suspend fun setGender(gender: String)
    suspend fun setWeight(weight: Float)
    suspend fun setHeight(heightCm: Int)
}

