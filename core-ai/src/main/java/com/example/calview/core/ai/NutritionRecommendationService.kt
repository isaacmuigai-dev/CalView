package com.example.calview.core.ai

/**
 * Data class representing a user's profile for nutrition calculations
 */
data class UserProfile(
    val gender: String,        // "Male", "Female", "Other"
    val age: Int,              // Years
    val weightKg: Float,       // Weight in kg
    val heightCm: Float,       // Height in cm
    val activityLevel: String, // "0-2", "3-5", "6+" workouts per week
    val goal: String           // "Lose weight", "Maintain", "Gain weight"
)

/**
 * Data class representing recommended daily nutrition intake
 */
data class NutritionRecommendation(
    val calories: Int,
    val protein: Int,      // grams
    val carbs: Int,        // grams
    val fats: Int,         // grams
    val explanation: String? = null
)

/**
 * Service for calculating personalized nutrition recommendations
 */
interface NutritionRecommendationService {
    /**
     * Calculate personalized nutrition recommendations based on user profile.
     * Uses AI when available, falls back to BMR/TDEE calculation.
     */
    suspend fun getRecommendations(profile: UserProfile): Result<NutritionRecommendation>
}
