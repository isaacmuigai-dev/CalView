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
    private val weightHistoryRepository: WeightHistoryRepository
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

        // Walkthrough flags
        val HAS_SEEN_DASHBOARD_WALKTHROUGH = booleanPreferencesKey("has_seen_dashboard_walkthrough")
        val HAS_SEEN_PROGRESS_WALKTHROUGH = booleanPreferencesKey("has_seen_progress_walkthrough")
        val HAS_SEEN_FEATURE_INTRO = booleanPreferencesKey("has_seen_feature_intro")
        val WEIGHT_CHANGE_PER_WEEK = floatPreferencesKey("weight_change_per_week")
        val USER_XP = intPreferencesKey("user_xp")
        val USER_LEVEL = intPreferencesKey("user_level")
        val LAST_ACTIVITY_TIMESTAMP = longPreferencesKey("last_activity_timestamp")
        val WATER_SERVING_SIZE = intPreferencesKey("water_serving_size")
        
        // Smart Coach tracking
        val COACH_LAST_MESSAGE_TIME = longPreferencesKey("coach_last_message_time")
        val COACH_MESSAGE_COUNT_TODAY = intPreferencesKey("coach_message_count_today")
        val COACH_LAST_MESSAGE_DATE = stringPreferencesKey("coach_last_message_date")
        val LAST_NOTIFIED_GOAL_WEIGHT = floatPreferencesKey("last_notified_goal_weight")
        val LAST_NOTIFIED_DAILY_GOAL_DATE = stringPreferencesKey("last_notified_daily_goal_date")
        val NOTIFIED_DAILY_GOAL_FLAGS = stringPreferencesKey("notified_daily_goal_flags")
        
        // Groups Profile
        val IS_GROUPS_PROFILE_COMPLETE = booleanPreferencesKey("is_groups_profile_complete")
        val GROUPS_FIRST_NAME = stringPreferencesKey("groups_first_name")
        val GROUPS_LAST_NAME = stringPreferencesKey("groups_last_name")
        val GROUPS_USERNAME = stringPreferencesKey("groups_username")
        val GROUPS_PROFILE_PHOTO_URL = stringPreferencesKey("groups_profile_photo_url")
        val IS_GROUP_CREATED = booleanPreferencesKey("is_group_created")
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
            lastRolloverDate = preferences[PreferencesKeys.LAST_ROLLOVER_DATE] ?: 0L,
            hasSeenDashboardWalkthrough = preferences[PreferencesKeys.HAS_SEEN_DASHBOARD_WALKTHROUGH] ?: false,
            hasSeenProgressWalkthrough = preferences[PreferencesKeys.HAS_SEEN_PROGRESS_WALKTHROUGH] ?: false,
            hasSeenFeatureIntro = preferences[PreferencesKeys.HAS_SEEN_FEATURE_INTRO] ?: false,
            waterConsumed = preferences[PreferencesKeys.WATER_CONSUMED] ?: 0,
            waterDate = preferences[PreferencesKeys.WATER_DATE] ?: 0L,
            lastKnownSteps = preferences[PreferencesKeys.LAST_KNOWN_STEPS] ?: 0,
            lastKnownCaloriesBurned = preferences[PreferencesKeys.LAST_KNOWN_CALORIES_BURNED] ?: 0,
            weeklyBurn = preferences[PreferencesKeys.WEEKLY_BURN] ?: 0,
            recordBurn = preferences[PreferencesKeys.RECORD_BURN] ?: 0,
            hasSeenCameraTutorial = preferences[PreferencesKeys.HAS_SEEN_CAMERA_TUTORIAL] ?: false,
            widgetDarkTheme = preferences[PreferencesKeys.WIDGET_DARK_THEME] ?: false,
            weightChangePerWeek = preferences[PreferencesKeys.WEIGHT_CHANGE_PER_WEEK] ?: 0.5f,
            userXp = preferences[PreferencesKeys.USER_XP] ?: 0,
            userLevel = preferences[PreferencesKeys.USER_LEVEL] ?: 1,
            lastActivityTimestamp = preferences[PreferencesKeys.LAST_ACTIVITY_TIMESTAMP] ?: 0L,
            isGroupsProfileComplete = preferences[PreferencesKeys.IS_GROUPS_PROFILE_COMPLETE] ?: false,
            groupsFirstName = preferences[PreferencesKeys.GROUPS_FIRST_NAME] ?: "",
            groupsLastName = preferences[PreferencesKeys.GROUPS_LAST_NAME] ?: "",
            groupsUsername = preferences[PreferencesKeys.GROUPS_USERNAME] ?: "",
            groupsProfilePhotoUrl = preferences[PreferencesKeys.GROUPS_PROFILE_PHOTO_URL] ?: "",
            isGroupCreated = preferences[PreferencesKeys.IS_GROUP_CREATED] ?: false
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

    override val weightChangePerWeek: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.WEIGHT_CHANGE_PER_WEEK] ?: 0.5f
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
        val start = preferences[PreferencesKeys.START_WEIGHT] ?: 0f
        if (start > 0f) start else preferences[PreferencesKeys.WEIGHT] ?: 0f
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

    override val hasSeenDashboardWalkthrough: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HAS_SEEN_DASHBOARD_WALKTHROUGH] ?: false
    }

    override val hasSeenProgressWalkthrough: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HAS_SEEN_PROGRESS_WALKTHROUGH] ?: false
    }

    override val hasSeenFeatureIntro: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HAS_SEEN_FEATURE_INTRO] ?: false
    }

    override val userXp: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_XP] ?: 0
    }

    override val userLevel: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_LEVEL] ?: 1
    }

    override val lastActivityTimestamp: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LAST_ACTIVITY_TIMESTAMP] ?: 0L
    }

    override val recordBurn: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.RECORD_BURN] ?: 0
    }


    override val waterServingSize: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.WATER_SERVING_SIZE] ?: 250
    }
    
    // Smart Coach tracking
    override val coachLastMessageTime: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.COACH_LAST_MESSAGE_TIME] ?: 0L
    }
    
    override val coachMessageCountToday: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.COACH_MESSAGE_COUNT_TODAY] ?: 0
    }
    
    override val coachLastMessageDate: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.COACH_LAST_MESSAGE_DATE] ?: ""
    }

    override val lastNotifiedGoalWeight: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LAST_NOTIFIED_GOAL_WEIGHT] ?: 0f
    }

    override val lastNotifiedDailyGoalDate: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LAST_NOTIFIED_DAILY_GOAL_DATE] ?: ""
    }

    override val notifiedDailyGoalFlags: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NOTIFIED_DAILY_GOAL_FLAGS] ?: ""
    }

    override val isGroupsProfileComplete: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_GROUPS_PROFILE_COMPLETE] ?: false
    }

    override val groupsFirstName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.GROUPS_FIRST_NAME] ?: ""
    }

    override val groupsLastName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.GROUPS_LAST_NAME] ?: ""
    }

    override val groupsUsername: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.GROUPS_USERNAME] ?: ""
    }

    override val groupsProfilePhotoUrl: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.GROUPS_PROFILE_PHOTO_URL] ?: ""
    }

    override val isGroupCreated: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_GROUP_CREATED] ?: false
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
            
            // Initialize start weight if not set
            if (preferences[PreferencesKeys.START_WEIGHT] == null || preferences[PreferencesKeys.START_WEIGHT] == 0f) {
                preferences[PreferencesKeys.START_WEIGHT] = weight
            }
        }

        // Record weight in history table
        weightHistoryRepository.insertWeight(
            WeightHistoryEntity(
                weight = weight,
                timestamp = System.currentTimeMillis()
            )
        )

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
            // Do NOT automatically reset start weight here. 
            // It should only be reset if the user explicitly changes it or starts a new journey.
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
            val birthDate = LocalDate.of(year, birthMonth, day.coerceIn(1, 31)) // Coerce day to valid range
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
            
            // Initialize start weight if not set
            if (preferences[PreferencesKeys.START_WEIGHT] == null || preferences[PreferencesKeys.START_WEIGHT] == 0f) {
                preferences[PreferencesKeys.START_WEIGHT] = weight
            }
        }
        
        // Record weight in history table
        weightHistoryRepository.insertWeight(
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

    override suspend fun setWeightChangePerWeek(pace: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WEIGHT_CHANGE_PER_WEEK] = pace
        }
        syncToFirestore()
    }

    override suspend fun setWaterConsumed(amount: Int, dateTimestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WATER_CONSUMED] = amount
            preferences[PreferencesKeys.WATER_DATE] = dateTimestamp
        }
        syncToFirestore()
        syncWidgetData()
    }

    override suspend fun setLastKnownSteps(steps: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_KNOWN_STEPS] = steps
        }
        syncToFirestore()
        syncWidgetData()
    }

    override suspend fun setActivityStats(caloriesBurned: Int, weeklyBurn: Int, recordBurn: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_KNOWN_CALORIES_BURNED] = caloriesBurned
            preferences[PreferencesKeys.WEEKLY_BURN] = weeklyBurn
            preferences[PreferencesKeys.RECORD_BURN] = recordBurn
        }
        syncToFirestore()
        syncWidgetData()
    }

    override suspend fun setHasSeenCameraTutorial(seen: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_SEEN_CAMERA_TUTORIAL] = seen
        }
        syncToFirestore()
    }

    override suspend fun setHasSeenDashboardWalkthrough(seen: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_SEEN_DASHBOARD_WALKTHROUGH] = seen
        }
        syncToFirestore()
    }

    override suspend fun setHasSeenProgressWalkthrough(seen: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_SEEN_PROGRESS_WALKTHROUGH] = seen
        }
        syncToFirestore()
    }

    override suspend fun setHasSeenFeatureIntro(seen: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_SEEN_FEATURE_INTRO] = seen
        }
        syncToFirestore()
    }

    override suspend fun setUserXp(xp: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_XP] = xp
        }
        syncToFirestore()
    }

    override suspend fun setUserLevel(level: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_LEVEL] = level
        }
        syncToFirestore()
    }
    
    override suspend fun setLastActivityTimestamp(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_ACTIVITY_TIMESTAMP] = timestamp
        }
    }

    override suspend fun setWaterServingSize(ml: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WATER_SERVING_SIZE] = ml
        }
        syncToFirestore()
    }
    
    override suspend fun setCoachMessageTracking(timestamp: Long, count: Int, date: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.COACH_LAST_MESSAGE_TIME] = timestamp
            preferences[PreferencesKeys.COACH_MESSAGE_COUNT_TODAY] = count
            preferences[PreferencesKeys.COACH_LAST_MESSAGE_DATE] = date
        }
    }

    override suspend fun setLastNotifiedGoalWeight(weight: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_NOTIFIED_GOAL_WEIGHT] = weight
        }
    }

    override suspend fun setDailyGoalNotified(date: String, flag: String) {
        context.dataStore.edit { preferences ->
            val lastDate = preferences[PreferencesKeys.LAST_NOTIFIED_DAILY_GOAL_DATE] ?: ""
            val currentFlags = if (lastDate == date) {
                preferences[PreferencesKeys.NOTIFIED_DAILY_GOAL_FLAGS] ?: ""
            } else {
                "" // Reset flags for a new day
            }
            
            val updatedFlags = if (currentFlags.isEmpty()) {
                flag
            } else if (currentFlags.split(",").contains(flag)) {
                currentFlags
            } else {
                "$currentFlags,$flag"
            }
            
            preferences[PreferencesKeys.LAST_NOTIFIED_DAILY_GOAL_DATE] = date
            preferences[PreferencesKeys.NOTIFIED_DAILY_GOAL_FLAGS] = updatedFlags
        }
    }

    override suspend fun clearDailyGoalNotifications() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.LAST_NOTIFIED_DAILY_GOAL_DATE)
            preferences.remove(PreferencesKeys.NOTIFIED_DAILY_GOAL_FLAGS)
        }
    }

    override suspend fun setGroupsProfileComplete(complete: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_GROUPS_PROFILE_COMPLETE] = complete
        }
        syncToFirestore()
    }

    override suspend fun setGroupsFirstName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GROUPS_FIRST_NAME] = name
        }
        syncToFirestore()
    }

    override suspend fun setGroupsLastName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GROUPS_LAST_NAME] = name
        }
        syncToFirestore()
    }

    override suspend fun setGroupsUsername(username: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GROUPS_USERNAME] = username
        }
        syncToFirestore()
    }

    override suspend fun setGroupsProfilePhotoUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GROUPS_PROFILE_PHOTO_URL] = url
        }
        syncToFirestore()
    }

    override suspend fun setGroupCreated(created: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_GROUP_CREATED] = created
        }
        syncToFirestore()
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
        android.util.Log.d("RestoreData", "restoreFromCloud called for userId: '$userId'")
        if (userId.isEmpty()) {
            android.util.Log.w("RestoreData", "Skipping restore: userId is empty")
            return false
        }
        
        return try {
            android.util.Log.d("RestoreData", "Fetching user data from Firestore...")
            val userData = firestoreRepository.getUserData(userId)
            if (userData != null) {
                android.util.Log.d("RestoreData", "✅ User data found. isOnboardingComplete=${userData.isOnboardingComplete}")
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
                    preferences[PreferencesKeys.HAS_SEEN_DASHBOARD_WALKTHROUGH] = userData.hasSeenDashboardWalkthrough
                    preferences[PreferencesKeys.IS_GROUPS_PROFILE_COMPLETE] = userData.isGroupsProfileComplete
                    preferences[PreferencesKeys.GROUPS_FIRST_NAME] = userData.groupsFirstName
                    preferences[PreferencesKeys.GROUPS_LAST_NAME] = userData.groupsLastName
                    preferences[PreferencesKeys.GROUPS_USERNAME] = userData.groupsUsername
                    preferences[PreferencesKeys.GROUPS_PROFILE_PHOTO_URL] = userData.groupsProfilePhotoUrl
                    preferences[PreferencesKeys.IS_GROUP_CREATED] = userData.isGroupCreated
                    preferences[PreferencesKeys.HAS_SEEN_PROGRESS_WALKTHROUGH] = userData.hasSeenProgressWalkthrough
                    preferences[PreferencesKeys.HAS_SEEN_FEATURE_INTRO] = userData.hasSeenFeatureIntro
                    preferences[PreferencesKeys.WATER_CONSUMED] = userData.waterConsumed
                    preferences[PreferencesKeys.WATER_DATE] = userData.waterDate
                    preferences[PreferencesKeys.LAST_KNOWN_STEPS] = userData.lastKnownSteps
                    preferences[PreferencesKeys.LAST_KNOWN_CALORIES_BURNED] = userData.lastKnownCaloriesBurned
                    preferences[PreferencesKeys.WEEKLY_BURN] = userData.weeklyBurn
                    preferences[PreferencesKeys.RECORD_BURN] = userData.recordBurn
                    preferences[PreferencesKeys.HAS_SEEN_CAMERA_TUTORIAL] = userData.hasSeenCameraTutorial
                    preferences[PreferencesKeys.WIDGET_DARK_THEME] = userData.widgetDarkTheme
                    preferences[PreferencesKeys.WEIGHT_CHANGE_PER_WEEK] = userData.weightChangePerWeek
                    preferences[PreferencesKeys.USER_XP] = userData.userXp
                    preferences[PreferencesKeys.USER_LEVEL] = userData.userLevel
                    preferences[PreferencesKeys.LAST_ACTIVITY_TIMESTAMP] = userData.lastActivityTimestamp
                }
                android.util.Log.d("RestoreData", "Local DataStore updated from cloud.")
                true
            } else {
                android.util.Log.w("RestoreData", "⚠️ No user data found in Firestore (userData is null)")
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("RestoreData", "❌ Error restoring from cloud", e)
            e.printStackTrace()
            false
        } finally {
            // Restore weight history and other repository data
            weightHistoryRepository.restoreFromCloud()
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
