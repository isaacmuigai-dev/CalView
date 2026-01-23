package com.example.calview.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "badges")
data class BadgeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val iconResName: String, // e.g., "ic_badge_streak_7"
    val dateUnlocked: Long,
    val tier: BadgeTier
)

enum class BadgeTier {
    BRONZE, SILVER, GOLD, PLATINUM
}
