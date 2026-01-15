package com.example.calview.core.ml.model

/**
 * Represents a food prediction from the classifier
 */
data class FoodPrediction(
    val label: String,
    val confidence: Float,
    val caloriesPer100g: Int = 0
)

/**
 * Represents a detected food item with bounding box
 */
data class DetectedFoodItem(
    val name: String,
    val calories: Int,
    val confidence: Float,
    val boundingBox: android.graphics.RectF,
    val trackingId: Int? = null
)

/**
 * Represents a detected object from ML Kit
 */
data class DetectedObject(
    val boundingBox: android.graphics.RectF,
    val trackingId: Int? = null,
    val category: String? = null
)
