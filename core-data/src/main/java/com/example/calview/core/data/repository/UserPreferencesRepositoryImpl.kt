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
        
        // New calorie settings
        val ADD_CALORIES_BACK = booleanPreferencesKey("add_calories_back")
        val ROLLOVER_EXTRA_CALORIES = booleanPreferencesKey("rollover_extra_calories")
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
    
    // New calorie settings for dashboard
    override val addCaloriesBack: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ADD_CALORIES_BACK] ?: false
    }
    
    override val rolloverExtraCalories: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ROLLOVER_EXTRA_CALORIES] ?: false
    }
    
    override val maxRolloverCalories: Int = 200 // Fixed value as per design

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
}
