package com.example.calview.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// Analysis status for meal scanning
enum class AnalysisStatus {
    PENDING,      // Just created, waiting to start
    ANALYZING,    // AI is processing the image
    COMPLETED,    // Analysis complete with results
    FAILED        // Analysis failed
}

@Entity(tableName = "meals")
data class MealEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val firestoreId: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fats: Int,
    val fiber: Int = 0,
    val sugar: Int = 0,
    val sodium: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val imagePath: String? = null,
    val imageUrl: String? = null, // Firebase Storage download URL
    val analysisStatus: AnalysisStatus = AnalysisStatus.COMPLETED,
    val analysisProgress: Float = 100f,  // 0-100 progress percentage
    val analysisStatusMessage: String = "",  // Current analysis phase message
    val healthInsight: String? = null,
    val confidenceScore: Float = 0f,  // Overall confidence 0-100%
    val detectedItemsJson: String? = null  // JSON array of detected items with per-item confidence
)

