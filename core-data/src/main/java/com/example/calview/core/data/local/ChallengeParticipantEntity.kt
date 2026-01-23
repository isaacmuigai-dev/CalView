package com.example.calview.core.data.local

import androidx.room.Entity

/**
 * Entity for tracking participant progress in social challenges.
 */
@Entity(
    tableName = "challenge_participants",
    primaryKeys = ["challengeId", "odsmUserId"]
)
data class ChallengeParticipantEntity(
    val challengeId: String = "",
    val odsmUserId: String = "", // Firebase Auth UID
    val displayName: String = "",
    val photoUrl: String = "",
    val currentProgress: Int = 0, // Current value (streak days, ml, steps, etc.)
    val progressPercent: Float = 0f,
    val lastUpdated: Long = System.currentTimeMillis(),
    val hasAccepted: Boolean = true, // False if pending invite
    val rank: Int = 0
)
