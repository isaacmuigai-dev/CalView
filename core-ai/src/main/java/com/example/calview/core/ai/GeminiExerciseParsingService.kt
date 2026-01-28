package com.example.calview.core.ai

import android.util.Log
import com.example.calview.core.ai.model.ExerciseAnalysisResponse
import com.example.calview.core.ai.model.ExerciseErrorResponse
import com.google.firebase.ai.GenerativeModel
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * Gemini-powered implementation of ExerciseParsingService.
 * Uses AI to parse natural language exercise descriptions and estimate calories.
 */
class GeminiExerciseParsingService @Inject constructor(
    private val generativeModel: GenerativeModel
) : ExerciseParsingService {
    
    companion object {
        private const val TAG = "GeminiExerciseAI"
    }
    
    private val json = Json { ignoreUnknownKeys = true }
    
    override suspend fun parseExerciseText(text: String, userWeightKg: Double): Result<ExerciseAnalysisResponse> {
        return try {
            Log.d(TAG, "Parsing exercise text: $text (user weight: $userWeightKg kg)")
            
            val prompt = buildExercisePrompt(text, userWeightKg)
            
            Log.d(TAG, "Sending request to Gemini API...")
            val response = generativeModel.generateContent(prompt)
            
            Log.d(TAG, "Received response from Gemini API")
            parseResponse(response.text)
        } catch (e: Exception) {
            Log.e(TAG, "Exercise parsing failed: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    private fun buildExercisePrompt(userInput: String, userWeightKg: Double): String {
        return """
            Parse the following exercise description and estimate calories burned.
            
            User's description: "$userInput"
            User's weight: $userWeightKg kg
            
            IMPORTANT INSTRUCTIONS:
            1. Identify all exercises mentioned in the description
            2. For each exercise, determine:
               - Exercise name (use standard names like "Running", "Weightlifting", "HIIT", etc.)
               - Type: "Cardio", "Strength", "Flexibility", "Sport", or "Other"
               - Duration in minutes (estimate if not specified)
               - Intensity: "low", "moderate", "high", or "very_high"
               - MET value (Metabolic Equivalent of Task)
               - Estimated calories burned using formula: MET × weight(kg) × duration(hours)
               - Sets/reps/weight if mentioned (for strength training)
               - Distance if mentioned (for cardio)
               - Primary muscle groups targeted
            
            3. MET Reference Values:
               - Walking (3.5 mph): 4.3
               - Running (6 mph): 9.8
               - Running (8 mph): 11.8
               - Cycling (moderate): 8.0
               - Swimming (moderate): 7.0
               - HIIT: 9.0-12.0
               - Weight Training: 5.0-6.0
               - Yoga: 2.5-4.0
               - Sports (general): 6.0-10.0
            
            4. Intensity affects MET:
               - Low intensity: 70% of base MET
               - Moderate intensity: 100% of base MET
               - High intensity: 120% of base MET
               - Very high intensity: 130% of base MET
            
            5. Provide a brief fitness insight or tip related to the workout.
            
            IMPORTANT: Return all numeric values as INTEGERS except for met_value, weight_kg, distance_km, and confidence which can be decimals.
            
            Return ONLY a valid JSON object with this structure:
            {
              "detected_exercises": [
                {
                  "name": "string (e.g., 'Running', 'Bench Press')",
                  "type": "string (Cardio/Strength/Flexibility/Sport/Other)",
                  "duration_minutes": integer or null,
                  "intensity": "string (low/moderate/high/very_high)",
                  "estimated_calories": integer,
                  "met_value": number,
                  "sets": integer or null,
                  "reps": integer or null,
                  "weight_kg": number or null,
                  "distance_km": number or null,
                  "muscle_groups": ["string"],
                  "confidence": number (0.0 to 1.0),
                  "detection_note": null or "string if uncertain"
                }
              ],
              "total_estimated_calories": integer,
              "total_duration_minutes": integer,
              "confidence_score": number (0.0 to 1.0),
              "fitness_insight": "string with a brief tip or insight about the workout"
            }
            
            If no exercise can be identified, return: {"error": "Could not identify any exercise from the description"}
        """.trimIndent()
    }
    
    private fun parseResponse(responseText: String?): Result<ExerciseAnalysisResponse> {
        val text = responseText?.trim()
        if (text.isNullOrEmpty()) {
            return Result.failure(Exception("Empty response from AI"))
        }
        
        Log.d(TAG, "Response text (first 300 chars): ${text.take(300)}")
        
        try {
            // Remove markdown code blocks if present
            val cleanedText = text
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()
            
            // Find JSON object boundaries
            val firstBrace = cleanedText.indexOf('{')
            val lastBrace = cleanedText.lastIndexOf('}')
            
            if (firstBrace == -1 || lastBrace == -1 || lastBrace <= firstBrace) {
                Log.e(TAG, "No JSON object found in response: $cleanedText")
                return Result.failure(Exception("No valid JSON found in AI response"))
            }
            
            var jsonString = cleanedText.substring(firstBrace, lastBrace + 1)
            
            // Cleanup common AI JSON mistakes
            jsonString = jsonString
                .replace(",\\s*\\}".toRegex(), "}")  // Trailing commas in objects
                .replace(",\\s*\\]".toRegex(), "]")  // Trailing commas in arrays
            
            // Check for error response
            if (jsonString.contains("\"error\"") && !jsonString.contains("\"detected_exercises\"")) {
                try {
                    val errorObj = json.decodeFromString<ExerciseErrorResponse>(jsonString)
                    return Result.failure(Exception(errorObj.error))
                } catch (e: Exception) {
                    return Result.failure(Exception("AI could not identify any exercise"))
                }
            }
            
            val result = json.decodeFromString<ExerciseAnalysisResponse>(jsonString)
            Log.d(TAG, "Successfully parsed ${result.detected_exercises.size} exercises, total calories: ${result.total_estimated_calories}")
            return Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "JSON parsing failed", e)
            Log.e(TAG, "Raw text: $text")
            return Result.failure(Exception("Failed to parse exercise data: ${e.message}"))
        }
    }
}
