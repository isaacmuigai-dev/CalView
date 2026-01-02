package com.example.calview.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UserPreferencesRepository {

    private object PreferencesKeys {
        val IS_ONBOARDING_COMPLETE = booleanPreferencesKey("is_onboarding_complete")
        val USER_GOAL = stringPreferencesKey("user_goal")
        val GENDER = stringPreferencesKey("gender")
        val AGE = intPreferencesKey("age")
        val WEIGHT = floatPreferencesKey("weight")
        val HEIGHT = intPreferencesKey("height")
        val RECOMMENDED_CALORIES = intPreferencesKey("recommended_calories")
        val RECOMMENDED_PROTEIN = intPreferencesKey("recommended_protein")
        val RECOMMENDED_CARBS = intPreferencesKey("recommended_carbs")
        val RECOMMENDED_FATS = intPreferencesKey("recommended_fats")
        
        // User profile from Google
        val USER_NAME = stringPreferencesKey("user_name")
        val PHOTO_URL = stringPreferencesKey("photo_url")
        
        // Referral system
        val REFERRAL_CODE = stringPreferencesKey("referral_code")
        val USED_REFERRAL_CODE = stringPreferencesKey("used_referral_code")
        
        // Calorie settings
        val ADD_CALORIES_BACK = booleanPreferencesKey("add_calories_back")
        val ROLLOVER_EXTRA_CALORIES = booleanPreferencesKey("rollover_extra_calories")
        val ROLLOVER_CALORIES_AMOUNT = intPreferencesKey("rollover_calories_amount")
        
        // Appearance settings
        val APPEARANCE_MODE = stringPreferencesKey("appearance_mode")
        
        // Personal details
        val GOAL_WEIGHT = floatPreferencesKey("goal_weight")
        val DAILY_STEPS_GOAL = intPreferencesKey("daily_steps_goal")
        val BIRTH_MONTH = stringPreferencesKey("birth_month")
        val BIRTH_DAY = intPreferencesKey("birth_day")
        val BIRTH_YEAR = intPreferencesKey("birth_year")
    }

    override val isOnboardingComplete: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_ONBOARDING_COMPLETE] ?: false
    }

    override val userGoal: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_GOAL] ?: ""
    }

    override val gender: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.GENDER] ?: ""
    }

    override val age: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AGE] ?: 0
    }

    override val weight: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.WEIGHT] ?: 0f
    }

    override val height: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HEIGHT] ?: 0
    }

    override val recommendedCalories: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.RECOMMENDED_CALORIES] ?: 1705
    }

    override val recommendedProtein: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.RECOMMENDED_PROTEIN] ?: 117
    }

    override val recommendedCarbs: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.RECOMMENDED_CARBS] ?: 203
    }

    override val recommendedFats: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.RECOMMENDED_FATS] ?: 47
    }
    
    // User profile from Google sign-in
    override val userName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_NAME] ?: ""
    }
    
    override val photoUrl: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.PHOTO_URL] ?: ""
    }
    
    // Referral system - generate unique code if not exists
    override val referralCode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.REFERRAL_CODE] ?: ""
    }
    
    override val usedReferralCode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USED_REFERRAL_CODE] ?: ""
    }
    
    // Calorie settings for dashboard
    override val addCaloriesBack: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ADD_CALORIES_BACK] ?: false
    }
    
    override val rolloverExtraCalories: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ROLLOVER_EXTRA_CALORIES] ?: false
    }
    
    override val rolloverCaloriesAmount: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ROLLOVER_CALORIES_AMOUNT] ?: 0
    }
    
    override val maxRolloverCalories: Int = 200 // Fixed value as per design
    
    // Appearance settings
    override val appearanceMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.APPEARANCE_MODE] ?: "automatic"
    }
    
    // Personal details
    override val goalWeight: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.GOAL_WEIGHT] ?: 0f
    }
    
    override val dailyStepsGoal: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DAILY_STEPS_GOAL] ?: 10000
    }
    
    override val birthMonth: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.BIRTH_MONTH] ?: "January"
    }
    
    override val birthDay: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.BIRTH_DAY] ?: 1
    }
    
    override val birthYear: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.BIRTH_YEAR] ?: 2000
    }

    override suspend fun setOnboardingComplete(complete: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_ONBOARDING_COMPLETE] = complete
        }
    }

    override suspend fun saveUserProfile(
        goal: String,
        gender: String,
        age: Int,
        weight: Float,
        height: Int
    ) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_GOAL] = goal
            preferences[PreferencesKeys.GENDER] = gender
            preferences[PreferencesKeys.AGE] = age
            preferences[PreferencesKeys.WEIGHT] = weight
            preferences[PreferencesKeys.HEIGHT] = height
        }
    }

    override suspend fun saveRecommendedMacros(calories: Int, protein: Int, carbs: Int, fats: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.RECOMMENDED_CALORIES] = calories
            preferences[PreferencesKeys.RECOMMENDED_PROTEIN] = protein
            preferences[PreferencesKeys.RECOMMENDED_CARBS] = carbs
            preferences[PreferencesKeys.RECOMMENDED_FATS] = fats
        }
    }
    
    override suspend fun setUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_NAME] = name
        }
    }
    
    override suspend fun setPhotoUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PHOTO_URL] = url
        }
    }
    
    override suspend fun setReferralCode(code: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REFERRAL_CODE] = code
        }
    }
    
    override suspend fun setUsedReferralCode(code: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USED_REFERRAL_CODE] = code
        }
    }
    
    override suspend fun setAddCaloriesBack(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ADD_CALORIES_BACK] = enabled
        }
    }
    
    override suspend fun setRolloverExtraCalories(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ROLLOVER_EXTRA_CALORIES] = enabled
        }
    }
    
    override suspend fun setRolloverCaloriesAmount(amount: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ROLLOVER_CALORIES_AMOUNT] = amount.coerceAtMost(200)
        }
    }
    
    // Appearance settings setter
    override suspend fun setAppearanceMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.APPEARANCE_MODE] = mode
        }
    }
    
    // Personal details setters
    override suspend fun setGoalWeight(weight: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GOAL_WEIGHT] = weight
        }
    }
    
    override suspend fun setDailyStepsGoal(steps: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_STEPS_GOAL] = steps
        }
    }
    
    override suspend fun setBirthDate(month: String, day: Int, year: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BIRTH_MONTH] = month
            preferences[PreferencesKeys.BIRTH_DAY] = day
            preferences[PreferencesKeys.BIRTH_YEAR] = year
        }
    }
    
    override suspend fun setGender(gender: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GENDER] = gender
        }
    }
    
    override suspend fun setWeight(weight: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WEIGHT] = weight
        }
    }
    
    override suspend fun setHeight(heightCm: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HEIGHT] = heightCm
        }
    }
    
    companion object {
        /**
         * Generate a unique 6-character referral code
         */
        fun generateReferralCode(): String {
            val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
            return (1..6).map { chars[Random.nextInt(chars.length)] }.joinToString("")
        }
    }
}
