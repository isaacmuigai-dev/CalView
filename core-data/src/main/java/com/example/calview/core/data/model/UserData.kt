package com.example.calview.core.data.model

/**
 * Data class representing user preferences and profile data
 * Used for Firestore serialization
 */
data class UserData(
    val userId: String = "",
    
    // Onboarding
    val isOnboardingComplete: Boolean = false,
    
    // User profile
    val userGoal: String = "",
    val gender: String = "",
    val age: Int = 0,
    val weight: Float = 0f,
    val height: Int = 0,
    
    // Recommended macros
    val recommendedCalories: Int = 1705,
    val recommendedProtein: Int = 117,
    val recommendedCarbs: Int = 203,
    val recommendedFats: Int = 47,
    
    // User profile from Google
    val userName: String = "",
    val photoUrl: String = "",
    
    // Referral system
    val referralCode: String = "",
    val usedReferralCode: String = "",
    
    // Calorie settings
    val addCaloriesBack: Boolean = false,
    val rolloverExtraCalories: Boolean = false,
    val rolloverCaloriesAmount: Int = 0,
    
    // Appearance settings
    val appearanceMode: String = "light",
    
    // Personal details
    val goalWeight: Float = 0f,
    val dailyStepsGoal: Int = 10000,
    val birthMonth: String = "January",
    val birthDay: Int = 1,
    val birthYear: Int = 2000,
    
    // Localization
    val language: String = "en",
    val lastRolloverDate: Long = 0L,
    val hasSeenDashboardWalkthrough: Boolean = false,
    val hasSeenFeatureIntro: Boolean = false,
    
    // Missing fields for complete backup
    val waterConsumed: Int = 0,
    val waterDate: Long = 0L,
    val lastKnownSteps: Int = 0,
    val lastKnownCaloriesBurned: Int = 0,
    val weeklyBurn: Int = 0,
    val recordBurn: Int = 0,
    val hasSeenCameraTutorial: Boolean = false,
    val widgetDarkTheme: Boolean = false,
    val weightChangePerWeek: Float = 0.5f
)
