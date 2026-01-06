package com.example.calview.core.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.calview.core.data.model.UserData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val firestoreRepository: FirestoreRepository
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

        // Widget Data
        val WATER_CONSUMED = intPreferencesKey("water_consumed")
        val WATER_DATE = longPreferencesKey("water_date")
        val LAST_KNOWN_STEPS = intPreferencesKey("last_known_steps")
        
        // Camera Tutorial
        val HAS_SEEN_CAMERA_TUTORIAL = booleanPreferencesKey("has_seen_camera_tutorial")
    }
    
    // Coroutine scope for background sync operations
    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    /**
     * Helper method to get current user data from DataStore
     */
    private suspend fun getCurrentUserData(): UserData {
        val preferences = context.dataStore.data.first()
        return UserData(
            userId = authRepository.getUserId(),
            isOnboardingComplete = preferences[PreferencesKeys.IS_ONBOARDING_COMPLETE] ?: false,
            userGoal = preferences[PreferencesKeys.USER_GOAL] ?: "",
            gender = preferences[PreferencesKeys.GENDER] ?: "",
            age = preferences[PreferencesKeys.AGE] ?: 0,
            weight = preferences[PreferencesKeys.WEIGHT] ?: 0f,
            height = preferences[PreferencesKeys.HEIGHT] ?: 0,
            recommendedCalories = preferences[PreferencesKeys.RECOMMENDED_CALORIES] ?: 1705,
            recommendedProtein = preferences[PreferencesKeys.RECOMMENDED_PROTEIN] ?: 117,
            recommendedCarbs = preferences[PreferencesKeys.RECOMMENDED_CARBS] ?: 203,
            recommendedFats = preferences[PreferencesKeys.RECOMMENDED_FATS] ?: 47,
            userName = preferences[PreferencesKeys.USER_NAME] ?: "",
            photoUrl = preferences[PreferencesKeys.PHOTO_URL] ?: "",
            referralCode = preferences[PreferencesKeys.REFERRAL_CODE] ?: "",
            usedReferralCode = preferences[PreferencesKeys.USED_REFERRAL_CODE] ?: "",
            addCaloriesBack = preferences[PreferencesKeys.ADD_CALORIES_BACK] ?: false,
            rolloverExtraCalories = preferences[PreferencesKeys.ROLLOVER_EXTRA_CALORIES] ?: false,
            rolloverCaloriesAmount = preferences[PreferencesKeys.ROLLOVER_CALORIES_AMOUNT] ?: 0,
            appearanceMode = preferences[PreferencesKeys.APPEARANCE_MODE] ?: "light",
            goalWeight = preferences[PreferencesKeys.GOAL_WEIGHT] ?: 0f,
            dailyStepsGoal = preferences[PreferencesKeys.DAILY_STEPS_GOAL] ?: 10000,
            birthMonth = preferences[PreferencesKeys.BIRTH_MONTH] ?: "January",
            birthDay = preferences[PreferencesKeys.BIRTH_DAY] ?: 1,
            birthYear = preferences[PreferencesKeys.BIRTH_YEAR] ?: 2000
        )
    }
    
    /**
     * Sync current data to Firestore (fire-and-forget)
     */
    private fun syncToFirestore() {
        val userId = authRepository.getUserId()
        Log.d("FirestoreSync", "syncToFirestore called. userId=$userId, isSignedIn=${authRepository.isSignedIn()}")
        if (userId.isNotEmpty()) {
            syncScope.launch {
                try {
                    val userData = getCurrentUserData()
                    Log.d("FirestoreSync", "Saving user data: $userData")
                    firestoreRepository.saveUserData(userId, userData)
                    Log.d("FirestoreSync", "Successfully saved user data to Firestore")
                } catch (e: Exception) {
                    Log.e("FirestoreSync", "Error syncing to Firestore", e)
                    e.printStackTrace()
                }
            }
        } else {
            Log.w("FirestoreSync", "Skipping sync - user not signed in")
        }
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
        preferences[PreferencesKeys.APPEARANCE_MODE] ?: "light"
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

    override val waterConsumed: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.WATER_CONSUMED] ?: 0
    }

    override val waterDate: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.WATER_DATE] ?: 0L
    }

    override val lastKnownSteps: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LAST_KNOWN_STEPS] ?: 0
    }

    override val hasSeenCameraTutorial: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HAS_SEEN_CAMERA_TUTORIAL] ?: false
    }

    override suspend fun setOnboardingComplete(complete: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_ONBOARDING_COMPLETE] = complete
        }
        syncToFirestore()
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
        syncToFirestore()
    }

    override suspend fun saveRecommendedMacros(calories: Int, protein: Int, carbs: Int, fats: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.RECOMMENDED_CALORIES] = calories
            preferences[PreferencesKeys.RECOMMENDED_PROTEIN] = protein
            preferences[PreferencesKeys.RECOMMENDED_CARBS] = carbs
            preferences[PreferencesKeys.RECOMMENDED_FATS] = fats
        }
        syncToFirestore()
    }
    
    override suspend fun setUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_NAME] = name
        }
        syncToFirestore()
    }
    
    override suspend fun setPhotoUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PHOTO_URL] = url
        }
        syncToFirestore()
    }
    
    override suspend fun setReferralCode(code: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REFERRAL_CODE] = code
        }
        syncToFirestore()
    }
    
    override suspend fun setUsedReferralCode(code: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USED_REFERRAL_CODE] = code
        }
        syncToFirestore()
    }
    
    override suspend fun setAddCaloriesBack(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ADD_CALORIES_BACK] = enabled
        }
        syncToFirestore()
    }
    
    override suspend fun setRolloverExtraCalories(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ROLLOVER_EXTRA_CALORIES] = enabled
        }
        syncToFirestore()
    }
    
    override suspend fun setRolloverCaloriesAmount(amount: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ROLLOVER_CALORIES_AMOUNT] = amount.coerceAtMost(200)
        }
        syncToFirestore()
    }
    
    // Appearance settings setter
    override suspend fun setAppearanceMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.APPEARANCE_MODE] = mode
        }
        syncToFirestore()
    }
    
    // Personal details setters
    override suspend fun setGoalWeight(weight: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GOAL_WEIGHT] = weight
        }
        syncToFirestore()
    }
    
    override suspend fun setDailyStepsGoal(steps: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_STEPS_GOAL] = steps
        }
        syncToFirestore()
    }
    
    override suspend fun setBirthDate(month: String, day: Int, year: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BIRTH_MONTH] = month
            preferences[PreferencesKeys.BIRTH_DAY] = day
            preferences[PreferencesKeys.BIRTH_YEAR] = year
        }
        syncToFirestore()
    }
    
    override suspend fun setGender(gender: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GENDER] = gender
        }
        syncToFirestore()
    }
    
    override suspend fun setWeight(weight: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WEIGHT] = weight
        }
        syncToFirestore()
    }
    
    override suspend fun setHeight(heightCm: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HEIGHT] = heightCm
        }
        syncToFirestore()
    }

    override suspend fun setWaterConsumed(amount: Int, dateTimestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WATER_CONSUMED] = amount
            preferences[PreferencesKeys.WATER_DATE] = dateTimestamp
        }
    }

    override suspend fun setLastKnownSteps(steps: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_KNOWN_STEPS] = steps
        }
    }

    override suspend fun setHasSeenCameraTutorial(seen: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_SEEN_CAMERA_TUTORIAL] = seen
        }
    }
    
    /**
     * Restore user data from Firestore cloud storage
     * Returns true if data was restored, false otherwise
     */
    override suspend fun restoreFromCloud(): Boolean {
        val userId = authRepository.getUserId()
        if (userId.isEmpty()) {
            return false
        }
        
        return try {
            val userData = firestoreRepository.getUserData(userId)
            if (userData != null) {
                // Update local DataStore with cloud data
                context.dataStore.edit { preferences ->
                    preferences[PreferencesKeys.IS_ONBOARDING_COMPLETE] = userData.isOnboardingComplete
                    preferences[PreferencesKeys.USER_GOAL] = userData.userGoal
                    preferences[PreferencesKeys.GENDER] = userData.gender
                    preferences[PreferencesKeys.AGE] = userData.age
                    preferences[PreferencesKeys.WEIGHT] = userData.weight
                    preferences[PreferencesKeys.HEIGHT] = userData.height
                    preferences[PreferencesKeys.RECOMMENDED_CALORIES] = userData.recommendedCalories
                    preferences[PreferencesKeys.RECOMMENDED_PROTEIN] = userData.recommendedProtein
                    preferences[PreferencesKeys.RECOMMENDED_CARBS] = userData.recommendedCarbs
                    preferences[PreferencesKeys.RECOMMENDED_FATS] = userData.recommendedFats
                    preferences[PreferencesKeys.USER_NAME] = userData.userName
                    preferences[PreferencesKeys.PHOTO_URL] = userData.photoUrl
                    preferences[PreferencesKeys.REFERRAL_CODE] = userData.referralCode
                    preferences[PreferencesKeys.USED_REFERRAL_CODE] = userData.usedReferralCode
                    preferences[PreferencesKeys.ADD_CALORIES_BACK] = userData.addCaloriesBack
                    preferences[PreferencesKeys.ROLLOVER_EXTRA_CALORIES] = userData.rolloverExtraCalories
                    preferences[PreferencesKeys.ROLLOVER_CALORIES_AMOUNT] = userData.rolloverCaloriesAmount
                    preferences[PreferencesKeys.APPEARANCE_MODE] = userData.appearanceMode
                    preferences[PreferencesKeys.GOAL_WEIGHT] = userData.goalWeight
                    preferences[PreferencesKeys.DAILY_STEPS_GOAL] = userData.dailyStepsGoal
                    preferences[PreferencesKeys.BIRTH_MONTH] = userData.birthMonth
                    preferences[PreferencesKeys.BIRTH_DAY] = userData.birthDay
                    preferences[PreferencesKeys.BIRTH_YEAR] = userData.birthYear
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
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
