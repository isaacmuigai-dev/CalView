package com.example.calview.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "challenges")
data class ChallengeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val type:  ChallengeType,
    val targetValue: Int,
    val currentProgress: Int = 0,
    val startDate: Long,
    val endDate: Long,
    val isCompleted: Boolean = false,
    val badgeRewardId: String? = null
)

enum class ChallengeType {
    STREAK,
    LOG_MEALS,
    HIT_PROTEIN,
    DRINK_WATER,
    WORKOUT,
    EARLY_BIRD
}
