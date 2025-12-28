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
    val name: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fats: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val imagePath: String? = null,
    val analysisStatus: AnalysisStatus = AnalysisStatus.COMPLETED,
    val analysisProgress: Float = 100f,  // 0-100 progress percentage
    val healthInsight: String? = null
)
