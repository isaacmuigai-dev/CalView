package com.example.calview.core.ai.model

import kotlinx.serialization.Serializable

/**
 * Response from AI exercise parsing service.
 */
@Serializable
data class ExerciseAnalysisResponse(
    val detected_exercises: List<ParsedExercise> = emptyList(),
    val total_estimated_calories: Int = 0,
    val total_duration_minutes: Int = 0,
    val confidence_score: Double = 0.0,
    val fitness_insight: String = ""
)

/**
 * Individual exercise parsed from user input.
 */
@Serializable
data class ParsedExercise(
    val name: String = "",
    val type: String = "Other",  // "Cardio", "Strength", "Flexibility", "Sport", "Other"
    val duration_minutes: Int? = null,
    val intensity: String = "moderate",  // "low", "moderate", "high", "very_high"
    val estimated_calories: Int = 0,
    val met_value: Double = 4.0,
    val sets: Int? = null,
    val reps: Int? = null,
    val weight_kg: Double? = null,
    val distance_km: Double? = null,
    val muscle_groups: List<String> = emptyList(),
    val confidence: Double = 0.9,
    val detection_note: String? = null
)

/**
 * Error response from AI.
 */
@Serializable
data class ExerciseErrorResponse(
    val error: String = ""
)

/**
 * Convert intensity string to float value (0.0 to 1.0).
 */
fun String.toIntensityFloat(): Float {
    return when (this.lowercase()) {
        "very_low", "very low" -> 0.1f
        "low", "light" -> 0.25f
        "moderate", "medium" -> 0.5f
        "high", "intense" -> 0.75f
        "very_high", "very high", "maximum", "max" -> 1.0f
        else -> 0.5f  // Default to moderate
    }
}

/**
 * Convert float intensity to descriptive string.
 */
fun Float.toIntensityString(): String {
    return when {
        this < 0.2f -> "Very Low"
        this < 0.4f -> "Low"
        this < 0.6f -> "Moderate"
        this < 0.8f -> "High"
        else -> "Very High"
    }
}
