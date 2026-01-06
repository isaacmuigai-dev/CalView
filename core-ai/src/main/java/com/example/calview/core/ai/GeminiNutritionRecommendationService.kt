package com.example.calview.core.ai

import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.type.content
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Gemini AI-powered nutrition recommendation service.
 * Uses AI for personalized recommendations with fallback to BMR/TDEE formulas.
 */
class GeminiNutritionRecommendationService @Inject constructor(
    private val generativeModel: GenerativeModel
) : NutritionRecommendationService {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getRecommendations(profile: UserProfile): Result<NutritionRecommendation> {
        return try {
            // Try AI-powered recommendation first
            getAIRecommendation(profile)
        } catch (e: Exception) {
            android.util.Log.w("NutritionAI", "AI recommendation failed, using fallback: ${e.message}")
            // Fallback to traditional calculation
            Result.success(calculateFallbackRecommendation(profile))
        }
    }

    private suspend fun getAIRecommendation(profile: UserProfile): Result<NutritionRecommendation> {
        val prompt = """
            You are a certified nutritionist AI. Calculate personalized daily nutrition recommendations.
            
            User Profile:
            - Gender: ${profile.gender}
            - Age: ${profile.age} years
            - Weight: ${profile.weightKg} kg
            - Height: ${profile.heightCm} cm
            - Activity Level: ${profile.activityLevel} workouts per week
            - Goal: ${profile.goal}
            
            Instructions:
            1. Calculate BMR using Mifflin-St Jeor equation
            2. Apply activity multiplier (sedentary=1.2, light=1.375, moderate=1.55, active=1.725, very active=1.9)
            3. Adjust calories based on goal:
               - Lose weight: Subtract 300-500 calories (safe deficit)
               - Maintain: No adjustment
               - Gain weight: Add 300-500 calories (lean bulk)
            4. Calculate macros:
               - Protein: 1.6-2.2g per kg body weight (higher for muscle gain)
               - Fats: 20-35% of calories
               - Carbs: Remaining calories
            
            Return ONLY a valid JSON object with this structure:
            {
              "calories": number,
              "protein": number,
              "carbs": number,
              "fats": number,
              "explanation": "Brief explanation of the calculation"
            }
        """.trimIndent()

        val response = generativeModel.generateContent(
            content { text(prompt) }
        )

        val responseText = response.text?.trim() ?: throw Exception("Empty response from AI")
        val cleanedJson = responseText
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        val aiResponse = json.decodeFromString<AIRecommendationResponse>(cleanedJson)
        
        return Result.success(
            NutritionRecommendation(
                calories = aiResponse.calories,
                protein = aiResponse.protein,
                carbs = aiResponse.carbs,
                fats = aiResponse.fats,
                explanation = aiResponse.explanation
            )
        )
    }

    /**
     * Fallback calculation using Mifflin-St Jeor equation.
     * Used when AI is unavailable.
     */
    private fun calculateFallbackRecommendation(profile: UserProfile): NutritionRecommendation {
        // Step 1: Calculate BMR using Mifflin-St Jeor
        val bmr = if (profile.gender.equals("Male", ignoreCase = true)) {
            10 * profile.weightKg + 6.25 * profile.heightCm - 5 * profile.age + 5
        } else {
            10 * profile.weightKg + 6.25 * profile.heightCm - 5 * profile.age - 161
        }

        // Step 2: Apply activity multiplier
        val activityMultiplier = when (profile.activityLevel) {
            "0-2" -> 1.375f  // Light activity
            "3-5" -> 1.55f   // Moderate activity
            "6+" -> 1.725f   // Very active
            else -> 1.375f   // Default to light
        }

        val tdee = bmr * activityMultiplier

        // Step 3: Adjust for goal
        val targetCalories = when {
            profile.goal.contains("lose", ignoreCase = true) -> (tdee - 400).roundToInt()
            profile.goal.contains("gain", ignoreCase = true) -> (tdee + 400).roundToInt()
            else -> tdee.roundToInt() // Maintain
        }

        // Step 4: Calculate macros
        // Protein: 1.8g per kg body weight
        val protein = (profile.weightKg * 1.8).roundToInt()
        
        // Fats: 25% of calories (1g fat = 9 calories)
        val fats = (targetCalories * 0.25 / 9).roundToInt()
        
        // Carbs: Remaining calories (1g = 4 calories)
        val proteinCalories = protein * 4
        val fatCalories = fats * 9
        val carbs = (targetCalories - proteinCalories - fatCalories) / 4

        return NutritionRecommendation(
            calories = targetCalories.coerceAtLeast(1200), // Minimum safe calories
            protein = protein,
            carbs = carbs.coerceAtLeast(50), // Minimum carbs
            fats = fats.coerceAtLeast(30), // Minimum fats
            explanation = "Calculated using Mifflin-St Jeor equation with ${activityMultiplier}x activity multiplier"
        )
    }

    @Serializable
    private data class AIRecommendationResponse(
        val calories: Int,
        val protein: Int,
        val carbs: Int,
        val fats: Int,
        val explanation: String? = null
    )
}
