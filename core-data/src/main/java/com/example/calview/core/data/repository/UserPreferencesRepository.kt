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
    val weightChangePerWeek: Flow<Float>
    
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
    
    // Language settings
    val language: Flow<String> // "en", "de", etc. (lowercase)
    
    // Appearance settings
    val appearanceMode: Flow<String>  // "light", "dark", "automatic"
    
    // Personal details
    val goalWeight: Flow<Float> // Target weight in kg
    val startWeight: Flow<Float> // Starting weight when goal was set
    val dailyStepsGoal: Flow<Int>  // Daily step target (default 10000)
    val birthMonth: Flow<String>  // "January", "February", etc.
    val birthDay: Flow<Int>  // 1-31
    val birthYear: Flow<Int>  // 1940-2010

    // Widget Data
    val waterConsumed: Flow<Int>
    val waterDate: Flow<Long> // Timestamp of last water update
    val lastKnownSteps: Flow<Int> // Cached steps for widget
    
    // Camera Tutorial
    val hasSeenCameraTutorial: Flow<Boolean> // True if user has seen camera best practices
    
    // Widget Theme
    val widgetDarkTheme: Flow<Boolean> // True if widget should use dark theme

    // Walkthrough flags
    val hasSeenDashboardWalkthrough: Flow<Boolean>
    val hasSeenProgressWalkthrough: Flow<Boolean>
    val hasSeenFeatureIntro: Flow<Boolean>
    
    // Gamification
    val userXp: Flow<Int>
    val userLevel: Flow<Int>
    
    // Activity tracking
    val lastActivityTimestamp: Flow<Long>
    val recordBurn: Flow<Int>

    
    // Smart Coach tracking
    val coachLastMessageTime: Flow<Long>
    val coachMessageCountToday: Flow<Int>
    val coachLastMessageDate: Flow<String>
    val lastNotifiedGoalWeight: Flow<Float>
    val lastNotifiedDailyGoalDate: Flow<String> // ISO_LOCAL_DATE of last notification
    val notifiedDailyGoalFlags: Flow<String>   // Comma-separated flags like "calories,protein,carbs,fats,steps,burned"



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
    
    // Rollover date tracking
    val lastRolloverDate: Flow<Long>
    suspend fun setLastRolloverDate(timestamp: Long)
    
    // Language settings method
    suspend fun setLanguage(code: String)
    
    // Appearance settings method
    suspend fun setAppearanceMode(mode: String)
    
    // Personal details methods
    suspend fun setGoalWeight(weight: Float)
    suspend fun setStartWeight(weight: Float)
    suspend fun setDailyStepsGoal(steps: Int)
    suspend fun setBirthDate(month: String, day: Int, year: Int)
    suspend fun setGender(gender: String)
    suspend fun setWeight(weight: Float)
    suspend fun setHeight(heightCm: Int)
    suspend fun setWeightChangePerWeek(pace: Float)
    
    // Widget Data methods
    val waterServingSize: Flow<Int> // Default 250 ml
    suspend fun setWaterServingSize(ml: Int)
    suspend fun setWaterConsumed(amount: Int, dateTimestamp: Long)
    suspend fun setLastKnownSteps(steps: Int)
    suspend fun setActivityStats(caloriesBurned: Int, weeklyBurn: Int, recordBurn: Int)
    
    // Camera Tutorial methods
    suspend fun setHasSeenCameraTutorial(seen: Boolean)
    
    // Widget Theme methods
    suspend fun setWidgetDarkTheme(isDark: Boolean)
    
    // Walkthrough methods
    suspend fun setHasSeenDashboardWalkthrough(seen: Boolean)
    suspend fun setHasSeenProgressWalkthrough(seen: Boolean)
    suspend fun setHasSeenFeatureIntro(seen: Boolean)
    
    suspend fun setUserXp(xp: Int)
    suspend fun setUserLevel(level: Int)
    
    suspend fun setLastActivityTimestamp(timestamp: Long)
    
    suspend fun setCoachMessageTracking(timestamp: Long, count: Int, date: String)
    suspend fun setLastNotifiedGoalWeight(weight: Float)
    suspend fun setDailyGoalNotified(date: String, flag: String)
    suspend fun clearDailyGoalNotifications()
    

    
    /**
     * Restore user data from Firestore cloud storage
     * Should be called on app start if user is signed in
     */
    suspend fun restoreFromCloud(): Boolean
    
    /**
     * Sync widget data to SharedPreferences for widget access
     * Widget cannot use DataStore (causes multiple instance conflict)
     * Should be called when macros, steps, or water change
     */
    suspend fun syncWidgetData()
    
    /**
     * Clear all local user data.
     * Called during account deletion to remove all local preferences.
     */
    suspend fun clearAllData()
    
    /**
     * Explicitly sync all user data to Firestore cloud.
     * Should be called before logout to ensure data is persisted.
     */
    suspend fun syncToCloud()
}

