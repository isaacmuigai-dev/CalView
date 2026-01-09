package com.example.calview.core.ai.model

import kotlinx.serialization.Serializable

@Serializable
data class FoodAnalysisResponse(
    val detected_items: List<FoodItem>,
    val total: NutritionalData,
    val confidence_score: Double,
    val health_insight: String
)

@Serializable
data class FoodItem(
    val name: String,
    val estimated_weight_g: Int,
    val calories: Int,
    val macros: Macros,
    val confidence: Double = 0.9,  // Per-item confidence 0.0-1.0
    val detection_note: String? = null  // e.g., "hidden/shiny surface", "partially visible"
)

@Serializable
data class NutritionalData(
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fats: Int,
    val fiber: Int = 0,
    val sugar: Int = 0,
    val sodium: Int = 0
)

@Serializable
data class Macros(
    val p: Int,
    val c: Int,
    val f: Int,
    val fi: Int = 0,  // Fiber
    val s: Int = 0,   // Sugar
    val na: Int = 0   // Sodium (mg)
)

@Serializable
data class ErrorResponse(
    val error: String
)
