package com.example.calview.core.data.repository

import android.content.Context
import android.content.Intent
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Month
import java.time.Period
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random
import com.example.calview.core.data.local.WeightHistoryDao
import com.example.calview.core.data.local.WeightHistoryEntity

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val firestoreRepository: FirestoreRepository,
    private val weightHistoryDao: WeightHistoryDao
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
        val LAST_ROLLOVER_DATE = longPreferencesKey("last_rollover_date")
        
        // Appearance settings
        val APPEARANCE_MODE = stringPreferencesKey("appearance_mode")
        
        // Language settings
        val LANGUAGE = stringPreferencesKey("language")
        
        // Personal details
        val GOAL_WEIGHT = floatPreferencesKey("goal_weight")
        val START_WEIGHT = floatPreferencesKey("start_weight")
        val DAILY_STEPS_GOAL = intPreferencesKey("daily_steps_goal")
        val BIRTH_MONTH = stringPreferencesKey("birth_month")
        val BIRTH_DAY = intPreferencesKey("birth_day")
        val BIRTH_YEAR = intPreferencesKey("birth_year")

        // Widget Data
        val WATER_CONSUMED = intPreferencesKey("water_consumed")
        val WATER_DATE = longPreferencesKey("water_date")
        val LAST_KNOWN_STEPS = intPreferencesKey("last_known_steps")
        val LAST_KNOWN_CALORIES_BURNED = intPreferencesKey("last_known_calories_burned")
        val WEEKLY_BURN = intPreferencesKey("weekly_burn")
        val RECORD_BURN = intPreferencesKey("record_burn")
        
        // Camera Tutorial
        val HAS_SEEN_CAMERA_TUTORIAL = booleanPreferencesKey("has_seen_camera_tutorial")
        
        // Widget Theme
        val WIDGET_DARK_THEME = booleanPreferencesKey("widget_dark_theme")
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
            birthYear = preferences[PreferencesKeys.BIRTH_YEAR] ?: 2000,

            language = preferences[PreferencesKeys.LANGUAGE] ?: "en",
            lastRolloverDate = preferences[PreferencesKeys.LAST_ROLLOVER_DATE] ?: 0L
        )
    }
    
    /**
     * Sync current data to Firestore (fire-and-forget)
     */
    private fun syncToFirestore() {
        val userId = authRepository.getUserId()
        val isSignedIn = authRepository.isSignedIn()
        
        // Log caller for debugging
        val caller = Thread.currentThread().stackTrace.getOrNull(3)?.methodName ?: "unknown"
        Log.d("FirestoreSync", "========================================")
        Log.d("FirestoreSync", "syncToFirestore called from: $caller")
        Log.d("FirestoreSync", "userId='$userId' (length=${userId.length})")
        Log.d("FirestoreSync", "isSignedIn=$isSignedIn")
        Log.d("FirestoreSync", "========================================")
        
        if (userId.isEmpty()) {
            Log.w("FirestoreSync", "⚠️ SKIPPING SYNC - userId is empty!")
            Log.w("FirestoreSync", "   This means user is likely not signed in.")
            Log.w("FirestoreSync", "   Make sure Google Sign-In completes before data sync.")
            return
        }
        
        syncScope.launch {
            try {
                Log.d("FirestoreSync", "📤 Starting Firestore sync for user: $userId")
                val startTime = System.currentTimeMillis()
                
                val userData = getCurrentUserData()
                Log.d("FirestoreSync", "📦 User data to sync:")
                Log.d("FirestoreSync", "   - userName: '${userData.userName}'")
                Log.d("FirestoreSync", "   - userGoal: '${userData.userGoal}'")
                Log.d("FirestoreSync", "   - gender: '${userData.gender}'")
                Log.d("FirestoreSync", "   - age: ${userData.age}")
                Log.d("FirestoreSync", "   - weight: ${userData.weight}")
                Log.d("FirestoreSync", "   - height: ${userData.height}")
                Log.d("FirestoreSync", "   - calories: ${userData.recommendedCalories}")
                
                firestoreRepository.saveUserData(userId, userData)
                
                val elapsed = System.currentTimeMillis() - startTime
                Log.d("FirestoreSync", "✅ SUCCESS: Saved to Firestore in ${elapsed}ms")
            } catch (e: Exception) {
                Log.e("FirestoreSync", "❌ ERROR syncing to Firestore: ${e.message}")
                Log.e("FirestoreSync", "   Exception type: ${e.javaClass.simpleName}")
                e.printStackTrace()
            }
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

    override val lastRolloverDate: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LAST_ROLLOVER_DATE] ?: 0L
    }
    
    override val maxRolloverCalories: Int = 200 // Fixed value as per design
    
    // Appearance settings
    override val appearanceMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.APPEARANCE_MODE] ?: "light"
    }

    override val language: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LANGUAGE] ?: "en"
    }.distinctUntilChanged()
    
    // Personal details
    override val goalWeight: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.GOAL_WEIGHT] ?: 0f
    }

    override val startWeight: Flow<Float> = context.dataStore.data.map { preferences ->
        // Default to current weight if start weight not set yet
        preferences[PreferencesKeys.START_WEIGHT] ?: preferences[PreferencesKeys.WEIGHT] ?: 0f
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

    override val widgetDarkTheme: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.WIDGET_DARK_THEME] ?: false
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
        syncWidgetData()
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
        syncWidgetData()
    }
    
    override suspend fun setRolloverCaloriesAmount(amount: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ROLLOVER_CALORIES_AMOUNT] = amount.coerceAtMost(maxRolloverCalories)
        }
        syncToFirestore()
        syncWidgetData()
    }

    override suspend fun setLastRolloverDate(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_ROLLOVER_DATE] = timestamp
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
    
    override suspend fun setLanguage(code: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE] = code.lowercase()
        }
        syncToFirestore()
    }
    
    // Personal details setters
    override suspend fun setGoalWeight(weight: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GOAL_WEIGHT] = weight
            // If starting weight isn't set, set it now to current weight
            if (preferences[PreferencesKeys.START_WEIGHT] == null) {
                 preferences[PreferencesKeys.START_WEIGHT] = preferences[PreferencesKeys.WEIGHT] ?: weight
            }
        }
        syncToFirestore()
    }

    override suspend fun setStartWeight(weight: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.START_WEIGHT] = weight
        }
        syncToFirestore()
    }
    
    override suspend fun setDailyStepsGoal(steps: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_STEPS_GOAL] = steps
        }
        syncToFirestore()
        syncWidgetData()
    }
    
    override suspend fun setBirthDate(month: String, day: Int, year: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BIRTH_MONTH] = month
            preferences[PreferencesKeys.BIRTH_DAY] = day
            preferences[PreferencesKeys.BIRTH_YEAR] = year
            
            // Calculate age from birth date
            val birthMonth = try {
                Month.valueOf(month.uppercase())
            } catch (e: Exception) {
                Month.JANUARY // Fallback
            }
            val birthDate = LocalDate.of(year, birthMonth, day.coerceIn(1, 28)) // Coerce day to valid range
            val today = LocalDate.now()
            val age = Period.between(birthDate, today).years
            preferences[PreferencesKeys.AGE] = age.coerceAtLeast(0)
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
        
        // Record weight in history table
        weightHistoryDao.insertWeight(
            WeightHistoryEntity(
                weight = weight,
                timestamp = System.currentTimeMillis()
            )
        )
        
        syncToFirestore()
        syncWidgetData()
    }
    
    override suspend fun setHeight(heightCm: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HEIGHT] = heightCm
        }
        syncToFirestore()
        syncWidgetData()
    }

    override suspend fun setWaterConsumed(amount: Int, dateTimestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WATER_CONSUMED] = amount
            preferences[PreferencesKeys.WATER_DATE] = dateTimestamp
        }
        syncWidgetData()
    }

    override suspend fun setLastKnownSteps(steps: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_KNOWN_STEPS] = steps
        }
        syncWidgetData()
    }

    override suspend fun setActivityStats(caloriesBurned: Int, weeklyBurn: Int, recordBurn: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_KNOWN_CALORIES_BURNED] = caloriesBurned
            preferences[PreferencesKeys.WEEKLY_BURN] = weeklyBurn
            preferences[PreferencesKeys.RECORD_BURN] = recordBurn
        }
        syncWidgetData()
    }

    override suspend fun setHasSeenCameraTutorial(seen: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_SEEN_CAMERA_TUTORIAL] = seen
        }
    }
    
    override suspend fun setWidgetDarkTheme(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WIDGET_DARK_THEME] = isDark
        }
        // Sync to SharedPreferences for widget access
        val widgetPrefs = context.getSharedPreferences("widget_data", Context.MODE_PRIVATE)
        widgetPrefs.edit().putBoolean("widget_dark_theme", isDark).apply()
        // Trigger widget refresh
        updateWidget()
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
                    preferences[PreferencesKeys.LANGUAGE] = userData.language
                    preferences[PreferencesKeys.LAST_ROLLOVER_DATE] = userData.lastRolloverDate
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            // Ensure widget is updated with new user data (or defaults if restore failed)
            syncWidgetData()
        }
    }
    
    /**
     * Sync widget data to SharedPreferences for widget access
     * Widget can't use DataStore (causes multiple instance conflict)
     * 
     * Syncs: goals, steps, water, and activity metrics (burn, weekly burn, record)
     */
    override suspend fun syncWidgetData() {
        try {
            val prefs = context.getSharedPreferences("widget_data", Context.MODE_PRIVATE)
            val dataStorePrefs = context.dataStore.data.first()
            
            val steps = dataStorePrefs[PreferencesKeys.LAST_KNOWN_STEPS] ?: 0
            val caloriesBurned = dataStorePrefs[PreferencesKeys.LAST_KNOWN_CALORIES_BURNED] ?: 0
            val weeklyBurn = dataStorePrefs[PreferencesKeys.WEEKLY_BURN] ?: 0
            val recordBurn = dataStorePrefs[PreferencesKeys.RECORD_BURN] ?: 0
            
            prefs.edit()
                .putInt("recommended_calories", dataStorePrefs[PreferencesKeys.RECOMMENDED_CALORIES] ?: 2000)
                .putInt("recommended_protein", dataStorePrefs[PreferencesKeys.RECOMMENDED_PROTEIN] ?: 117)
                .putInt("recommended_carbs", dataStorePrefs[PreferencesKeys.RECOMMENDED_CARBS] ?: 203)
                .putInt("recommended_fats", dataStorePrefs[PreferencesKeys.RECOMMENDED_FATS] ?: 47)
                .putInt("daily_steps_goal", dataStorePrefs[PreferencesKeys.DAILY_STEPS_GOAL] ?: 10000)
                .putInt("last_known_steps", steps)
                .putInt("water_consumed", dataStorePrefs[PreferencesKeys.WATER_CONSUMED] ?: 0)
                .putLong("water_date", dataStorePrefs[PreferencesKeys.WATER_DATE] ?: 0L)
                // Activity stats
                .putInt("calories_burned", caloriesBurned)
                .putInt("weekly_burn", weeklyBurn)
                .putInt("record_burn", recordBurn)
                // New fields for widget redesign
                .putFloat("weight", dataStorePrefs[PreferencesKeys.WEIGHT] ?: 0f)
                .putInt("height", dataStorePrefs[PreferencesKeys.HEIGHT] ?: 0)
                .putBoolean("rollover_enabled", dataStorePrefs[PreferencesKeys.ROLLOVER_EXTRA_CALORIES] ?: false)
                .putInt("rollover_amount", dataStorePrefs[PreferencesKeys.ROLLOVER_CALORIES_AMOUNT] ?: 0)
                .putBoolean("add_calories_back", dataStorePrefs[PreferencesKeys.ADD_CALORIES_BACK] ?: false)
                .apply()
            
            // Trigger widget update
            updateWidget()
            Log.d("UserPreferences", "Synced widget data successfully")
        } catch (e: Exception) {
            Log.e("UserPreferences", "Error syncing widget data", e)
        }
    }
    
    /**
     * Trigger widget update
     */
    private fun updateWidget() {
        try {
            val intent = Intent(context, Class.forName("com.example.calview.widget.CaloriesWidgetProvider"))
            intent.action = android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val widgetManager = android.appwidget.AppWidgetManager.getInstance(context)
            val ids = widgetManager.getAppWidgetIds(
                android.content.ComponentName(context, Class.forName("com.example.calview.widget.CaloriesWidgetProvider"))
            )
            intent.putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        } catch (e: Exception) {
            Log.e("UserPreferences", "Error updating widget", e)
        }
    }
    
    override suspend fun clearAllData() {
        try {
            // Clear DataStore preferences
            context.dataStore.edit { preferences ->
                preferences.clear()
            }
            
            // Clear Widget SharedPreferences
            context.getSharedPreferences("widget_data", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply()
                
            // Trigger widget update to reflect empty state
            updateWidget()
            
            Log.d("UserPreferences", "Cleared all local preferences and widget data")
        } catch (e: Exception) {
            Log.e("UserPreferences", "Error clearing preferences", e)
        }
    }
    
    /**
     * Public method to sync all user data to Firestore.
     * Called before logout to ensure data is persisted.
     */
    override suspend fun syncToCloud() {
        val userId = authRepository.getUserId()
        if (userId.isEmpty()) {
            Log.w("FirestoreSync", "syncToCloud: Cannot sync - user not signed in")
            return
        }
        
        try {
            Log.d("FirestoreSync", "🔄 syncToCloud: Starting full sync before logout for user: $userId")
            val userData = getCurrentUserData()
            firestoreRepository.saveUserData(userId, userData)
            Log.d("FirestoreSync", "✅ syncToCloud: Successfully synced all data before logout")
        } catch (e: Exception) {
            Log.e("FirestoreSync", "❌ syncToCloud: Error syncing before logout", e)
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
