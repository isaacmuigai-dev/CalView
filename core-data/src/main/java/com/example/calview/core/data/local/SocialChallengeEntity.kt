package com.example.calview.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity for social challenges between friends.
 * Synced with Firebase Firestore for real-time updates.
 */
@Entity(tableName = "social_challenges")
data class SocialChallengeEntity(
    @PrimaryKey val id: String = "",
    val title: String = "",
    val description: String = "",
    val type: String = "STREAK", // STREAK, WATER, CALORIES, PROTEIN, STEPS, LOGGING
    val targetValue: Int = 0, // e.g., 7 days, 2000ml, 10000 steps
    val startDate: Long = 0,
    val endDate: Long = 0,
    val creatorId: String = "",
    val creatorName: String = "",
    val isActive: Boolean = true,
    val inviteCode: String = "", // For sharing
    val firestoreId: String = "" // Firebase document ID
)

/**
 * Challenge types
 */
enum class SocialChallengeType {
    STREAK,    // Who can maintain longest streak
    WATER,     // Who drinks most water
    CALORIES,  // Who hits calorie goals most days
    PROTEIN,   // Who hits protein goals most days
    STEPS,     // Who gets most steps
    LOGGING    // Who logs meals most consistently
}
