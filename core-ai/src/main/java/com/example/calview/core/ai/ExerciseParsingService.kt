package com.example.calview.core.ai

import com.example.calview.core.ai.model.ExerciseAnalysisResponse

/**
 * Service for parsing natural language exercise descriptions using AI.
 * Premium feature for CalView.
 */
interface ExerciseParsingService {
    
    /**
     * Parse a natural language exercise description and estimate calories burned.
     * 
     * @param text User's description of the exercise (e.g., "Ran 5km in 30 mins", "Did a heavy chest workout for an hour")
     * @param userWeightKg User's weight in kg for accurate calorie calculation
     * @return Result containing parsed exercises with calorie estimates
     * 
     * Example inputs:
     * - "Ran 5km in 30 minutes"
     * - "Did a heavy chest workout for an hour"
     * - "Played basketball for 45 minutes"
     * - "20 minutes of HIIT followed by 10 minutes of stretching"
     */
    suspend fun parseExerciseText(text: String, userWeightKg: Double): Result<ExerciseAnalysisResponse>
}
